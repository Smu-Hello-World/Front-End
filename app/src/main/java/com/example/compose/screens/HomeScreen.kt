package com.example.compose.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.R
import com.example.compose.network.AnalyzeRequest
import com.example.compose.network.AnalyzeResponse
import com.example.compose.network.RetrofitClient
import com.example.compose.ui.theme.Pretendard
import com.example.compose.utils.JsonManager
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.compose.Screen
import androidx.compose.runtime.mutableStateOf

@Composable
fun HomeScreen(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit
) {
    var incomeMoney by remember {
        mutableIntStateOf(0)
    }

    var outcomeMoney by remember {
        mutableIntStateOf(0)
    }

    var analyzeResult by remember {
        mutableStateOf<AnalyzeResponse?>(null)
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {

        try {

            val jsonString =
                JsonManager.readBankData(context)

            if (jsonString == "데이터 없음")
                return@LaunchedEffect

            val jsonArray =
                JSONArray(jsonString)

            val requestList =
                mutableListOf<AnalyzeRequest>()


            for (i in 0 until jsonArray.length()) {

                val item =
                    jsonArray.getJSONObject(i)

                requestList.add(

                    AnalyzeRequest(

                        money =
                            item.getString("money"),

                        type =
                            item.getString("type"),

                        receiver =
                            item.getString("receiver")
                    )
                )
            }

            RetrofitClient.api
                .analyzeConsumption(requestList)
                .enqueue(

                    object : Callback<AnalyzeResponse> {

                        override fun onResponse(
                            call: Call<AnalyzeResponse>,
                            response: Response<AnalyzeResponse>
                        ) {

                            val result =
                                response.body()

                            incomeMoney =
                                result?.income_money ?: 0

                            outcomeMoney =
                                result?.outcome_money ?: 0

                            analyzeResult = result
                        }

                        override fun onFailure(
                            call: Call<AnalyzeResponse>,
                            t: Throwable
                        ) {

                            t.printStackTrace()
                        }
                    }
                )

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
    Scaffold(
        containerColor = Color(0xFFF8F7F3),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                bottom = 100.dp
            )
        ) {
            item {
                TopBar()
                Spacer(modifier = Modifier.height(18.dp))
                HeroSection()
                SummaryCard(
                    income = incomeMoney,
                    outcome = outcomeMoney,
                    onDetailClick = {
                        onScreenChange(Screen.ANALYSIS)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TipCard()
                Spacer(modifier = Modifier.height(16.dp))
                CategoryHeader()
                Spacer(modifier = Modifier.height(10.dp))
                TopCategoryRow(
                    analyzeResult
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = null,
            tint = Color(0xFF111111),
            modifier = Modifier.size(28.dp)
        )

    }
}

@Composable
private fun HeroSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "오늘의 소비,",
                fontSize = 28.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111),
                lineHeight = 32.sp
            )
            Row {
                Text(
                    text = "과소비",
                    fontSize = 28.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF35A65A),
                    lineHeight = 32.sp
                )
                Text(
                    text = "일까요?",
                    fontSize = 28.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111),
                    lineHeight = 32.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "똑똑한 소비 습관을 함께 만들어요",
                fontSize = 13.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF666666)
            )
        }

        Box(
            modifier = Modifier
                .width(150.dp)
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.character),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .scale(3f)
            )
        }
    }
}
@Composable
private fun TipCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F4E8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "오늘의 한 줄 팁",
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF24A050)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "작은 지출도 쌓이면 큰 금액이 돼요!",
                    fontSize = 12.sp,
                    fontFamily = Pretendard,
                    color = Color(0xFF222222)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "커피 한 잔 대신 텀블러를 챙겨보세요",
                    fontSize = 11.sp,
                    fontFamily = Pretendard,
                    color = Color(0xFF444444)
                )
            }

            Text(
                text = "🥤",
                fontSize = 42.sp
            )
        }
    }
}

@Composable
private fun SummaryCard(
    income: Int,
    outcome: Int,
    onDetailClick: () -> Unit
) {
    val context = LocalContext.current

    val progress =

        if (income == 0)
            0f
        else
            outcome.toFloat() / income.toFloat()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "이번 달 소비 요약",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Pretendard,
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "5.1 - 5.31",
                    fontFamily = Pretendard,
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "총 지출",
                        fontSize = 11.sp,
                        fontFamily = Pretendard,
                        color = Color(0xFF777777)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = outcome.toString() + "원",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Pretendard,
                        color = Color(0xFF111111)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "예산 " + income.toString() + "원",
                        fontSize = 12.sp,
                        fontFamily = Pretendard,
                        color = Color(0xFF777777)
                    )
                }

                Box(
                    modifier = Modifier.size(86.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        strokeWidth = 8.dp,
                        color = Color(0xFF35A65A),
                        trackColor = Color(0xFFE2F1E5),
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 18.sp,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF35A65A),
                trackColor = Color(0xFFE2F1E5)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "상세 분석 보기 >",
                    color = Color(0xFF35A65A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Pretendard,
                    modifier = Modifier.clickable {
                        onDetailClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "카테고리별 지출 TOP 3",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Pretendard,
            color = Color(0xFF111111)
        )
    }
}

@Composable
private fun TopCategoryRow(
    result: AnalyzeResponse?
) {

    val categories = listOf(

        Triple(
            "식비",
            result?.food_money ?: 0,
            Pair("🍴", Color(0xFFEEF8EA))
        ),

        Triple(
            "카페",
            result?.cafe_money ?: 0,
            Pair("☕", Color(0xFFEAF4FF))
        ),

        Triple(
            "편의점",
            result?.cvs_money ?: 0,
            Pair("🏪", Color(0xFFFFF4E5))
        ),

        Triple(
            "교통",
            result?.taxi_money ?: 0,
            Pair("🚕", Color(0xFFFFF4E5))
        ),

        Triple(
            "쇼핑",
            result?.shop_money ?: 0,
            Pair("👜", Color(0xFFF3EEFF))
        )
    )

    val total =
        categories.sumOf { it.second }

    val top3 =
        categories
            .sortedByDescending { it.second }
            .take(3)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        top3.forEach { category ->

            val title = category.first
            val amount = category.second
            val icon = category.third.first
            val color = category.third.second

            val percent =

                if (total == 0)
                    "0%"
                else
                    "${amount * 100 / total}%"

            SmallCategoryCard(

                modifier =
                    Modifier.weight(1f),

                bg = color,

                icon = icon,

                title = title,

                amount =
                    "%,d원".format(amount),

                percent = percent
            )
        }
    }
}

@Composable
private fun SmallCategoryCard(
    modifier: Modifier,
    bg: Color,
    icon: String,
    title: String,
    amount: String,
    percent: String
) {
    Card(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontFamily = Pretendard,
                color = Color(0xFF444444)
            )
            Text(
                text = amount,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
                color = Color(0xFF111111)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = percent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
                color = Color(0xFF35A65A)
            )
        }
    }
}
