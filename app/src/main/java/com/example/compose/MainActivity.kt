package com.example.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Bookmark
import com.example.compose.R
import com.example.compose.ui.theme.Pretendard
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import com.example.compose.service.NotificationService
import androidx.compose.runtime.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.runtime.*

import com.example.compose.network.AnalyzeRequest
import com.example.compose.network.AnalyzeResponse
import com.example.compose.network.RetrofitClient

import com.example.compose.network.*
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray

import com.example.compose.utils.JsonManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (!isNotificationServiceEnabled()) {

            startActivity(
                Intent(
                    Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                )
            )
        }

        setContent {

            MaterialTheme {

                HomeScreen()
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {

        val enabledListeners =
            Settings.Secure.getString(
                contentResolver,
                "enabled_notification_listeners"
            ) ?: return false

        val componentName =
            ComponentName(
                this,
                NotificationService::class.java
            )

        return enabledListeners.contains(
            componentName.flattenToString()
        )
    }
}

@Composable
fun HomeScreen() {
    var incomeMoney by remember {
        mutableIntStateOf(0)
    }

    var outcomeMoney by remember {
        mutableIntStateOf(0)
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
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("홈") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.ListAlt, contentDescription = null) },
                    label = { Text("기록") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
                    label = { Text("분석") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    label = { Text("알림") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                    label = { Text("마이") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 18.dp)
        ) {
            item {
                TopBar()
                Spacer(modifier = Modifier.height(18.dp))
                HeroSection()
                SummaryCard(
                    income = incomeMoney,
                    outcome = outcomeMoney
                )
                Spacer(modifier = Modifier.height(16.dp))
                TipCard()
                Spacer(modifier = Modifier.height(16.dp))
                CategoryHeader()
                Spacer(modifier = Modifier.height(10.dp))
                TopCategoryRow()
                Spacer(modifier = Modifier.height(12.dp))
                ResultCard()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = null,
            tint = Color(0xFF111111),
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            contentAlignment = Alignment.TopEnd
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFF111111),
                modifier = Modifier.size(28.dp)
            )

            Box(
                modifier = Modifier
                    .offset(x = (-1).dp, y = 2.dp)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
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
                    text = "커피 한 잔 대신 텀블러를 챙겨보세요 ☕",
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
    outcome: Int
) {
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
                        text = "432,000원",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Pretendard,
                        color = Color(0xFF111111)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "예산 550,000원",
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
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "전체 보기 >",
            fontSize = 11.sp,
            fontFamily = Pretendard,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun TopCategoryRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SmallCategoryCard(
            modifier = Modifier.weight(1f),
            bg = Color(0xFFEEF8EA),
            icon = "🍴",
            title = "식비",
            amount = "172,000원",
            percent = "39%"
        )
        SmallCategoryCard(
            modifier = Modifier.weight(1f),
            bg = Color(0xFFFFF4E5),
            icon = "🚕",
            title = "교통",
            amount = "78,000원",
            percent = "18%"
        )
        SmallCategoryCard(
            modifier = Modifier.weight(1f),
            bg = Color(0xFFF3EEFF),
            icon = "👜",
            title = "쇼핑",
            amount = "65,000원",
            percent = "15%"
        )
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

@Composable
private fun ResultCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F7EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "☁", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "괜찮아요!",
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF35A65A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "예산 범위 내에서 소비하고 있어요.",
                    fontFamily = Pretendard,
                    fontSize = 12.sp,
                    color = Color(0xFF444444)
                )
            }

            Text(
                text = ">",
                fontFamily = Pretendard,
                fontSize = 18.sp,
                color = Color(0xFF35A65A)
            )
        }
    }
}
