package com.example.todo.feature.tasks

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.todo.R
import com.example.todo.data.model.Task

data class TasksUiState(
    val items: List<Task> = emptyList(),
    val empty: Boolean = false,
    val isLoading: Boolean = false,
    val filteringUiInfo: FilteringUiInfo = FilteringUiInfo(),
    @StringRes val userMessage: Int? = null
)

data class FilteringUiInfo(
    @StringRes val currentFilteringLabel: Int = R.string.label_all,
    @StringRes val noTaskLabel: Int = R.string.no_tasks_all,
    @DrawableRes val noTaskIconRes: Int = R.drawable.logo_no_fill
)
