package com.example.compose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

import com.example.compose.utils.JsonManager

import com.example.compose.network.AnalyzeRequest
import com.example.compose.network.AnalyzeResponse
import com.example.compose.network.RetrofitClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun AnalysisScreen() {

    var analyzeResult by remember {
        mutableStateOf<AnalyzeResponse?>(null)
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

        if (json == "데이터 없음")
            return@LaunchedEffect

        transactions =
            loadTransactions(json)

        val requestList =

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

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    val tabs = listOf(
        "요약",
        "소비패턴",
        "카테고리",
        "비교"
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        AnalysisHeader()

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            containerColor = Color.White,
            contentColor = Color(0xFF22C55E),
            indicator = { tabPositions ->

                TabRowDefaults.SecondaryIndicator(
                    modifier =
                        Modifier.tabIndicatorOffset(
                            tabPositions[selectedTab]
                        ),
                    color = Color(0xFF22C55E)
                )
            }
        ) {

            tabs.forEachIndexed { index, title ->

                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                    },
                    text = {
                        Text(title)
                    }
                )
            }
        }

        when (selectedTab) {

            0 -> AnalysisSummaryTab()

            1 -> AnalysisPatternTab(
                transactions = transactions,
                analyzeResult = analyzeResult
            )

            2 -> AnalysisCategoryTab()

            3 -> AnalysisCompareTab()
        }
    }
}