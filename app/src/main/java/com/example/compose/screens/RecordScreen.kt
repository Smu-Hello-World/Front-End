package com.example.compose.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.ui.theme.Pretendard
import com.example.compose.utils.JsonManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.CircleShape
import com.example.compose.network.AnalyzeRequest
import com.example.compose.network.AnalyzeResponse
import com.example.compose.network.RetrofitClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

private fun loadTransactions(
    jsonString: String
): List<Transaction> {

    val list =
        mutableListOf<Transaction>()

    val jsonArray =
        org.json.JSONArray(jsonString)

    for (i in 0 until jsonArray.length()) {

        val item =
            jsonArray.getJSONObject(i)

        list.add(

            Transaction(

                type =
                    item.optString("type"),

                money =
                    item.optString("money"),

                sender =
                    item.optString("sender"),

                receiver =
                    item.optString("receiver"),

                time =
                    item.optLong("time")
            )
        )
    }

    return list.reversed()
}
@Composable
fun RecordScreen() {
    var analyzeResult by remember {
        mutableStateOf<AnalyzeResponse?>(
            null
        )
    }

    val context =
        LocalContext.current

    var transactions by remember {

        mutableStateOf(
            emptyList<Transaction>()
        )
    }

    LaunchedEffect(Unit) {

        val json =
            JsonManager.readBankData(
                context
            )

        Log.d(
            "JSON_TEST",
            json
        )


        if (json == "데이터 없음") {
            return@LaunchedEffect
        }
        val transactions =
            loadTransactions(json)

        val requestList: List<AnalyzeRequest> =

            transactions.map {

                AnalyzeRequest(

                    money = it.money,

                    type = it.type,

                    receiver = it.receiver
                )
            }

        RetrofitClient.api
            .analyzeConsumption(
                requestList
            )
            .enqueue(

                object :
                    Callback<AnalyzeResponse> {

                    override fun onResponse(
                        call: Call<AnalyzeResponse>,
                        response: Response<AnalyzeResponse>
                    ) {

                        analyzeResult =
                            response.body()
                    }

                    override fun onFailure(
                        call: Call<AnalyzeResponse>,
                        t: Throwable
                    ) {

                        t.printStackTrace()
                    }
                }
            )
    }

    LazyColumn(


        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF8F7F3)
            )
            .padding(16.dp)

    ) {

        item {

            RecordHeader()

            Spacer(
                Modifier.height(20.dp)
            )

            FilterRow()

            Spacer(
                Modifier.height(16.dp)
            )

            MonthSelector()

            Spacer(
                Modifier.height(16.dp)
            )

            SummarySection(
                transactions
            )

            Spacer(
                Modifier.height(16.dp)
            )

            StatusCard()

            Spacer(
                Modifier.height(16.dp)
            )

            CategoryCard()

            Spacer(
                Modifier.height(20.dp)
            )

            Text(
                text = "최근 기록",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                Modifier.height(12.dp)
            )
        }

        items(transactions) {

            TransactionItem(it)
        }
    }
}

@Composable
fun RecordHeader() {

    Text(
        text = "기록",
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = Pretendard
    )
}

@Composable
fun SummarySection(

    transactions:
    List<Transaction>
) {

    val income =

        transactions

            .filter {

                it.type == "입금"
            }

            .sumOf {

                it.money.toIntOrNull()
                    ?: 0
            }

    val outcome =

        transactions

            .filter {

                it.type == "출금"
            }

            .sumOf {

                it.money.toIntOrNull()
                    ?: 0
            }

    RecordSummaryCard(
        income,
        outcome
    )
}

@Composable
fun FilterRow() {

    Row(

        modifier = Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceEvenly
    ) {

        FilterChip("전체", true)

        FilterChip("수입", false)

        FilterChip("지출", false)
    }
}

@Composable
fun FilterChip(

    text: String,

    selected: Boolean
) {

    Box(

        modifier = Modifier

            .background(

                if (selected)
                    Color(0xFFEAF6EC)
                else
                    Color.White,

                RoundedCornerShape(20.dp)
            )

            .padding(
                horizontal = 40.dp,
                vertical = 12.dp
            )

    ) {

        Text(
            text = text,
            color =
                if (selected)
                    Color(0xFF2CA85E)
                else
                    Color.Black
        )
    }
}

@Composable
fun MonthSelector() {

    Row(

        modifier = Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text("<")

        Text(
            "2024년 5월",
            fontWeight = FontWeight.Bold
        )

        Text(">")
    }
}

@Composable
fun RecordSummaryCard(
    income: Int,

    outcome: Int
) {

    Card(

        shape = RoundedCornerShape(24.dp),

        colors =
            CardDefaults.cardColors(
                containerColor = Color.White
            )

    ) {

        Column(

            modifier = Modifier.padding(20.dp)

        ) {

            Text(
                "이번 달 지출",
                color = Color.Gray
            )

            Spacer(
                Modifier.height(8.dp)
            )

            Text(

                text =
                    "${outcome}원",

                fontSize = 32.sp,

                fontWeight =
                    FontWeight.Bold
            )
            Spacer(
                Modifier.height(8.dp)
            )

            Text(

                text =
                    "예산 " + "${income}원",
            )
        }
    }
}


@Composable
fun StatusCard() {

    Card(

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFFF2F8F0)
            )

    ) {

        Row(

            modifier =
                Modifier.padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                "☁",
                fontSize = 40.sp
            )

            Spacer(
                Modifier.width(12.dp)
            )

            Column {

                Text(
                    "좋아요!",
                    color = Color(0xFF2CA85E),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "예산 범위 내에서 소비하고 있어요."
                )
            }
        }
    }
}

@Composable
fun CategoryCard() {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp),

        colors =
            CardDefaults.cardColors(
                containerColor = Color.White
            )

    ) {

        Column(

            modifier =
                Modifier.padding(20.dp)
        ) {

            Text(

                text = "카테고리별 지출",

                fontWeight =
                    FontWeight.Bold,

                fontSize = 18.sp
            )

            Spacer(
                Modifier.height(20.dp)
            )

            Box(

                modifier =
                    Modifier
                        .size(160.dp)
                        .align(
                            Alignment.CenterHorizontally
                        )
                        .background(
                            Color(0xFFEAF6EC),
                            CircleShape
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(

                    text = "39%",

                    fontSize = 30.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        Color(0xFF2CA85E)
                )
            }

            Spacer(
                Modifier.height(20.dp)
            )

            Text("🍴 식비 39%")
            Spacer(Modifier.height(6.dp))

            Text("🚕 교통 18%")
            Spacer(Modifier.height(6.dp))

            Text("🛍 쇼핑 15%")
        }
    }
}

@Composable
fun TransactionItem(

    item: Transaction
) {

    val amountColor =

        if (
            item.type == "입금"
        )
            Color(
                0xFF2CA85E
            )
        else
            Color.Black

    Card(

        shape =
            RoundedCornerShape(
                18.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        modifier =
            Modifier

                .fillMaxWidth()

                .padding(
                    vertical = 4.dp
                )

    ) {

        Row(

            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(

                text =

                    when (

                        item.type
                    ) {

                        "입금" ->
                            "💰"

                        else ->
                            "💳"
                    },

                fontSize = 28.sp
            )

            Spacer(
                Modifier.width(12.dp)
            )

            Column(

                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(

                    text =

                        if (
                            item.receiver.isNotBlank()
                        )

                            item.receiver

                        else

                            item.sender,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    item.type
                )
            }

            Text(

                text =

                    if (
                        item.type == "입금"
                    )

                        "+${item.money}원"

                    else

                        "-${item.money}원",

                color =
                    amountColor,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}
