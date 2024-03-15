package com.example.todo.addedit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.todo.R
import com.example.todo.data.model.Task
import com.example.todo.data.repository.ITaskRepository
import com.example.todo.feature.addedit.AddEditTaskUiState
import com.example.todo.feature.addedit.AddEditTaskViewModel
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

class AddEditTasksViewModelTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val taskRepository: ITaskRepository = mockk(relaxed = true)
    private val addEditTaskViewModel: AddEditTaskViewModel by lazy {
        AddEditTaskViewModel(taskRepository, SavedStateHandle(mapOf(TASK_ID_ARG to "0")))
    }

    @Test
    fun `load task details when task id in not null`() = runTest {
        // given
        val task = Task("0", "title", "desc", false)
        every { taskRepository.getTaskStream("0") } returns flowOf(task)

        // when/then
        addEditTaskViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(AddEditTaskUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                AddEditTaskUiState(
                    isLoading = false,
                    title = "title",
                    description = "desc"
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `show error when task not exist`() = runTest {
        // given
        every { taskRepository.getTaskStream("0") } returns flowOf(null)

        // when/then
        addEditTaskViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(AddEditTaskUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                AddEditTaskUiState(
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
        every { taskRepository.getTaskStream("0") } returns flow { throw IllegalStateException() }

        // when/then
        addEditTaskViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(AddEditTaskUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                AddEditTaskUiState(
                    isLoading = false,
                    userMessage = R.string.loading_task_error
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `no load data when task id is null`() = runTest {
        // given
        val viewModel = AddEditTaskViewModel(taskRepository, SavedStateHandle())

        // when/then
        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(AddEditTaskUiState(isLoading = false))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `create task when task id is null`() = runTest {
        // given
        val viewModel = AddEditTaskViewModel(taskRepository, SavedStateHandle())
        viewModel.onTitleChanged("title")
        viewModel.onDescriptionChanged("desc")

        // when
        viewModel.saveTask()
        advanceUntilIdle()

        // then
        coVerify { taskRepository.createTask("title", "desc") }
        assertThat(viewModel.uiState.value.isTaskSaved).isTrue()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `update task when task id is not null`() = runTest {
        // given
        addEditTaskViewModel.onTitleChanged("title")
        addEditTaskViewModel.onDescriptionChanged("desc")

        // when
        addEditTaskViewModel.saveTask()
        advanceUntilIdle()

        // then
        coVerify { taskRepository.updateTask("0", "title", "desc") }
        assertThat(addEditTaskViewModel.uiState.value.isTaskSaved).isTrue()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `show error message when title is empty`() = runTest {
        // given
        addEditTaskViewModel.onTitleChanged("")

        // when
        addEditTaskViewModel.saveTask()
        advanceUntilIdle()

        // then
        assertThat(
            addEditTaskViewModel.uiState.value.userMessage
        ).isEqualTo(R.string.empty_task_message)
    }

    @Test
    fun `clear user message when snackbar message shown`() = runTest {
        // given/when
        addEditTaskViewModel.snackbarMessageShown()

        // then
        assertThat(addEditTaskViewModel.uiState.value.userMessage).isNull()
    }

    @Test
    fun `update title when onTitleChanged is called`() = runTest {
        // given/when
        addEditTaskViewModel.onTitleChanged("title")

        // then
        assertThat(addEditTaskViewModel.uiState.value.title).isEqualTo("title")
    }

    @Test
    fun `update description when onDescriptionChanged is called`() = runTest {
        // given/when
        addEditTaskViewModel.onDescriptionChanged("desc")

        // then
        assertThat(addEditTaskViewModel.uiState.value.description).isEqualTo("desc")
    }
}
