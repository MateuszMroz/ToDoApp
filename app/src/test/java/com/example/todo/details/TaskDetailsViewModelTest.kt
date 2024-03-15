package com.example.todo.details

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.todo.R
import com.example.todo.data.model.Task
import com.example.todo.data.repository.ITaskRepository
import com.example.todo.feature.details.TaskDetailsUiState
import com.example.todo.feature.details.TaskDetailsViewModel
import com.example.todo.feature.details.navigation.TASK_ID_ARG
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

class TaskDetailsViewModelTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val taskRepository: ITaskRepository = mockk(relaxed = true)
    private val statisticsViewModel: TaskDetailsViewModel by lazy {
        TaskDetailsViewModel(taskRepository, SavedStateHandle(mapOf(TASK_ID_ARG to "0")))
    }

    @Test
    fun `load task details when task exists`() = runTest {
        // given
        val task = Task("0", "title", "desc", false)
        every { taskRepository.getTaskStream("0") } returns flowOf(task)

        // when/then
        statisticsViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TaskDetailsUiState(isLoading = true))
            assertThat(
                awaitItem()
            ).isEqualTo(TaskDetailsUiState(isLoading = false, task = task))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `load no data when no tasks`() = runTest {
        // given
        every { taskRepository.getTaskStream("0") } returns flowOf(null)

        // when/then
        statisticsViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TaskDetailsUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TaskDetailsUiState(
                    isLoading = false,
                    userMessage = R.string.task_not_found
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `show error message when task loading fails`() = runTest {
        // given
        every { taskRepository.getTaskStream("0") } returns
            flow {
                throw RuntimeException("Something went wrong")
            }

        // when/then
        statisticsViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TaskDetailsUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TaskDetailsUiState(
                    isLoading = false,
                    userMessage = R.string.loading_task_error
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `delete task when task exists`() = runTest {
        // given
        val task = Task("0", "title", "desc", false)
        every { taskRepository.getTaskStream("0") } returns flowOf(task)

        // when
        statisticsViewModel.deleteTask()
        advanceUntilIdle()

        // then
        coVerify { taskRepository.deleteTask("0") }
        assertThat(statisticsViewModel.uiState.value.isTaskDeleted).isTrue()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `mark complete task when task is active`() = runTest {
        // given
        val task = Task("0", "title", "desc", false)
        every { taskRepository.getTaskStream("0") } returns flowOf(task)

        // when
        statisticsViewModel.onTaskChecked(true)
        advanceUntilIdle()

        // then
        coVerify { taskRepository.completeTask("0") }
        assertThat(
            statisticsViewModel.uiState.value.userMessage
        ).isEqualTo(R.string.task_marked_complete)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `mark active task when task is completed`() = runTest {
        // given
        val task = Task("0", "title", "desc", true)
        every { taskRepository.getTaskStream("0") } returns flowOf(task)

        // when
        statisticsViewModel.onTaskChecked(false)
        advanceUntilIdle()

        // then
        coVerify { taskRepository.activateTask("0") }
        assertThat(
            statisticsViewModel.uiState.value.userMessage
        ).isEqualTo(R.string.task_marked_active)
    }
}
