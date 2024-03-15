package com.example.todo.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task")
data class TaskEntity(
    @PrimaryKey var id: String,
    var title: String,
    var description: String,
    var isCompleted: Boolean
)
