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

@Composable
fun AnalysisScreen() {

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

            1 -> AnalysisPatternTab()

            2 -> AnalysisCategoryTab()

            3 -> AnalysisCompareTab()
        }
    }
}