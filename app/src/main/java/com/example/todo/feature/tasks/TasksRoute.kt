package com.example.todo.feature.tasks

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todo.R
import com.example.todo.data.model.Task
import com.example.todo.feature.tasks.TasksFilterType.ACTIVE
import com.example.todo.feature.tasks.TasksFilterType.ALL
import com.example.todo.feature.tasks.TasksFilterType.COMPLETED
import com.example.todo.ui.design.RefreshIndicator
import com.example.todo.ui.design.TaskTopBar
import com.example.todo.ui.theme.ToDoTheme

@Composable
fun TasksRoute(
    @StringRes userMessage: Int,
    onAddTask: () -> Unit,
    onTaskClick: (Task) -> Unit,
    openDrawer: () -> Unit,
    onShowStatistic: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = {
            TaskTopBar(
                openDrawer = openDrawer,
                onFilterAllTask = { viewModel.setFiltering(ALL) },
                onFilterActiveTask = { viewModel.setFiltering(ACTIVE) },
                onFilterCompletedTask = { viewModel.setFiltering(COMPLETED) },
                onClearCompletedTasks = { viewModel.clearCompletedTasks() },
                onRefresh = { viewModel.refresh() },
                onShowStatistic = onShowStatistic
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(id = R.string.add_task)
                )
            }
        }
    ) { paddingValues ->

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        TasksScreen(
            loading = uiState.isLoading,
            tasks = uiState.items,
            empty = uiState.empty,
            currentFilteringLabel = uiState.filteringUiInfo.currentFilteringLabel,
            noTasksLabel = uiState.filteringUiInfo.noTaskLabel,
            noTasksIconRes = uiState.filteringUiInfo.noTaskIconRes,
            onRefresh = { viewModel.refresh() },
            onTaskClick = onTaskClick,
            onTaskCheckedChange = viewModel::completeTask,
            modifier = Modifier.padding(paddingValues)
        )

        uiState.userMessage?.let {
            val snackbarText = stringResource(id = it)
            LaunchedEffect(scaffoldState, viewModel, it, snackbarText) {
                scaffoldState.snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        LaunchedEffect(userMessage) {
            if (userMessage != 0) {
                viewModel.showEditResultMessage(userMessage)
            }
        }
    }
}

@Composable
private fun TasksScreen(
    loading: Boolean,
    tasks: List<Task>,
    empty: Boolean,
    @StringRes currentFilteringLabel: Int,
    @StringRes noTasksLabel: Int,
    @DrawableRes noTasksIconRes: Int,
    onRefresh: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    RefreshIndicator(
        isRefreshing = loading,
        empty = empty,
        emptyContent = { TaskEmptyContent(label = noTasksLabel, icon = noTasksIconRes, modifier) },
        onRefresh = onRefresh
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.horizontal_margin),
                    vertical = dimensionResource(id = R.dimen.vertical_margin)
                )
        ) {
            Text(
                text = stringResource(id = currentFilteringLabel),
                style = MaterialTheme.typography.h6,
                modifier =
                Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.horizontal_margin),
                    vertical = dimensionResource(id = R.dimen.vertical_margin)
                )
            )
            LazyColumn {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onTaskClick = { onTaskClick(task) },
                        onTaskCheckedChange = { onTaskCheckedChange(task, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItem(task: Task, onTaskClick: (Task) -> Unit, onTaskCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.horizontal_margin),
                vertical =
                dimensionResource(
                    id = R.dimen.list_item_padding
                )
            )
            .clickable { onTaskClick(task) }
    ) {
        Checkbox(checked = task.isCompleted, onCheckedChange = onTaskCheckedChange)
        Text(
            text = task.titleForList,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(
                horizontal = dimensionResource(id = R.dimen.horizontal_margin)
            ),
            textDecoration =
            if (task.isCompleted) {
                TextDecoration.LineThrough
            } else {
                null
            }
        )
    }
}

@Composable
private fun TaskEmptyContent(@StringRes label: Int, @DrawableRes icon: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = label),
            modifier = modifier.size(96.dp)
        )
        Text(text = stringResource(id = label))
    }
}

@Preview
@Composable
private fun TaskItemPreview() {
    ToDoTheme {
        Surface {
            TaskItem(
                task =
                Task(
                    title = "Title 1",
                    description = "Description 1",
                    isCompleted = false,
                    id = "ID 1"
                ),
                onTaskClick = {},
                onTaskCheckedChange = {}
            )
        }
    }
}

@Preview
@Composable
private fun TaskItemCompletedPreview() {
    ToDoTheme {
        Surface {
            TaskItem(
                task =
                Task(
                    title = "Title 2",
                    description = "Description 2",
                    isCompleted = true,
                    id = "ID 2"
                ),
                onTaskClick = {},
                onTaskCheckedChange = {}
            )
        }
    }
}

@Preview
@Composable
private fun TaskEmptyContentPreview() {
    ToDoTheme {
        Surface {
            TaskEmptyContent(
                label = R.string.no_tasks_all,
                icon = R.drawable.logo_no_fill
            )
        }
    }
}

@Preview
@Composable
private fun TaskContentPreview() {
    ToDoTheme {
        Surface {
            TasksScreen(
                loading = false,
                tasks =
                listOf(
                    Task(
                        title = "Title 1",
                        description = "Description 1",
                        isCompleted = false,
                        id = "ID 1"
                    ),
                    Task(
                        title = "Title 2",
                        description = "Description 2",
                        isCompleted = true,
                        id = "ID 2"
                    ),
                    Task(
                        title = "Title 3",
                        description = "Description 3",
                        isCompleted = true,
                        id = "ID 3"
                    ),
                    Task(
                        title = "Title 4",
                        description = "Description 4",
                        isCompleted = false,
                        id = "ID 4"
                    ),
                    Task(
                        title = "Title 5",
                        description = "Description 5",
                        isCompleted = true,
                        id = "ID 5"
                    )
                ),
                empty = false,
                currentFilteringLabel = R.string.label_all,
                noTasksLabel = 0,
                noTasksIconRes = 0,
                onRefresh = {},
                onTaskClick = {},
                onTaskCheckedChange = { _, _ -> }
            )
        }
    }
}
