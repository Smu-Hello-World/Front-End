package com.example.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import com.example.compose.network.AnalyzeResponse
import androidx.compose.ui.draw.clip

import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight

fun getConsumerType(
    result: AnalyzeResponse?
): Pair<String, Int> {

    if (result == null)
        return "분석중" to 0

    val rate =
        result.spending_rate

    val type = when {

        rate < 40 ->
            "절약형 소비자"

        rate < 70 ->
            "계획적인 소비자"

        rate < 90 ->
            "일반 소비자"

        else ->
            "과소비 위험형"
    }

    val score =
        (100 - rate)
            .coerceIn(0.0, 100.0)
            .toInt()

    return type to score
}

@Composable
fun AnalysisPatternTab(

    transactions: List<Transaction>,

    analyzeResult: AnalyzeResponse?

) {

    var selectedPeriod by remember {

        mutableStateOf(
            "월간"
        )
    }

    var selectedMonth by remember {

        mutableStateOf(
            "2025년 6월"
        )
    }

    var selectedTab by remember {
        mutableIntStateOf(1)
    }

    LazyColumn(

        modifier = Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(16.dp)

    ) {

        item {

            PatternTopBar(

                selectedPeriod =
                    selectedPeriod,

                selectedMonth =
                    selectedMonth,

                onPeriodChange = {

                    selectedPeriod = it
                },

                onPrevMonth = {

                },

                onNextMonth = {

                }
            )
        }

        item {
            ConsumerTypeCard(
                analyzeResult
            )
        }

        item {
            SpendingRhythmCard()
        }

        item {
            TimeHeatMapCard()
        }

        item {
            InsightCards(
                transactions
            )
        }
    }
}

@Composable
fun PatternTopBar(

    selectedPeriod: String,

    selectedMonth: String,

    onPeriodChange: (String) -> Unit,

    onPrevMonth: () -> Unit,

    onNextMonth: () -> Unit

) {

    Row(

        modifier = Modifier
            .fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically

    ) {

        Row(

            modifier = Modifier
                .background(
                    Color(0xFFF4F4F4),
                    RoundedCornerShape(16.dp)
                )
                .padding(4.dp)

        ) {

            listOf(
                "주간",
                "월간",
                "연간"
            ).forEach {

                val selected =
                    selectedPeriod == it

                Box(

                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (selected)
                                Color.White
                            else
                                Color.Transparent
                        )
                        .clickable {

                            onPeriodChange(it)
                        }
                        .padding(
                            horizontal = 18.dp,
                            vertical = 10.dp
                        )

                ) {

                    Text(

                        text = it,

                        color =
                            if (selected)
                                Color(0xFF24A050)
                            else
                                Color.Gray,

                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }

        Row(

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            IconButton(
                onClick = onPrevMonth
            ) {

                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    null
                )
            }

            Text(

                text = selectedMonth,

                fontWeight =
                    FontWeight.Bold,

                fontSize = 18.sp
            )

            IconButton(
                onClick = onNextMonth
            ) {

                Icon(
                    Icons.Default.KeyboardArrowRight,
                    null
                )
            }
        }
    }
}

@Composable
private fun ConsumerTypeCard(
    result: AnalyzeResponse?
) {
    val consumerType =
        getConsumerType(
            result
        )

    Card(

        shape =
            RoundedCornerShape(
                24.dp
            )

    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(

                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    "나의 소비 패턴 유형"
                )

                Spacer(
                    Modifier.height(
                        12.dp
                    )
                )

                val consumerType =
                    getConsumerType(
                        result
                    )

                Text(
                    consumerType.first
                )

                Spacer(
                    Modifier.height(
                        8.dp
                    )
                )

                Text(
                    "규칙적인 소비 습관을 가지고 있어요."
                )

                Text(
                    "소비 계획을 잘 지키고 있어요!"
                )
            }

            Box(

                modifier =
                    Modifier.size(
                        120.dp
                    ),

                contentAlignment =
                    Alignment.Center

            ) {

                CircularProgressIndicator(

                    progress = {
                        consumerType.second / 100f
                    },

                    modifier =
                        Modifier.size(
                            120.dp
                        ),

                    strokeWidth =
                        10.dp,

                    color =
                        Color(
                            0xFF24A050
                        )
                )

                Text(
                    "${consumerType.second}점"
                )
            }
        }
    }
}

@Composable
private fun SpendingRhythmCard() {

    Card(

        shape =
            RoundedCornerShape(
                24.dp
            )

    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
        ) {

            Text(

                "소비 리듬",

                fontWeight =
                    FontWeight.Bold,

                fontSize = 20.sp
            )

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Text(
                "이번 달은 평소보다 주말 소비가 많았어요."
            )

            Spacer(
                Modifier.height(
                    24.dp
                )
            )

            Box(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            Color(
                                0xFFF8F8F8
                            ),
                            RoundedCornerShape(
                                12.dp
                            )
                        ),

                contentAlignment =
                    Alignment.Center

            ) {

                Text(
                    "그래프 영역"
                )
            }
        }
    }
}

@Composable
private fun TimeHeatMapCard() {

    Card(

        shape =
            RoundedCornerShape(
                24.dp
            )

    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp)

        ) {

            Text(

                "시간대별 소비 분포",

                fontWeight =
                    FontWeight.Bold,

                fontSize = 20.sp
            )

            Spacer(
                Modifier.height(
                    20.dp
                )
            )

            repeat(7) {

                Row {

                    repeat(16) {

                        Box(

                            modifier =
                                Modifier
                                    .padding(
                                        2.dp
                                    )
                                    .size(
                                        16.dp
                                    )
                                    .background(

                                        Color(
                                            0xFFCFF2D8
                                        ),

                                        RoundedCornerShape(
                                            4.dp
                                        )
                                    )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightCards(

    transactions: List<Transaction>

) {

    Column {

        Text(

            "소비 패턴 인사이트",

            fontWeight =
                FontWeight.Bold,

            fontSize = 20.sp
        )

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        Row(

            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            InsightCard(

                title =
                    "주말 소비 증가",

                desc =
                    "주말에 평균 ${
                        weekendRate(
                            transactions
                        )
                    }% 더 지출",

                badge =
                    "▲ 27%"
            )

            InsightCard(

                title =
                    "오후 소비 집중",

                desc =
                    "오후 2~7시에 집중",

                badge =
                    "${getPeakHour(
                        transactions
                    )}시"
            )

            InsightCard(

                title =
                    "월초 소비 경향",

                desc =
                    "1~5일 소비가 많음",

                badge =
                    "1~5일"
            )
        }
    }
}

@Composable
private fun InsightCard(

    title: String,

    desc: String,

    badge: String

) {

    Card(

        modifier =
            Modifier.width(
                170.dp
            ),

        shape =
            RoundedCornerShape(
                20.dp
            )

    ) {

        Column(

            modifier =
                Modifier.padding(
                    16.dp
                )

        ) {

            Box(

                modifier =
                    Modifier
                        .size(40.dp)
                        .background(

                            Color(
                                0xFFEAF7EE
                            ),

                            CircleShape
                        )
            )

            Spacer(
                Modifier.height(
                    12.dp
                )
            )

            Text(

                title,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Text(
                desc
            )

            Spacer(
                Modifier.height(
                    12.dp
                )
            )

            AssistChip(

                onClick = {},

                label = {

                    Text(
                        badge
                    )
                }
            )
        }
    }
}

fun weekendRate(
    transactions: List<Transaction>
): Int {

    var weekday = 0
    var weekend = 0

    transactions.forEach {

        val cal =
            Calendar.getInstance()

        cal.timeInMillis =
            it.time

        val day =

            cal.get(
                Calendar.DAY_OF_WEEK
            )

        val money =
            it.money.toIntOrNull()
                ?: 0

        if (

            day ==
            Calendar.SATURDAY ||

            day ==
            Calendar.SUNDAY

        ) {

            weekend += money

        } else {

            weekday += money
        }
    }

    if (weekday == 0)
        return 0

    return (

            (weekend.toFloat()
                    / weekday)

                    * 100

            ).toInt()
}

fun getPeakHour(
    transactions: List<Transaction>
): Int {

    val hours =
        IntArray(24)

    transactions.forEach {

        val cal =
            Calendar.getInstance()

        cal.timeInMillis =
            it.time

        val hour =

            cal.get(
                Calendar.HOUR_OF_DAY
            )

        hours[hour]++
    }

    return hours
        .indices

        .maxByOrNull {

            hours[it]

        } ?: 0
}

fun getEarlyMonthRate(
    transactions: List<Transaction>
): Int {

    val early =

        transactions.count {

            val cal =
                Calendar.getInstance()

            cal.timeInMillis =
                it.time

            cal.get(
                Calendar.DAY_OF_MONTH
            ) <= 5
        }

    if (transactions.isEmpty())
        return 0

    return (

            early * 100
                    / transactions.size
            )
}