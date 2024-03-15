package com.example.todo.feature.addedit

data class AddEditTaskUiState(
    var title: String = "",
    var description: String = "",
    var isLoading: Boolean = false,
    var userMessage: Int? = null,
    var isTaskSaved: Boolean = false
)
