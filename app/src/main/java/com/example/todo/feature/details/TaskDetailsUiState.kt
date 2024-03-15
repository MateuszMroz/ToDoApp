package com.example.todo.feature.details

import com.example.todo.data.model.Task

data class TaskDetailsUiState(
    val task: Task? = null,
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isTaskDeleted: Boolean = false
)
