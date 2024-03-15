package com.example.todo.feature.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.R
import com.example.todo.data.model.Task
import com.example.todo.data.repository.ITaskRepository
import com.example.todo.feature.statistic.StatisticUiState.Empty
import com.example.todo.feature.statistic.StatisticUiState.Error
import com.example.todo.feature.statistic.StatisticUiState.Loading
import com.example.todo.feature.statistic.StatisticUiState.Success
import com.example.todo.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class StatisticsViewModel
@Inject
constructor(
    private val taskRepository: ITaskRepository
) : ViewModel() {
    val uiState: StateFlow<StatisticUiState> =
        taskRepository.getTasksStream()
            .map { it.toStatistics() }
            .catch { emit(Error(R.string.loading_tasks_error)) }
            .stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = Loading
            )

    private fun List<Task>.toStatistics() = if (isNotEmpty()) {
        val statsResult = getActiveAndCompletedStats(this)
        Success(
            StatisticData(
                activeTasksPercent = statsResult.activeTasksPercent,
                completedTasksPercent = statsResult.completedTasksPercent
            )
        )
    } else {
        Empty
    }

    fun refresh() {
        viewModelScope.launch {
            taskRepository.refresh()
        }
    }

    /**Other methods to achieve the same result*/

//    private val _uiState: MutableStateFlow<StatisticUiState> = MutableStateFlow(Loading)
//    val uiState: StateFlow<StatisticUiState> = _uiState.asStateFlow()
//
//    init {
//        viewModelScope.launch {
//            taskRepository.getTasksStream()
//                .map { it.toStatistics() }
//                .catch { _uiState.value = Error(R.string.loading_tasks_error) }
//                .onStart { _uiState.value = Loading }
//                .collect {
//                    _uiState.value = it
//                }
//        }
//    }
}
