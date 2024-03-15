package com.example.todo.feature.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.R
import com.example.todo.core.AsyncResult
import com.example.todo.core.AsyncResult.Error
import com.example.todo.core.AsyncResult.Loading
import com.example.todo.core.AsyncResult.Success
import com.example.todo.data.model.Task
import com.example.todo.data.repository.ITaskRepository
import com.example.todo.feature.details.navigation.TASK_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val taskRepository: ITaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val taskId: String =
        requireNotNull(savedStateHandle[TASK_ID_ARG]) // check if possible to be private

    private val _uiState = MutableStateFlow(TaskDetailsUiState(isLoading = true))
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()

    init {
        loadTask(taskId)
    }

    private fun loadTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.getTaskStream(taskId)
                .map { task ->
                    if (task != null) {
                        Success(
                            task
                        )
                    } else {
                        Error(R.string.task_not_found)
                    }
                }
                .catch {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = R.string.loading_task_error
                        )
                    }
                }
                .collect { handleResult(it) }
        }
    }

    private fun handleResult(result: AsyncResult<Task>) {
        when (result) {
            Loading ->
                _uiState.update {
                    it.copy(isLoading = true)
                }

            is Error ->
                _uiState.update {
                    it.copy(
                        userMessage = result.errorMessage,
                        isLoading = false
                    )
                }

            is Success ->
                _uiState.update {
                    it.copy(
                        task = result.data,
                        isLoading = false
                    )
                }
        }
    }

    fun snackbarMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun deleteTask() {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
            _uiState.update { it.copy(isTaskDeleted = true) }
        }
    }

    fun refresh() = viewModelScope.launch {
        taskRepository.refreshTask(taskId)
    }

    fun onTaskChecked(checked: Boolean) = viewModelScope.launch {
        if (checked) {
            taskRepository.completeTask(taskId)
        } else {
            taskRepository.activateTask(taskId)
        }

        _uiState.update {
            it.copy(
                userMessage = if (checked) R.string.task_marked_complete else R.string.task_marked_active
            )
        }
    }

    /**
     Second way how achieve that. Alternative to the current option.
     */

    //    private val _userMessage = MutableStateFlow<Int?>(null)
    //    private val _isTaskDeleted = MutableStateFlow(false)
    //    private val _taskAsync = taskRepository.getTaskStream(taskId)
    //        .map { task ->
    //            if (task == null) Error(R.string.task_not_found)
    //            else Success(task)
    //        }
    //        .catch { emit(Error(errorMessage = R.string.loading_task_error)) }
    //
    //    val uiState: StateFlow<TaskDetailsUiState> =
    //        combine(_userMessage, _isTaskDeleted, _taskAsync) { userMessage, isTaskDeleted, taskAsync ->
    //            when (taskAsync) {
    //                Loading -> TaskDetailsUiState(isLoading = true)
    //                is Error -> TaskDetailsUiState(
    //                    userMessage = taskAsync.errorMessage,
    //                    isTaskDeleted = isTaskDeleted,
    //                    isLoading = false
    //                )
    //
    //                is Success -> TaskDetailsUiState(
    //                    task = taskAsync.data,
    //                    userMessage = userMessage,
    //                    isTaskDeleted = isTaskDeleted,
    //                    isLoading = false
    //                )
    //            }
    //        }.stateIn(
    //            scope = viewModelScope,
    //            started = WhileUiSubscribed,
    //            initialValue = TaskDetailsUiState()
    //        )
    //
    //    fun snackbarMessageShown() {
    //        _userMessage.update { null }
    //    }
    //
    //    fun deleteTask() {
    //        viewModelScope.launch {
    //            taskRepository.deleteTask(taskId)
    //            _isTaskDeleted.update { true }
    //        }
    //    }
    //
    //    fun refresh() = viewModelScope.launch {
    //        taskRepository.refreshTask(taskId)
    //    }
    //
    //    fun onTaskChecked(checked: Boolean) = viewModelScope.launch {
    //        if (checked) taskRepository.completeTask(taskId)
    //        else taskRepository.activateTask(taskId)
    //
    //        _userMessage.update { if (checked) R.string.task_marked_complete else R.string.task_marked_active }
    //    }
}
