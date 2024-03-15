package com.example.todo.statistic

import com.example.todo.data.model.Task
import com.example.todo.feature.statistic.getActiveAndCompletedStats
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StatisticUtilsTest {
    @Test
    fun `get statistic for tasks when all task are completed`() {
        // given
        val tasks =
            listOf(
                Task("1", "title", "desc", true),
                Task("2", "title", "desc", true)
            )

        // when
        val result = getActiveAndCompletedStats(tasks)

        // then
        assertThat(result.activeTasksPercent).isEqualTo(0f)
        assertThat(result.completedTasksPercent).isEqualTo(100f)
    }

    @Test
    fun `get statistic for tasks when all task are active`() {
        // given
        val tasks =
            listOf(
                Task("1", "title", "desc", false),
                Task("2", "title", "desc", false)
            )

        // when
        val result = getActiveAndCompletedStats(tasks)

        // then
        assertThat(result.activeTasksPercent).isEqualTo(100f)
        assertThat(result.completedTasksPercent).isEqualTo(0f)
    }

    @Test
    fun `get statistic for task when 40 percent are active and 60 percent are completed`() {
        // given
        val tasks =
            listOf(
                Task("1", "title", "desc", true),
                Task("2", "title", "desc", true),
                Task("3", "title", "desc", true),
                Task("4", "title", "desc", false),
                Task("5", "title", "desc", false)
            )

        // when
        val result = getActiveAndCompletedStats(tasks)

        // then
        assertThat(result.activeTasksPercent).isEqualTo(40f)
        assertThat(result.completedTasksPercent).isEqualTo(60f)
    }

    @Test
    fun `get statistic when no task is present`() {
        // given
        val tasks = emptyList<Task>()

        // when
        val result = getActiveAndCompletedStats(tasks)

        // then
        assertThat(result.activeTasksPercent).isEqualTo(0f)
        assertThat(result.completedTasksPercent).isEqualTo(0f)
    }
}
