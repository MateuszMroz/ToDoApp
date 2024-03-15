package com.example.todo.feature.statistic

import com.example.todo.data.model.Task

data class StatsResult(val activeTasksPercent: Float, val completedTasksPercent: Float)

internal fun getActiveAndCompletedStats(tasks: List<Task>): StatsResult {
    return if (tasks.isEmpty()) {
        StatsResult(0f, 0f)
    } else {
        val totalTasks = tasks.size
        val numberOfActiveTasks = tasks.count { !it.isCompleted }
        StatsResult(
            activeTasksPercent = 100f * numberOfActiveTasks / tasks.size,
            completedTasksPercent = 100f * (totalTasks - numberOfActiveTasks) / tasks.size
        )
    }
}
