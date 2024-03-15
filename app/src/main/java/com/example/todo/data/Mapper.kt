package com.example.todo.data

import com.example.todo.data.model.Task
import com.example.todo.data.source.local.TaskEntity

fun TaskEntity.toTask(): Task = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted
)

fun List<TaskEntity>.toTasks() = map { it.toTask() }

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted
)
