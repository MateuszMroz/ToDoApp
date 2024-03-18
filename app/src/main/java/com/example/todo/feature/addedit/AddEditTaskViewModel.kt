package com.example.todo.feature.addedit

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
import com.example.todo.feature.navArgs
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
class AddEditTaskViewModel @Inject constructor(
    private val taskRepository: ITaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args: AddEditTaskArgs = savedStateHandle.navArgs()

    private val _uiState = MutableStateFlow(AddEditTaskUiState(isLoading = args.taskId != null))
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

    init {
        if (args.taskId != null) loadTask(args.taskId)
    }

    private fun loadTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.getTaskStream(taskId)
                .map { task -> if (task != null) Success(task) else Error(R.string.task_not_found) }
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
                    it.copy(
                        isLoading = false
                    )
                }

            is Error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = result.errorMessage
                    )
                }

            is Success ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        title = result.data.title,
                        description = result.data.description
                    )
                }
        }
    }

    fun saveTask() {
        viewModelScope.launch {
            if (_uiState.value.title.isBlank() || _uiState.value.description.isBlank()) {
                _uiState.update { it.copy(userMessage = R.string.empty_task_message) }
            } else {
                if (args.taskId != null) updateTask() else createTask()
            }
        }
    }

    private suspend fun createTask() = with(_uiState.value) {
        taskRepository.createTask(title, description)
        _uiState.update { it.copy(isTaskSaved = true) }
    }

    private suspend fun updateTask() = with(_uiState.value) {
        if (args.taskId == null) {
            throw RuntimeException("updateTask() was called but task is new.")
        }

        taskRepository.updateTask(args.taskId, title, description)
        _uiState.update { it.copy(isTaskSaved = true) }
    }

    fun onTitleChanged(title: String) {
        _uiState.update {
            it.copy(title = title)
        }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update {
            it.copy(description = description)
        }
    }

    fun snackbarMessageShown() {
        _uiState.update {
            it.copy(userMessage = null)
        }
    }
}
