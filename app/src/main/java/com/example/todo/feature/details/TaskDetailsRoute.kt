package com.example.todo.feature.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todo.R
import com.example.todo.data.model.Task
import com.example.todo.ui.design.RefreshIndicator
import com.example.todo.ui.design.TaskBackTopBar
import com.example.todo.ui.theme.ToDoTheme

@Composable
fun TaskDetailsRoute(
    onEditTask: (String) -> Unit,
    onDeleteTask: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailsViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = {
            TaskBackTopBar(
                title = R.string.task_details,
                onBack = onBack,
                actionIcon = Filled.Delete,
                onActionIconClick = viewModel::deleteTask
            )
        },
        floatingActionButton = {
            if (uiState.task != null) {
                FloatingActionButton(onClick = { onEditTask(viewModel.taskId) }) {
                    Icon(Filled.Edit, stringResource(id = R.string.edit_task))
                }
            }
        }
    ) { paddingValues ->

        TaskDetailsScreen(
            loading = uiState.isLoading,
            empty = uiState.task == null && !uiState.isLoading,
            task = uiState.task,
            onRefresh = viewModel::refresh,
            onTaskChecked = viewModel::onTaskChecked,
            modifier = modifier.padding(paddingValues)
        )

        uiState.userMessage?.let {
            val snackbarText = stringResource(id = it)
            LaunchedEffect(scaffoldState, viewModel, it, snackbarText) {
                scaffoldState.snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        LaunchedEffect(uiState.isTaskDeleted) {
            if (uiState.isTaskDeleted) {
                onDeleteTask()
            }
        }
    }
}

@Composable
private fun TaskDetailsScreen(
    loading: Boolean,
    empty: Boolean,
    task: Task?,
    onRefresh: () -> Unit,
    onTaskChecked: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    RefreshIndicator(
        isRefreshing = loading,
        empty = empty,
        emptyContent = {
            Text(
                text = stringResource(id = R.string.no_data),
                style =
                TextStyle(
                    color = Color.Red,
                    fontSize = 20.sp,
                    fontWeight = Bold
                ),
                modifier =
                modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            )
        },
        onRefresh = onRefresh
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task?.isCompleted ?: false,
                onCheckedChange = { onTaskChecked(it) },
                modifier = Modifier.padding(start = 8.dp)
            )
            Column(
                modifier =
                modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if (task != null) {
                    Text(
                        text = task.title,
                        style =
                        TextStyle(
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = Bold
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = task.description,
                        style =
                        TextStyle(
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                        // modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun TaskDetailsContentPrev() {
    ToDoTheme {
        Surface {
            TaskDetailsScreen(
                loading = false,
                empty = false,
                task = Task("1", "Title", "Description", false),
                onRefresh = {},
                onTaskChecked = {}
            )
        }
    }
}

@Preview
@Composable
private fun TaskDetailsContentEmptyPrev() {
    ToDoTheme {
        Surface {
            TaskDetailsScreen(
                loading = false,
                empty = true,
                task = null,
                onRefresh = {},
                onTaskChecked = {}
            )
        }
    }
}
