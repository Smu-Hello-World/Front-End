package com.example.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material3.Card
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnalysisSummaryTab() {

    LazyColumn(

        modifier = Modifier.padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        item {

            Card {

                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(
                        "이번 달 소비 점수"
                    )

                    Text(
                        "82",
                        fontSize = 48.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        "상위 28%"
                    )
                }
            }
        }
    }
}