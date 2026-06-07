package com.example.compose

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.MaterialTheme

import com.example.compose.screens.HomeScreen
import com.example.compose.screens.RecordScreen
import com.example.compose.screens.AnalysisScreen

import com.example.compose.service.NotificationService
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.ListAlt

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier

import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

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

                MainPagerScreen()
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPagerScreen() {

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )

    val scope = rememberCoroutineScope()

    Scaffold(

        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            null
                        )
                    },
                    label = {
                        Text("홈")
                    }
                )

                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Outlined.ListAlt,
                            null
                        )
                    },
                    label = {
                        Text("기록")
                    }
                )

                NavigationBarItem(
                    selected = pagerState.currentPage == 2,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.ShowChart,
                            null
                        )
                    },
                    label = {
                        Text("분석")
                    }
                )
            }
        }

    ) { padding ->


        HorizontalPager(
            state = pagerState,

            modifier = Modifier
                .fillMaxSize()
        ) { page ->

            when (page) {

                0 -> HomeScreen(
                    currentScreen = Screen.HOME,
                    onScreenChange = { screen ->

                        when(screen) {

                            Screen.RECORD -> {

                                scope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            }

                            Screen.ANALYSIS -> {

                                scope.launch {
                                    pagerState.animateScrollToPage(2)
                                }
                            }

                            else -> {}
                        }
                    }
                )

                1 -> RecordScreen()

                2 -> AnalysisScreen()
            }
        }
    }
}
