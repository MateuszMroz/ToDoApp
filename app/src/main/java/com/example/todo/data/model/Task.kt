package com.example.todo.data.model

data class Task(
    val id: String,
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false
) {
    val titleForList: String
        get() = title.ifEmpty { description }
}
