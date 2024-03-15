package com.example.todo.feature.statistic.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.todo.feature.statistic.StatisticRoute

const val STATISTIC_SCREEN = "statistic"
const val STATISTIC_ROUTE = STATISTIC_SCREEN

fun NavController.navigateToStatistic() {
    val route = STATISTIC_SCREEN

    navigate(route) {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.statisticsScreen(onBack: () -> Unit) {
    composable(route = STATISTIC_ROUTE) {
        StatisticRoute(onBack = onBack)
    }
}
