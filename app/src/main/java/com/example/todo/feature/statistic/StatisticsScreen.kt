package com.example.todo.feature.statistic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todo.R
import com.example.todo.feature.ToDoNavGraph
import com.example.todo.ui.design.RefreshIndicator
import com.example.todo.ui.design.TaskBackTopBar
import com.example.todo.ui.theme.ToDoTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@ToDoNavGraph
@Destination
@Composable
fun StatisticRoute(
    navigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TaskBackTopBar(
                onBack = { navigator.popBackStack() },
                title = R.string.statistics_title
            )
        }
    ) { paddingValues ->

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        StatisticRoute(
            uiState = uiState,
            onRefresh = viewModel::refresh,
            modifier = modifier.padding(paddingValues)
        )
    }
}

@Composable
fun StatisticRoute(uiState: StatisticUiState, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    RefreshIndicator(
        isRefreshing = uiState is StatisticUiState.Loading,
        empty = uiState is StatisticUiState.Empty,
        emptyContent = {
            Text(
                text = stringResource(id = R.string.statistics_no_tasks),
                modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        },
        onRefresh = onRefresh
    ) {
    }

    Column(
        modifier =
        modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        (uiState as? StatisticUiState.Success)?.let {
            Text(
                text =
                stringResource(
                    id = R.string.statistics_active_tasks,
                    it.data.activeTasksPercent
                ),
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text =
                stringResource(
                    id = R.string.statistics_completed_tasks,
                    it.data.completedTasksPercent
                ),
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Preview
@Composable
private fun StatisticContentPreview() {
    ToDoTheme {
        Surface {
            StatisticRoute(
                uiState =
                StatisticUiState.Success(
                    StatisticData(
                        activeTasksPercent = 60f,
                        completedTasksPercent = 40f
                    )
                ),
                onRefresh = {}
            )
        }
    }
}
