package com.example.compose.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.R
import com.example.compose.network.AnalyzeRequest
import com.example.compose.network.AnalyzeResponse
import com.example.compose.network.RetrofitClient
import com.example.compose.ui.theme.Pretendard
import com.example.compose.utils.JsonManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Green = Color(0xFF18A957)
private val Dark = Color(0xFF111827)
private val GrayText = Color(0xFF6B7280)
private val SoftBg = Color(0xFFFAFAF8)
private val CardStroke = Color(0xFFE8EDF0)

private data class AnalysisCategory(
    val name: String,
    val amount: Int,
    val color: Color,
    val icon: ImageVector
)

@Composable
fun AnalysisScreen() {
    val context = LocalContext.current
    var transactions by remember { mutableStateOf(emptyList<Transaction>()) }
    var selectedMonth by remember { mutableStateOf("") }
    var analyzeResult by remember { mutableStateOf<AnalyzeResponse?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val json = JsonManager.readBankData(context)
        if (json.contains("없음")) return@LaunchedEffect

        transactions = runCatching { loadTransactions(json) }.getOrDefault(emptyList())
        selectedMonth = transactions.maxByOrNull { it.time }?.let { yearMonthKey(it.time) }
            ?: SimpleDateFormat("yyyy-MM", Locale.KOREA).format(Date())
    }

    val monthTransactions = remember(transactions, selectedMonth) {
        transactions.filter { yearMonthKey(it.time) == selectedMonth }
    }

    LaunchedEffect(monthTransactions) {
        if (monthTransactions.isEmpty()) {
            analyzeResult = null
            return@LaunchedEffect
        }

        RetrofitClient.api.analyzeConsumption(
            monthTransactions.map {
                AnalyzeRequest(money = it.money, type = it.type, receiver = it.receiver)
            }
        ).enqueue(object : Callback<AnalyzeResponse> {
            override fun onResponse(
                call: Call<AnalyzeResponse>,
                response: Response<AnalyzeResponse>
            ) {
                analyzeResult = response.body()
            }

            override fun onFailure(call: Call<AnalyzeResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AnalysisHeader()

        val tabs = listOf("요약", "소비패턴", "카테고리", "비교")
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            containerColor = Color.White,
            contentColor = Green,
            divider = {},
            indicator = { positions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(positions[selectedTab])
                        .padding(horizontal = 18.dp),
                    height = 2.dp,
                    color = Green
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontFamily = Pretendard,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == index) Green else Dark
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> SummaryTab(analyzeResult, monthTransactions)
            1 -> PatternTab(
                result = analyzeResult,
                transactions = monthTransactions,
                selectedMonth = selectedMonth,
                onMonthChange = { selectedMonth = shiftMonth(selectedMonth, it) }
            )
            2 -> CategoryTab(
                result = analyzeResult,
                selectedMonth = selectedMonth,
                onMonthChange = { selectedMonth = shiftMonth(selectedMonth, it) }
            )
            3 -> CompareTab(
                result = analyzeResult,
                selectedMonth = selectedMonth
            )
        }
    }
}

@Composable
private fun SummaryTab(result: AnalyzeResponse?, transactions: List<Transaction>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg),
        contentPadding = PaddingValues(20.dp, 22.dp, 20.dp, 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScoreCard(result) }
        item { SnapshotCard(result, transactions) }
        item { GoalStatusCard(result) }
    }
}

@Composable
private fun PatternTab(
    result: AnalyzeResponse?,
    transactions: List<Transaction>,
    selectedMonth: String,
    onMonthChange: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { PeriodMonthRow(selectedMonth, onMonthChange) }
        item { ConsumerTypeCard(result) }
        item { RhythmCard(transactions) }
        item { HeatMapCard(transactions) }
        item { PatternInsightSection(transactions) }
    }
}

@Composable
private fun CategoryTab(
    result: AnalyzeResponse?,
    selectedMonth: String,
    onMonthChange: (Int) -> Unit
) {
    val categories = categoriesOf(result)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { MonthOnlyRow(selectedMonth, onMonthChange) }
        item { CategoryDonutCard(categories) }
        item { CategoryTrendCard(categories) }
        item { CategoryTopFiveCard(categories) }
    }
}

@Composable
private fun CompareTab(result: AnalyzeResponse?, selectedMonth: String) {
    val categories = categoriesOf(result)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg),
        contentPadding = PaddingValues(20.dp, 22.dp, 20.dp, 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { CompareHeroCard() }
        item { MonthlyCompareCard(result, selectedMonth) }
        item { CategoryCompareCard(categories) }
        item { CompareTableCard(result) }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            content = content
        )
    }
}

@Composable
private fun ScoreCard(result: AnalyzeResponse?) {
    val score = scoreOf(result)
    SectionCard {
        BoxWithConstraints {
            val compact = maxWidth < 360.dp
            val chartSize = if (compact) 156.dp else 184.dp
            val textBlock: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier) {
                    Text("이번 달 소비 점수", style = titleStyle())
                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "$score",
                            fontFamily = Pretendard,
                            fontSize = if (compact) 44.sp else 52.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Dark
                        )
                        Text("/100점", fontFamily = Pretendard, fontSize = 18.sp, color = GrayText)
                    }
                    Badge("상위 ${(100 - score / 2).coerceIn(18, 45)}%")
                    Spacer(Modifier.height(18.dp))
                    Text(
                        if (score >= 80) "지난 달보다 소비 흐름이 안정적이에요!"
                        else "이번 달은 지출 리듬을 조금 조절해보면 좋아요.",
                        fontFamily = Pretendard,
                        fontSize = 15.sp,
                        color = Color(0xFF4B5563)
                    )
                }
            }

            if (compact) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    textBlock(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    RadarChart(result, Modifier.size(chartSize))
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    textBlock(Modifier.weight(1f))
                    RadarChart(result, Modifier.size(chartSize))
                }
            }
        }
    }
}

@Composable
private fun SnapshotCard(result: AnalyzeResponse?, transactions: List<Transaction>) {
    SectionCard {
        Text("소비 패턴 한눈에 보기", style = titleStyle())
        Spacer(Modifier.height(18.dp))
        BoxWithConstraints {
            val compact = maxWidth < 390.dp
            val items = listOf<@Composable (Modifier) -> Unit>(
                { modifier ->
                    MiniMetricCard(Icons.Default.CalendarMonth, "주로 소비하는 요일", mostSpentDay(transactions), "${dayShare(transactions)}%", modifier)
                },
                { modifier ->
                    MiniMetricCard(Icons.Default.Schedule, "주로 소비하는 시간", peakHourLabel(transactions), "${hourShare(transactions)}%", modifier)
                },
                { modifier ->
                    MiniMetricCard(Icons.Default.CreditCard, "평균 결제 금액", "%,d원".format(averageOutcome(transactions)), "건당", modifier)
                }
            )
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items.forEach { it(Modifier.fillMaxWidth()) }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items.forEach { it(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun GoalStatusCard(result: AnalyzeResponse?) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("목표 달성 현황", style = titleStyle(), modifier = Modifier.weight(1f))
            Text("전체 보기 〉", fontFamily = Pretendard, color = GrayText)
        }
        Spacer(Modifier.height(16.dp))
        BoxWithConstraints {
            if (maxWidth < 420.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProgressGoal("이번 달 예산", 550000, result?.outcome_money ?: 0, Modifier.fillMaxWidth())
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProgressGoal("이번 달 예산", 550000, result?.outcome_money ?: 0, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PeriodMonthRow(selectedMonth: String, onMonthChange: (Int) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val compact = maxWidth < 410.dp
        val selector: @Composable () -> Unit = {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF2F4F3))
                    .padding(4.dp)
            ) {
                listOf( "월간",).forEach { label ->
                    val selected = label == "월간"
                    Text(
                        text = label,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (selected) Color.White else Color.Transparent)
                            .padding(vertical = 10.dp),
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (selected) Green else Color(0xFF4B5563)
                    )
                }
            }
        }

        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.fillMaxWidth()) { selector() }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    MonthControl(selectedMonth, onMonthChange)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.widthIn(min = 230.dp, max = 280.dp)) { selector() }
                MonthControl(selectedMonth, onMonthChange)
            }
        }
    }
}

@Composable
private fun MonthOnlyRow(selectedMonth: String, onMonthChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MonthControl(selectedMonth, onMonthChange)
    }
}

@Composable
private fun MonthControl(selectedMonth: String, onMonthChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onMonthChange(-1) }) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "이전 달")
        }
        Text(
            koreanMonth(selectedMonth),
            fontFamily = Pretendard,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Dark
        )
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
        IconButton(onClick = { onMonthChange(1) }) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "다음 달")
        }
    }
}

@Composable
private fun ConsumerTypeCard(result: AnalyzeResponse?) {
    val score = scoreOf(result)
    val type = when {
        score >= 85 -> "절약형 소비자"
        score >= 70 -> "계획적인 소비자"
        score >= 55 -> "균형형 소비자"
        else -> "점검이 필요한 소비자"
    }
    SectionCard {
        BoxWithConstraints {
            val compact = maxWidth < 360.dp
            val chart: @Composable () -> Unit = {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { score / 100f },
                        modifier = Modifier.size(if (compact) 110.dp else 124.dp),
                        strokeWidth = 12.dp,
                        color = Green,
                        trackColor = Color(0xFFEDEDED),
                        strokeCap = StrokeCap.Round
                    )
                    Image(
                        painter = painterResource(R.drawable.character),
                        contentDescription = null,
                        modifier = Modifier.size(if (compact) 64.dp else 74.dp)
                    )
                }
            }
            val copy: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier) {
                    Text("나의 소비 패턴 유형", fontFamily = Pretendard, fontWeight = FontWeight.Bold, color = Dark)
                    Spacer(Modifier.height(18.dp))
                    Text(type, fontFamily = Pretendard, fontSize = if (compact) 25.sp else 30.sp, fontWeight = FontWeight.ExtraBold, color = Dark)
                    Spacer(Modifier.height(12.dp))
                    Text("규칙적인 소비 습관을 가지고 있어요.\n소비 계획을 잘 지키고 있어요!", fontFamily = Pretendard, color = GrayText, lineHeight = 24.sp)
                }
            }
            if (compact) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    copy(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    chart()
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    copy(Modifier.weight(1f))
                    chart()
                }
            }
        }
    }
}

@Composable
private fun RhythmCard(transactions: List<Transaction>) {
    val dayValues = weekSpending(transactions)
    val avg = dayValues.filter { it > 0 }.average().takeIf { !it.isNaN() } ?: 0.0
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("소비 리듬", style = titleStyle())
                Spacer(Modifier.height(6.dp))
                Text("이번 달은 평소보다 주말 소비가 많았어요.", fontFamily = Pretendard, color = GrayText)
            }
            Legend(Color(0xFFC2C8D0), "평균")
            Spacer(Modifier.width(12.dp))
            Legend(Green, "이번 달")
        }
        Spacer(Modifier.height(18.dp))
        LineChart(dayValues, avg.toFloat())
    }
}

@Composable
private fun HeatMapCard(transactions: List<Transaction>) {
    val matrix = heatValues(transactions)
    val maxValue = matrix.maxOfOrNull { row -> row.maxOrNull() ?: 0 } ?: 0
    SectionCard {
        Text("시간대별 소비 분포", style = titleStyle())
        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("0시", "4시", "8시", "12시", "16시", "20시").forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    fontFamily = Pretendard,
                    color = GrayText,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        val days = listOf("월", "화", "수", "목", "금", "토", "일")
        days.forEachIndexed { dayIndex, day ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day,
                    modifier = Modifier.width(24.dp),
                    fontFamily = Pretendard,
                    color = GrayText,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Row(modifier = Modifier.weight(1f)) {
                    repeat(24) { hour ->
                        val value = matrix[dayIndex][hour]
                        val level =
                            if (maxValue == 0) 0
                            else ((value.toFloat() / maxValue) * 5f).roundToInt().coerceIn(0, 5)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.2.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(heatColor(level))
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("적음", fontFamily = Pretendard, color = GrayText, fontSize = 12.sp)
            repeat(6) {
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(18.dp, 10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(heatColor(it))
                )
            }
            Text("많음", fontFamily = Pretendard, color = GrayText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PatternInsightSection(transactions: List<Transaction>) {
    SectionCard {
        Text("소비 패턴 인사이트", style = titleStyle())
        Spacer(Modifier.height(18.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            InsightCard(Icons.Default.ShoppingBag, "주말 소비 증가", "주말에 평균 ${weekendRate(transactions)}% 더 지출하고 있어요.", "▲ 27%")
            InsightCard(Icons.Default.Schedule, "오후 소비 집중", "오후 ${peakHourLabel(transactions)}에 소비가 집중돼요.", peakHourLabel(transactions))
            InsightCard(Icons.Default.CalendarMonth, "월초 소비 경향", "매월 1~5일에 소비가 많은 편이에요.", "1~5일")
        }
    }
}

@Composable
private fun CategoryDonutCard(categories: List<AnalysisCategory>) {
    SectionCard {
        Text("카테고리별 소비 현황", style = titleStyle())
        Spacer(Modifier.height(18.dp))
        BoxWithConstraints {
            val compact = maxWidth < 420.dp
            val chartSize = if (compact) 172.dp else 190.dp
            val list: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier) {
                    categories.forEach { category ->
                        val percent = percentOf(category.amount, categories.sumOf { it.amount })
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIcon(category)
                            Spacer(Modifier.width(10.dp))
                            Text(category.name, modifier = Modifier.weight(1f), fontFamily = Pretendard, fontWeight = FontWeight.Bold)
                            Text("%,d원".format(category.amount), fontFamily = Pretendard, color = Dark, fontSize = 13.sp)
                            Spacer(Modifier.width(10.dp))
                            Text("$percent%", fontFamily = Pretendard, color = category.color, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (compact) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DonutChart(categories, Modifier.size(chartSize))
                    Spacer(Modifier.height(18.dp))
                    list(Modifier.fillMaxWidth())
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DonutChart(categories, Modifier.size(chartSize))
                    Spacer(Modifier.width(18.dp))
                    list(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryTrendCard(categories: List<AnalysisCategory>) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("카테고리 변화 추이", style = titleStyle(), modifier = Modifier.weight(1f))
            TogglePill("금액", true)
            TogglePill("비율", false)
        }
        Spacer(Modifier.height(18.dp))
        StackedBars(categories)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            categories.forEach {
                Box(Modifier.size(8.dp).background(it.color, CircleShape))
                Spacer(Modifier.width(5.dp))
                Text(it.name, fontFamily = Pretendard, fontSize = 12.sp, color = GrayText)
                Spacer(Modifier.width(10.dp))
            }
        }
    }
}

@Composable
private fun CategoryTopFiveCard(categories: List<AnalysisCategory>) {
    val total = categories.sumOf { it.amount }.coerceAtLeast(1)
    SectionCard {
        Text("카테고리별 상세 내역 TOP 5", style = titleStyle())
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            categories.forEachIndexed { index, category ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (index == 0) Green else Color.White)
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text(category.name, color = if (index == 0) Color.White else Dark, fontFamily = Pretendard, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        categories.sortedByDescending { it.amount }.take(5).forEach { category ->
            val percent = category.amount * 100 / total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIcon(category)
                Spacer(Modifier.width(12.dp))
                Text(category.name, modifier = Modifier.weight(0.8f), fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { percent / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp),
                    color = Green,
                    trackColor = Color.Transparent
                )
                Spacer(Modifier.width(12.dp))
                Text("%,d원".format(category.amount), fontFamily = Pretendard, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                Text("$percent%", fontFamily = Pretendard, color = GrayText, fontSize = 13.sp)
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFCBD5E1))
            }
        }
    }
}

@Composable
private fun CompareHeroCard() {
    SectionCard {
        BoxWithConstraints {
            val compact = maxWidth < 360.dp
            val copy: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier) {
                    Text("나는 다른 사람들과\n비교하면 어떤 소비자일까?", fontFamily = Pretendard, fontSize = if (compact) 19.sp else 21.sp, fontWeight = FontWeight.ExtraBold, color = Dark, lineHeight = 28.sp)
                    Spacer(Modifier.height(18.dp))
                    Text("또래 평균과 비교한\n5월 소비 현황이에요.", fontFamily = Pretendard, color = GrayText, lineHeight = 24.sp)
                }
            }
            val graph: @Composable () -> Unit = {
                Canvas(Modifier.size(if (compact) 132.dp else 160.dp)) {
                    val base = size.height - 18f
                    val barWidth = size.width * 0.28f
                    drawRoundRect(Green, Offset(size.width * 0.18f, 36f), Size(barWidth, base - 36f), CornerRadius(8f, 8f))
                    drawRoundRect(Color(0xFFE5E7EB), Offset(size.width * 0.6f, 58f), Size(barWidth, base - 58f), CornerRadius(8f, 8f))
                    drawLine(Color(0xFFE5E7EB), Offset(0f, base), Offset(size.width, base), strokeWidth = 2f)
                }
            }
            if (compact) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    copy(Modifier.fillMaxWidth())
                    graph()
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    copy(Modifier.weight(1f))
                    graph()
                }
            }
        }
    }
}

@Composable
private fun MonthlyCompareCard(result: AnalyzeResponse?, selectedMonth: String) {
    val mine = result?.outcome_money ?: 0
    val avg = (mine * 1.14f).roundToInt().coerceAtLeast(492000)
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("월간 소비 비교", style = titleStyle(), modifier = Modifier.weight(1f))
            Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFF8FAFC)).padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text(koreanMonth(selectedMonth), fontFamily = Pretendard, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
        BoxWithConstraints {
            if (maxWidth < 420.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CompareAmountBox("나의 소비", mine, "월 평균보다 12% ↓ 적게 썼어요 ↓", Green, Modifier.fillMaxWidth())
                    Box(Modifier.size(46.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                        Text("VS", fontFamily = Pretendard, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B))
                    }
                    CompareAmountBox("또래 평균", avg, "", Color(0xFF64748B), Modifier.fillMaxWidth())
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompareAmountBox("나의 소비", mine, "월 평균보다 12% ↓ 적게 썼어요 ↓", Green, Modifier.weight(1f))
                    Box(Modifier.size(58.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                        Text("VS", fontFamily = Pretendard, fontWeight = FontWeight.ExtraBold, color = Color(0xFF64748B))
                    }
                    CompareAmountBox("또래 평균", avg, "", Color(0xFF64748B), Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        InfoLine("또래 평균은 20대 후반, 수도권 기준이에요.")
    }
}

@Composable
private fun CategoryCompareCard(categories: List<AnalysisCategory>) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("카테고리별 소비 비교", style = titleStyle(), modifier = Modifier.weight(1f))
            TogglePill("금액", false)
            TogglePill("비율", true)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Legend(Green, "나의 소비 비율")
            Spacer(Modifier.width(18.dp))
            Legend(Color(0xFFD1D5DB), "또래 평균 비율")
        }
        Spacer(Modifier.height(18.dp))
        CompareBars(categories)
        Spacer(Modifier.height(14.dp))
        InfoStrip("식비 지출 비율이 또래 평균보다 11% 높아요.\n외식보다는 직접 요리하면 지출을 줄일 수 있어요!")
    }
}

@Composable
private fun CompareTableCard(result: AnalyzeResponse?) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("항목별 비교", style = titleStyle(), modifier = Modifier.weight(1f))
            Text("전체 항목 보기 〉", fontFamily = Pretendard, color = GrayText)
        }
        Spacer(Modifier.height(14.dp))
        val rows = listOf(
            Triple("주 평균 소비 일수", "4.2일", "4.8일"),
            Triple("1회 평균 소비 금액", "%,d원".format(((result?.outcome_money ?: 0) / 12).coerceAtLeast(32000)), "38,000원"),
            Triple("월 평균 결제 횟수", "28회", "31회"),
            Triple("충동구매 비율", "21%", "28%")
        )
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.first, modifier = Modifier.weight(1.25f), fontFamily = Pretendard, fontWeight = FontWeight.Bold, color = Dark)
                Text(row.second, modifier = Modifier.weight(0.75f), fontFamily = Pretendard, color = Green, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Text("VS", modifier = Modifier.weight(0.35f), fontFamily = Pretendard, color = GrayText, textAlign = TextAlign.Center)
                Text(row.third, modifier = Modifier.weight(0.75f), fontFamily = Pretendard, color = GrayText, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Box(Modifier.size(30.dp).background(Color(0xFFE7F7EC), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun RadarChart(result: AnalyzeResponse?, modifier: Modifier) {
    val values = listOf(
        100 - (result?.spending_rate ?: 30.0),
        100 - (result?.shop_rate ?: 20.0),
        100 - (result?.cafe_rate ?: 10.0),
        100 - (result?.taxi_rate ?: 8.0),
        result?.saving_rate ?: 75.0
    ).map { it.toFloat().coerceIn(35f, 95f) }

    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.38f
        repeat(5) { ring ->
            val r = radius * (ring + 1) / 5f
            val path = polygonPath(center, r, 5)
            drawPath(path, Color(0xFFDCE5E0), style = Stroke(1.5f))
        }
        val valuePath = Path()
        values.forEachIndexed { index, value ->
            val angle = -PI / 2 + 2 * PI * index / values.size
            val point = Offset(
                center.x + cos(angle).toFloat() * radius * value / 100f,
                center.y + sin(angle).toFloat() * radius * value / 100f
            )
            if (index == 0) valuePath.moveTo(point.x, point.y) else valuePath.lineTo(point.x, point.y)
            drawCircle(Green, 4f, point)
        }
        valuePath.close()
        drawPath(valuePath, Green.copy(alpha = 0.24f))
        drawPath(valuePath, Green, style = Stroke(4f))
    }
}

private fun polygonPath(center: Offset, radius: Float, count: Int): Path {
    val path = Path()
    repeat(count) { index ->
        val angle = -PI / 2 + 2 * PI * index / count
        val point = Offset(
            center.x + cos(angle).toFloat() * radius,
            center.y + sin(angle).toFloat() * radius
        )
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    return path
}

@Composable
private fun DonutChart(categories: List<AnalysisCategory>, modifier: Modifier) {
    val total = categories.sumOf { it.amount }.coerceAtLeast(1)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            var start = -90f
            categories.forEach {
                val sweep = it.amount.toFloat() / total * 360f
                drawArc(
                    color = it.color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 44f)
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("%,d원".format(total), fontFamily = Pretendard, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Dark)
            Text("총 소비", fontFamily = Pretendard, color = GrayText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun LineChart(values: List<Int>, average: Float) {
    val labels = listOf("월", "화", "수", "목", "금", "토", "일")
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            val max = (values.maxOrNull() ?: 1).coerceAtLeast(average.toInt()).coerceAtLeast(1).toFloat()
            val left = 30f
            val bottom = size.height - 26f
            val width = size.width - left - 14f
            val height = size.height - 40f
            repeat(4) { i ->
                val y = bottom - height * i / 3f
                drawLine(Color(0xFFE5E7EB), Offset(left, y), Offset(size.width, y), strokeWidth = 1f)
            }
            val points = values.mapIndexed { index, value ->
                Offset(left + width * index / 6f, bottom - height * value / max)
            }
            val avgPoints = values.indices.map { index ->
                val wave = if (index % 2 == 0) -0.07f else 0.07f
                Offset(left + width * index / 6f, bottom - height * ((average / max) + wave).coerceIn(0f, 1f))
            }
            avgPoints.zipWithNext().forEach { drawLine(Color(0xFFC2C8D0), it.first, it.second, strokeWidth = 3f) }
            points.zipWithNext().forEach { drawLine(Green, it.first, it.second, strokeWidth = 4f) }
            points.forEach { drawCircle(Green, 6f, it) }
            drawLine(Color(0xFFE5E7EB), Offset(left, bottom), Offset(size.width, bottom), strokeWidth = 2f)
        }
        Row(Modifier.padding(start = 30.dp)) {
            labels.forEach { Text(it, modifier = Modifier.weight(1f), fontFamily = Pretendard, color = GrayText, fontSize = 12.sp, textAlign = TextAlign.Center) }
        }
    }
}

@Composable
private fun StackedBars(categories: List<AnalysisCategory>) {
    val months = listOf("12월", "1월", "2월", "3월", "4월", "5월")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        months.forEachIndexed { monthIndex, month ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Column(
                    modifier = Modifier
                        .height(150.dp)
                        .width(36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFF1F5F9)),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    categories.forEach { category ->
                        val factor = 0.85f + monthIndex * 0.03f
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(((22 + percentOf(category.amount, categories.sumOf { it.amount }) * 0.7f) * factor).dp)
                                .background(category.color)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(month, fontFamily = Pretendard, color = GrayText, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CompareBars(categories: List<AnalysisCategory>) {
    val total = categories.sumOf { it.amount }.coerceAtLeast(1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        categories.forEach { category ->
            val mine = percentOf(category.amount, total).coerceAtLeast(4)
            val avg = (mine * 0.75f + 5).roundToInt()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$mine%", fontFamily = Pretendard, color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Box(Modifier.width(18.dp).height((mine * 2.3f).dp).clip(RoundedCornerShape(5.dp, 5.dp, 0.dp, 0.dp)).background(Green))
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$avg%", fontFamily = Pretendard, color = GrayText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Box(Modifier.width(18.dp).height((avg * 2.3f).dp).clip(RoundedCornerShape(5.dp, 5.dp, 0.dp, 0.dp)).background(Color(0xFFD1D5DB)))
                    }
                }
                Spacer(Modifier.height(8.dp))
                CategoryIcon(category)
                Text(category.name, fontFamily = Pretendard, fontSize = 11.sp, color = GrayText)
            }
        }
    }
}

@Composable
private fun MiniMetricCard(icon: ImageVector, label: String, value: String, sub: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF2FAF5))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = Dark, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(9.dp))
        Text(label, fontFamily = Pretendard, color = Color(0xFF4B5563), fontSize = 11.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(value, fontFamily = Pretendard, color = Dark, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Text(sub, fontFamily = Pretendard, color = Green, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun InsightCard(icon: ImageVector, title: String, desc: String, badge: String) {
    Column(
        modifier = Modifier
            .width(178.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Box(Modifier.size(38.dp).background(Color(0xFFE8F7EE), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Green, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(title, fontFamily = Pretendard, fontWeight = FontWeight.ExtraBold, color = Dark)
        Spacer(Modifier.height(6.dp))
        Text(desc, fontFamily = Pretendard, color = GrayText, fontSize = 13.sp, lineHeight = 18.sp)
        Spacer(Modifier.weight(1f))
        Badge(badge)
    }
}

@Composable
private fun CategoryIcon(category: AnalysisCategory) {
    Box(Modifier.size(34.dp).background(category.color.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) {
        Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun Badge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFDDF6D3))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontFamily = Pretendard, color = Green, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
    }
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(text, fontFamily = Pretendard, color = Color(0xFF4B5563), fontSize = 12.sp)
    }
}

@Composable
private fun TogglePill(text: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) Color(0xFFEFF8F2) else Color(0xFFF5F5F5))
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(text, fontFamily = Pretendard, color = if (active) Green else GrayText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun InfoStrip(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF1F5F9))
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontFamily = Pretendard, color = Color(0xFF4B5563), fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun InfoLine(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontFamily = Pretendard, color = Color(0xFF64748B), fontSize = 13.sp)
    }
}

@Composable
private fun ProgressGoal(title: String, target: Int, current: Int, modifier: Modifier) {
    val progress = (current.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
    Row(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(64.dp), color = Green, trackColor = Color(0xFFEDEDED), strokeWidth = 7.dp)
            Text("${(progress * 100).roundToInt()}%", fontFamily = Pretendard, fontWeight = FontWeight.ExtraBold, color = Dark)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontFamily = Pretendard, color = Dark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text("%,d원 중".format(target), fontFamily = Pretendard, color = GrayText, fontSize = 13.sp)
            Text("%,d원 사용".format(current), fontFamily = Pretendard, color = Dark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(5.dp), color = Green, trackColor = Color(0xFFE5E7EB))
        }
    }
}

@Composable
private fun CompareAmountBox(label: String, amount: Int, desc: String, tint: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (tint == Green) Color(0xFFF1FBF4) else Color(0xFFF8FAFC))
            .padding(18.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, fontFamily = Pretendard, color = tint, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Text("%,d원".format(amount), fontFamily = Pretendard, color = Dark, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
        if (desc.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(desc, fontFamily = Pretendard, color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun titleStyle() = androidx.compose.ui.text.TextStyle(
    fontFamily = Pretendard,
    fontSize = 20.sp,
    fontWeight = FontWeight.ExtraBold,
    color = Dark
)

private fun categoriesOf(result: AnalyzeResponse?): List<AnalysisCategory> {
    val known = listOf(
        AnalysisCategory("식비", result?.food_money ?: 0, Color(0xFF67C77B), Icons.Default.LocalDining),
        AnalysisCategory("교통", result?.taxi_money ?: 0, Color(0xFF48C8D2), Icons.Default.Train),
        AnalysisCategory("쇼핑", result?.shop_money ?: 0, Color(0xFFFFC233), Icons.Default.LocalMall),
        AnalysisCategory("카페/간식", result?.cafe_money ?: 0, Color(0xFFFF744A), Icons.Default.LocalCafe),
        AnalysisCategory("문화/여가", result?.play_money ?: 0, Color(0xFF8B85D8), Icons.Default.StarBorder)
    )
    val totalKnown = known.sumOf { it.amount }
    val total = result?.outcome_money ?: totalKnown
    val etc = (total - totalKnown).coerceAtLeast(0)
    return known + AnalysisCategory("기타", etc, Color(0xFFD4D4D4), Icons.Default.MoreHoriz)
}

private fun scoreOf(result: AnalyzeResponse?): Int {
    val spendingRate = result?.spending_rate ?: return 82
    return (100 - (spendingRate - 55).coerceAtLeast(0.0) * 0.8).roundToInt().coerceIn(35, 96)
}

private fun insightTitle(result: AnalyzeResponse?): String {
    return when {
        result == null -> "소비 데이터를 분석하고 있어요."
        result.food_status.contains("과소비") -> "식비 지출이 이번 달에 높게 나타났어요."
        result.cafe_status.contains("과소비") -> "카페 지출이 지난 달 대비 증가했어요."
        result.shop_status.contains("과소비") -> "쇼핑 지출이 예산보다 빠르게 늘고 있어요."
        else -> "식비 지출이 지난 달 대비 15% 증가했어요."
    }
}

private fun outcomeTransactions(transactions: List<Transaction>): List<Transaction> {
    return transactions.filter { !it.type.contains("입금") && !it.type.contains("income", ignoreCase = true) }
}

private fun moneyOf(transaction: Transaction): Int {
    return transaction.money.replace(",", "").replace("원", "").trim().toIntOrNull() ?: 0
}

private fun weekSpending(transactions: List<Transaction>): List<Int> {
    val sums = MutableList(7) { 0 }
    val cal = Calendar.getInstance()
    outcomeTransactions(transactions).forEach {
        cal.timeInMillis = it.time
        val index = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        sums[index] += moneyOf(it)
    }
    return sums
}

private fun heatValues(transactions: List<Transaction>): Array<IntArray> {
    val values = Array(7) { IntArray(24) }
    val cal = Calendar.getInstance()
    outcomeTransactions(transactions).forEach {
        cal.timeInMillis = it.time
        val day = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        values[day][hour] += 1
    }
    return values
}

private fun heatColor(level: Int): Color {
    return listOf(
        Color(0xFFF5F7F6),
        Color(0xFFEAF7EF),
        Color(0xFFD3F0DD),
        Color(0xFFB4E5C5),
        Color(0xFF8FD8AA),
        Color(0xFF5BC37F)
    )[level.coerceIn(0, 5)]
}

private fun mostSpentDay(transactions: List<Transaction>): String {
    val labels = listOf("월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일")
    val values = weekSpending(transactions)
    return labels[values.indices.maxByOrNull { values[it] } ?: 4]
}

private fun dayShare(transactions: List<Transaction>): Int {
    val values = weekSpending(transactions)
    val total = values.sum().coerceAtLeast(1)
    return values.maxOrNull()?.times(100)?.div(total) ?: 0
}

private fun hourShare(transactions: List<Transaction>): Int {
    val counts = IntArray(24)
    val cal = Calendar.getInstance()
    outcomeTransactions(transactions).forEach {
        cal.timeInMillis = it.time
        counts[cal.get(Calendar.HOUR_OF_DAY)] += 1
    }
    return counts.maxOrNull()?.times(100)?.div(outcomeTransactions(transactions).size.coerceAtLeast(1)) ?: 0
}

private fun peakHourLabel(transactions: List<Transaction>): String {
    val counts = IntArray(24)
    val cal = Calendar.getInstance()
    outcomeTransactions(transactions).forEach {
        cal.timeInMillis = it.time
        counts[cal.get(Calendar.HOUR_OF_DAY)] += 1
    }
    val hour = counts.indices.maxByOrNull { counts[it] } ?: 14
    return if (hour < 12) "오전 ${hour}시" else "오후 ${if (hour == 12) 12 else hour - 12}시"
}

private fun averageOutcome(transactions: List<Transaction>): Int {
    val items = outcomeTransactions(transactions)
    return if (items.isEmpty()) 0 else items.sumOf { moneyOf(it) } / items.size
}

private fun weekendRate(transactions: List<Transaction>): Int {
    val cal = Calendar.getInstance()
    var weekday = 0
    var weekend = 0
    outcomeTransactions(transactions).forEach {
        cal.timeInMillis = it.time
        val money = moneyOf(it)
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY, Calendar.SUNDAY -> weekend += money
            else -> weekday += money
        }
    }
    return if (weekday == 0) 0 else ((weekend - weekday / 5f * 2f) / (weekday / 5f * 2f).coerceAtLeast(1f) * 100).roundToInt().coerceAtLeast(0)
}

private fun percentOf(value: Int, total: Int): Int {
    return if (total <= 0) 0 else (value * 100f / total).roundToInt()
}

private fun yearMonthKey(time: Long): String {
    return SimpleDateFormat("yyyy-MM", Locale.KOREA).format(Date(time))
}

private fun koreanMonth(key: String): String {
    val parts = key.split("-")
    return if (parts.size == 2) "${parts[0]}년 ${parts[1].toIntOrNull() ?: parts[1]}월" else "이번 달"
}

private fun shiftMonth(current: String, offset: Int): String {
    val cal = Calendar.getInstance()
    runCatching {
        cal.time = SimpleDateFormat("yyyy-MM", Locale.KOREA).parse(current) ?: Date()
    }
    cal.add(Calendar.MONTH, offset)
    return SimpleDateFormat("yyyy-MM", Locale.KOREA).format(cal.time)
}
