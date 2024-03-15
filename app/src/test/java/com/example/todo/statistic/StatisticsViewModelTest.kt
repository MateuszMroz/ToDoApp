package com.example.todo.statistic

import app.cash.turbine.test
import com.example.todo.R
import com.example.todo.data.model.Task
import com.example.todo.data.repository.ITaskRepository
import com.example.todo.feature.statistic.StatisticData
import com.example.todo.feature.statistic.StatisticUiState
import com.example.todo.feature.statistic.StatisticUiState.Empty
import com.example.todo.feature.statistic.StatisticUiState.Error
import com.example.todo.feature.statistic.StatisticUiState.Loading
import com.example.todo.feature.statistic.StatisticsViewModel
import com.example.todo.utils.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class StatisticsViewModelTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val taskRepository: ITaskRepository = mockk(relaxed = true)
    private val statisticsViewModel: StatisticsViewModel by lazy {
        StatisticsViewModel(taskRepository)
    }

    @Test
    fun `load Empty state when no tasks`() = runTest {
        // given
        every { taskRepository.getTasksStream() } returns flowOf(emptyList())

        // when/then
        statisticsViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(Loading)
            assertThat(awaitItem()).isEqualTo(Empty)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `load Success state when tasks exist`() = runTest {
        // given
        val tasks =
            listOf(
                Task("1", "title", "desc", false),
                Task("2", "title", "desc", true)
            )
        every { taskRepository.getTasksStream() } returns flowOf(tasks)

        // when/then
        statisticsViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(Loading)
            assertThat(awaitItem()).isEqualTo(
                StatisticUiState.Success(
                    StatisticData(
                        activeTasksPercent = 50f,
                        completedTasksPercent = 50f
                    )
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `load Error state when error occur`() = runTest {
        // given
        every { taskRepository.getTasksStream() } returns flow { throw IllegalStateException() }

        // when/then
        statisticsViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(Loading)
            assertThat(awaitItem()).isEqualTo(Error(R.string.loading_tasks_error))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `refresh tasks when is called`() = runTest {
        // given/when
        statisticsViewModel.refresh()
        advanceUntilIdle()

        // then
        coVerify { taskRepository.refresh() }
    }
}
