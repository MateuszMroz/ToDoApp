package com.example.todo.tasks

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.todo.FakeTaskRepository
import com.example.todo.R
import com.example.todo.data.model.Task
import com.example.todo.feature.tasks.FilteringUiInfo
import com.example.todo.feature.tasks.TasksFilterType.ACTIVE
import com.example.todo.feature.tasks.TasksFilterType.ALL
import com.example.todo.feature.tasks.TasksFilterType.COMPLETED
import com.example.todo.feature.tasks.TasksUiState
import com.example.todo.feature.tasks.TasksViewModel
import com.example.todo.utils.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
Unit tests for the implementation of [TasksViewModel]
With FakeTaskRepository working more like a integration test
 */

class TasksViewModelTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val tasksViewModel: TasksViewModel by lazy {
        TasksViewModel(
            fakeRepository,
            SavedStateHandle()
        )
    }
    private lateinit var fakeRepository: FakeTaskRepository

    private val tasks =
        listOf(
            Task(id = "1", title = "Title1", description = "Desc1"),
            Task(id = "2", title = "Title2", description = "Desc2", isCompleted = true),
            Task(id = "3", title = "Title3", description = "Desc3", isCompleted = true)
        )

    @Before
    fun setUp() {
        // for test flow from db we have to test with fake repository(no mocking)
        fakeRepository = FakeTaskRepository()
        fakeRepository.addTasks(tasks)
    }

    @Test
    fun `load tasks when screen is opened`() = runTest {
        // given
        fakeRepository = FakeTaskRepository()
        fakeRepository.addTasks(tasks)

        // when/then
        tasksViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TasksUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TasksUiState(
                    isLoading = false,
                    items = tasks
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `load no data when no tasks`() = runTest {
        // given
        fakeRepository = FakeTaskRepository()

        // when/then
        tasksViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TasksUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TasksUiState(
                    isLoading = false,
                    empty = true,
                    items = emptyList()
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `show error message when tasks loading fails`() = runTest {
        // given
        fakeRepository = FakeTaskRepository()
        fakeRepository.setShouldThrowError(true)

        // when/then
        tasksViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TasksUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TasksUiState(
                    isLoading = false,
                    userMessage = R.string.loading_tasks_error
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `show ALL task when all tasks are filtered`() = runTest {
        /**Alternative way without turbine*/

//            // wait until all initial coroutines are done
//            advanceUntilIdle()
//
//            tasksViewModel.setFiltering(COMPLETED)
//            assertThat(tasksViewModel.uiState.first().isLoading).isTrue()
//
//            // wait until filtering is done
//            advanceUntilIdle()
//            assertThat(tasksViewModel.uiState.first().isLoading).isFalse()
//            assertThat(tasksViewModel.uiState.first().items).hasSize(2)

        // given
        fakeRepository = FakeTaskRepository()
        fakeRepository.addTasks(tasks)

        // when
        tasksViewModel.setFiltering(ALL)

        // then
        tasksViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TasksUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TasksUiState(
                    isLoading = false,
                    items = tasks
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `show COMPLETED task when completed tasks are filtered`() = runTest {
        // given
        fakeRepository = FakeTaskRepository()
        fakeRepository.addTasks(tasks)

        // when
        tasksViewModel.setFiltering(COMPLETED)

        // then
        tasksViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TasksUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TasksUiState(
                    isLoading = false,
                    filteringUiInfo =
                    FilteringUiInfo(
                        R.string.label_completed,
                        R.string.no_tasks_completed,
                        R.drawable.ic_verified_user_96dp
                    ),
                    items = tasks.filter { it.isCompleted }
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `show ACTIVE task when active tasks are filtered`() = runTest {
        // given
        fakeRepository = FakeTaskRepository()
        fakeRepository.addTasks(tasks)

        // when
        tasksViewModel.setFiltering(ACTIVE)

        // then
        tasksViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TasksUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TasksUiState(
                    isLoading = false,
                    filteringUiInfo =
                    FilteringUiInfo(
                        R.string.label_active,
                        R.string.no_tasks_active,
                        R.drawable.ic_check_circle_96dp
                    ),
                    items = tasks.filter { !it.isCompleted }
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `refresh tasks when is called`() = runTest {
        // given
        fakeRepository = FakeTaskRepository()
        fakeRepository.addTasks(tasks)

        // when
        tasksViewModel.refresh()

        // then
        tasksViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TasksUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TasksUiState(
                    isLoading = false,
                    items = tasks
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `clear completed tasks when is called`() = runTest {
        // given
        fakeRepository = FakeTaskRepository()
        fakeRepository.addTasks(tasks)

        // when
        tasksViewModel.clearCompletedTasks()

        // then
        tasksViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TasksUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TasksUiState(
                    isLoading = false,
                    userMessage = R.string.completed_tasks_cleared,
                    items = tasks.filter { !it.isCompleted }
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `complete task when is called`() = runTest {
        // given
        fakeRepository = FakeTaskRepository()
        fakeRepository.addTasks(tasks)

        // when
        tasksViewModel.completeTask(tasks[0], true)

        // then
        tasksViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TasksUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TasksUiState(
                    isLoading = false,
                    userMessage = R.string.task_marked_complete,
                    items =
                    tasks.map {
                        if (it.id == tasks[0].id) {
                            it.copy(isCompleted = true)
                        } else {
                            it
                        }
                    }
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `activate task when is called`() = runTest {
        // given
        fakeRepository = FakeTaskRepository()
        fakeRepository.addTasks(tasks)

        // when
        tasksViewModel.completeTask(tasks[1], false)

        // then
        tasksViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TasksUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TasksUiState(
                    isLoading = false,
                    userMessage = R.string.task_marked_active,
                    items =
                    tasks.map {
                        if (it.id == tasks[1].id) {
                            it.copy(isCompleted = false)
                        } else {
                            it
                        }
                    }
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `snackbar message shown when is called`() = runTest {
        // given
        fakeRepository = FakeTaskRepository()
        fakeRepository.addTasks(tasks)

        // when
        tasksViewModel.snackbarMessageShown()

        // then
        tasksViewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(TasksUiState(isLoading = true))
            assertThat(awaitItem()).isEqualTo(
                TasksUiState(
                    isLoading = false,
                    userMessage = null,
                    items = tasks
                )
            )
            cancelAndConsumeRemainingEvents()
        }
    }
}
