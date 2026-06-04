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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.Button

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
        transactions =
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

                        Log.d(
                            "API_RESULT",
                            response.body().toString()
                        )

                        analyzeResult =
                            response.body()
                    }

                    override fun onFailure(
                        call: Call<AnalyzeResponse>,
                        t: Throwable
                    ) {

                        Log.e(
                            "API_TEST",
                            "실패",
                            t
                        )
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

            val context = LocalContext.current

            Button(

                onClick = {

                    insertDummyData(
                        context
                    )
                }

            ) {

                Text(
                    "더미 데이터 생성"
                )
            }

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

            CategoryCard(
                analyzeResult
            )

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
fun CategoryCard(
    result: AnalyzeResponse?
) {
    val categories = listOf(

        "식비" to (result?.food_money ?: 0),

        "카페" to (result?.cafe_money ?: 0),

        "교통" to (result?.taxi_money ?: 0),

        "쇼핑" to (result?.shop_money ?: 0),

        "편의점" to (result?.cvs_money ?: 0)
    )

    val total =

        categories.sumOf {
            it.second
        }

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
                        .fillMaxWidth(),

                contentAlignment =
                    Alignment.Center
            ) {

                Canvas(

                    modifier =
                        Modifier.size(180.dp)
                ) {

                    val colors = listOf(

                        Color(0xFF4CAF50),

                        Color(0xFFFF9800),

                        Color(0xFF2196F3),

                        Color(0xFFE91E63),

                        Color(0xFF9C27B0)
                    )

                    var startAngle = -90f

                    categories.forEachIndexed {

                            index,
                            category ->

                        val value =
                            category.second

                        val sweepAngle =

                            if (total == 0)

                                0f

                            else

                                value.toFloat() /
                                        total *
                                        360f

                        drawArc(

                            color =
                                colors[index],

                            startAngle =
                                startAngle,

                            sweepAngle =
                                sweepAngle,

                            useCenter =
                                false,

                            style =
                                Stroke(
                                    width = 40f
                                ),

                            size =
                                Size(
                                    size.width,
                                    size.height
                                )
                        )

                        startAngle +=
                            sweepAngle
                    }
                }

                Text(

                    text =

                        if (total == 0)

                            "0원"

                        else

                            "${total}원",

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                Modifier.height(20.dp)
            )
            categories.forEach {

                val percent =

                    if (total == 0)

                        0

                    else

                        (
                                it.second * 100
                                        / total
                                )

                Text(
                    "${it.first} ${percent}%"
                )

                Spacer(
                    Modifier.height(6.dp)
                )
            }
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

private fun insertDummyData(
    context: android.content.Context
) {

    JsonManager.saveBankData(
        context,
        "입금",
        "1500000",
        "회사",
        "입출금통장"
    )

    JsonManager.saveBankData(
        context,
        "출금",
        "4500",
        "",
        "스타벅스"
    )

    JsonManager.saveBankData(
        context,
        "출금",
        "12000",
        "",
        "맥도날드"
    )

    JsonManager.saveBankData(
        context,
        "출금",
        "8000",
        "",
        "카카오T"
    )

    JsonManager.saveBankData(
        context,
        "출금",
        "3500",
        "",
        "GS25"
    )

    JsonManager.saveBankData(
        context,
        "출금",
        "59000",
        "",
        "무신사"
    )

    JsonManager.saveBankData(
        context,
        "출금",
        "15000",
        "",
        "올리브영"
    )
}
