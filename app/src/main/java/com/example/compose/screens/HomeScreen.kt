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
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

data class CategoryItem(

    val title: String,

    val amount: Int,

    val iconRes: Int,

    val bgColor: Color,

    val accentColor: Color
)

data class TipData(

    val title: String,

    val description: String,

    val imageRes: Int
)

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
                Spacer(modifier = Modifier.height(16.dp))
                HeroSection()
                SummaryCard(
                    income = incomeMoney,
                    outcome = outcomeMoney,
                    onDetailClick = {
                        onScreenChange(Screen.ANALYSIS)
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
                TipCard(
                    analyzeResult
                )
                Spacer(modifier = Modifier.height(14.dp))
                CategoryHeader()
                Spacer(modifier = Modifier.height(10.dp))
                TopCategoryRow(
                    analyzeResult
                )
            }
        }
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

fun getTodayTip(
    result: AnalyzeResponse?
): TipData {

    if (result == null) {

        return TipData(

            "소비 분석 준비중",

            "소비 데이터를 수집하고 있어요",

            R.drawable.tip_saving
        )
    }

    return when {

        result.shop_status == "과소비" ->

            TipData(

                "쇼핑 과소비",

                "구매 전 하루만 고민해보세요",

                R.drawable.tip_shopping
            )

        result.cafe_status == "과소비" ->

            TipData(

                "카페 과소비",

                "텀블러를 사용하면 지출을 줄일 수 있어요",

                R.drawable.tip_coffee
            )

        result.food_status == "과소비" ->

            TipData(

                "식비 과소비",

                "배달보다 학식이나 직접 요리를 추천해요",

                R.drawable.tip_food
            )

        result.play_status == "과소비" ->

            TipData(

                "놀거리 과소비",

                "이번 주는 무료 취미를 즐겨보세요",

                R.drawable.tip_play
            )

        else -> {

            val randomTips = listOf(

                TipData(
                    "절약 팁",
                    "이번 달 소비 목표를 세워보세요",
                    R.drawable.tip_target
                ),

                TipData(
                    "절약 팁",
                    "작은 저축이 큰 자산이 됩니다",
                    R.drawable.tip_saving
                ),
            )

            randomTips.random()
        }
    }
}

@Composable
private fun TipCard(
    result: AnalyzeResponse?
) {
    val tip = getTodayTip(result)

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
                    text = tip.title,
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF24A050)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tip.description,
                    fontSize = 12.sp,
                    fontFamily = Pretendard,
                    color = Color(0xFF222222)
                )
            }
            Image(

                painter =
                    painterResource(
                        tip.imageRes
                    ),

                contentDescription = null,

                modifier = Modifier.size(40.dp)
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

        CategoryItem(
            "식비",
            result?.food_money ?: 0,
            R.drawable.food,
            Color(0xFFEEF8EA),
            Color(0xFF56C58A)
        ),

        CategoryItem(
            "카페",
            result?.cafe_money ?: 0,
            R.drawable.cafe,
            Color(0xFFEAF4FF),
            Color(0xFF4AA8FF)

        ),

        CategoryItem(
            "편의점",
            result?.cvs_money ?: 0,
            R.drawable.store,
            Color(0xFFFFF4E5),
            Color(0xFFFFB347)
        ),

        CategoryItem(
            "교통",
            result?.taxi_money ?: 0,
            R.drawable.car,
            Color(0xFFFFF4E5),
            Color(0xFFFF9D2E)
        ),

        CategoryItem(
            "쇼핑",
            result?.shop_money ?: 0,
            R.drawable.shopping,
            Color(0xFFF3EEFF),
            Color(0xFF6366F1)
        )
    )

    val total =
        categories.sumOf { it.amount }

    val top3 =
        categories
            .sortedByDescending { it.amount }
            .take(3)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        top3.forEach { category ->

            val title = category.title

            val amount = category.amount

            val iconRes = category.iconRes

            val color = category.bgColor

            val percent =

                if (total == 0)
                    "0%"
                else
                    "${amount * 100 / total}%"

            val accentColor =
                category.accentColor

            SmallCategoryCard(

                modifier = Modifier.weight(1f),

                bg = color,

                iconRes = iconRes,

                title = title,

                amount = "%,d원".format(amount),

                percent = percent,

                accentColor = accentColor
            )
        }
    }
}

@Composable
private fun SmallCategoryCard(
    modifier: Modifier,
    bg: Color,
    iconRes: Int,
    title: String,
    amount: String,
    percent: String,
    accentColor: Color
) {
    Card(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = bg
        )
    ) {
        Row(

            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Image(

                painter =
                    painterResource(iconRes),

                contentDescription = null,

                modifier = Modifier.size(20.dp)
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column {

                Text(

                    text = title,

                    fontSize = 10.sp,

                    fontWeight = FontWeight.Medium,

                    color = Color.Gray
                )

                Text(

                    text = amount,

                    fontSize = 12.sp,

                    fontWeight = FontWeight.SemiBold
                )

                Text(

                    text = percent,

                    fontSize = 12.sp,

                    fontWeight = FontWeight.SemiBold,

                    color = accentColor
                )
            }
        }
    }
}
