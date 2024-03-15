package com.example.todo.feature.tasks

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.ADD_RESULT_OK
import com.example.todo.DELETE_RESULT_OK
import com.example.todo.EDIT_RESULT_OK
import com.example.todo.R
import com.example.todo.core.AsyncResult
import com.example.todo.core.AsyncResult.Error
import com.example.todo.core.AsyncResult.Loading
import com.example.todo.core.AsyncResult.Success
import com.example.todo.data.model.Task
import com.example.todo.data.repository.ITaskRepository
import com.example.todo.feature.tasks.TasksFilterType.ACTIVE
import com.example.todo.feature.tasks.TasksFilterType.ALL
import com.example.todo.feature.tasks.TasksFilterType.COMPLETED
import com.example.todo.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val TASKS_FILTER_SAVED_STATE_KEY = "TASKS_FILTER_SAVED_STATE_KEY"

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: ITaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val savedFilterType = savedStateHandle.getStateFlow(
        TASKS_FILTER_SAVED_STATE_KEY,
        ALL
    )
    private val filterUiInfo = savedFilterType.map {
        getFilterUiInfo(
            it
        )
    }.distinctUntilChanged()
    private val isLoading = MutableStateFlow(false)
    private val userMessage = MutableStateFlow<Int?>(null)

    private val filteredTasksAsync =
        combine(
            taskRepository.getTasksStream(),
            savedFilterType
        ) { tasks, filter -> filterTasks(tasks, filter) }
            .map { Success(it) }
            .catch<AsyncResult<List<Task>>> { emit(Error(R.string.loading_tasks_error)) }

    val uiState: StateFlow<TasksUiState> =
        combine(
            filterUiInfo,
            isLoading,
            userMessage,
            filteredTasksAsync
        ) { filterUiInfo, isLoading, userMessage, tasks ->
            when (tasks) {
                Loading -> TasksUiState(isLoading = true)
                is Error -> TasksUiState(userMessage = tasks.errorMessage, isLoading = false)
                is Success ->
                    TasksUiState(
                        items = tasks.data,
                        empty = tasks.data.isEmpty(),
                        isLoading = isLoading,
                        filteringUiInfo = filterUiInfo,
                        userMessage = userMessage
                    )
            }
        }.stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = TasksUiState(isLoading = true)
        )

    fun setFiltering(filterType: TasksFilterType) {
        savedStateHandle[TASKS_FILTER_SAVED_STATE_KEY] = filterType
    }

    private fun filterTasks(tasks: List<Task>, filter: TasksFilterType): List<Task> {
        return when (filter) {
            ALL -> tasks
            ACTIVE -> tasks.filter { !it.isCompleted }
            COMPLETED -> tasks.filter { it.isCompleted }
        }
    }

    private fun getFilterUiInfo(filterType: TasksFilterType): FilteringUiInfo {
        return when (filterType) {
            ALL ->
                FilteringUiInfo(
                    R.string.label_all,
                    R.string.no_tasks_all,
                    R.drawable.logo_no_fill
                )

            ACTIVE ->
                FilteringUiInfo(
                    R.string.label_active,
                    R.string.no_tasks_active,
                    R.drawable.ic_check_circle_96dp
                )

            COMPLETED ->
                FilteringUiInfo(
                    R.string.label_completed,
                    R.string.no_tasks_completed,
                    R.drawable.ic_verified_user_96dp
                )
        }
    }

    fun snackbarMessageShown() {
        userMessage.value = null
    }

    fun showEditResultMessage(result: Int) {
        showSnackbarMessage(
            when (result) {
                ADD_RESULT_OK -> R.string.successfully_added_task_message
                EDIT_RESULT_OK -> R.string.successfully_saved_task_message
                DELETE_RESULT_OK -> R.string.successfully_deleted_task_message
                else -> return
            }
        )
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        userMessage.value = message
    }

    fun refresh() {
        isLoading.value = true
        viewModelScope.launch {
            taskRepository.refresh()
            isLoading.value = false
        }
    }

    fun completeTask(task: Task, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            taskRepository.completeTask(taskId = task.id)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            taskRepository.activateTask(taskId = task.id)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun clearCompletedTasks() = viewModelScope.launch {
        taskRepository.clearCompletedTasks()
        showSnackbarMessage(R.string.completed_tasks_cleared)
        refresh()
    }
}
