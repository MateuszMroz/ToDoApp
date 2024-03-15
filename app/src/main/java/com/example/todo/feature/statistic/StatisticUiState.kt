package com.example.todo.feature.statistic

sealed interface StatisticUiState {
    data object Loading : StatisticUiState

    data object Empty : StatisticUiState

    data class Error(val errorMessage: Int) : StatisticUiState

    data class Success(val data: StatisticData) : StatisticUiState
}

data class StatisticData(
    val activeTasksPercent: Float,
    val completedTasksPercent: Float
)
