package com.example.todo.ui.design

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.todo.R
import com.example.todo.ui.theme.ToDoTheme

@Composable
fun TaskTopBar(
    openDrawer: () -> Unit,
    onFilterAllTask: () -> Unit,
    onFilterActiveTask: () -> Unit,
    onFilterCompletedTask: () -> Unit,
    onClearCompletedTasks: () -> Unit,
    onRefresh: () -> Unit,
    onShowStatistic: () -> Unit
) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(id = R.string.open_drawer)
                )
            }
        },
        actions = {
            FilterTaskMenu(
                onFilterAllTask = onFilterAllTask,
                onFilterActiveTask = onFilterActiveTask,
                onFilterCompletedTask = onFilterCompletedTask
            )
            MoreTasksMenu(
                onClearCompletedTasks = onClearCompletedTasks,
                onRefresh = onRefresh,
                onShowStatisttic = onShowStatistic
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TaskBackTopBar(@StringRes title: Int, onBack: () -> Unit, actionIcon: ImageVector? = null, onActionIconClick: () -> Unit = {}) {
    TopAppBar(
        title = { Text(text = stringResource(id = title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.menu_back)
                )
            }
        },
        actions = {
            actionIcon?.let {
                IconButton(onClick = onActionIconClick) {
                    Icon(imageVector = it, contentDescription = null)
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun FilterTaskMenu(onFilterAllTask: () -> Unit, onFilterActiveTask: () -> Unit, onFilterCompletedTask: () -> Unit) {
    TopAppBarDropDownMenu(iconContent = {
        Icon(
            painterResource(id = R.drawable.ic_filter_list),
            stringResource(id = R.string.menu_filter)
        )
    }) { closeMenu ->
        DropdownMenuItem(onClick = {
            onFilterAllTask()
            closeMenu()
        }) {
            Text(text = stringResource(id = R.string.nav_all))
        }
        DropdownMenuItem(onClick = {
            onFilterActiveTask()
            closeMenu()
        }) {
            Text(text = stringResource(id = R.string.nav_active))
        }
        DropdownMenuItem(onClick = {
            onFilterCompletedTask()
            closeMenu()
        }) {
            Text(text = stringResource(id = R.string.nav_completed))
        }
    }
}

@Composable
fun MoreTasksMenu(onClearCompletedTasks: () -> Unit, onRefresh: () -> Unit, onShowStatisttic: () -> Unit) {
    TopAppBarDropDownMenu(iconContent = {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(id = R.string.menu_more)
        )
    }) { closeMenu ->
        DropdownMenuItem(onClick = {
            onClearCompletedTasks()
            closeMenu()
        }) {
            Text(text = stringResource(id = R.string.menu_clear))
        }
        DropdownMenuItem(onClick = {
            onRefresh()
            closeMenu()
        }) {
            Text(text = stringResource(id = R.string.refresh))
        }
        DropdownMenuItem(onClick = {
            onShowStatisttic()
            closeMenu()
        }) {
            Text(text = stringResource(id = R.string.statistics_title))
        }
    }
}

@Composable
fun TopAppBarDropDownMenu(iconContent: @Composable () -> Unit, content: @Composable (() -> Unit) -> Unit) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        IconButton(onClick = { expanded = !expanded }) {
            iconContent()
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
        ) {
            content {
                expanded = !expanded
            }
        }
    }
}

@Preview
@Composable
fun TaskTopBarPreview() {
    ToDoTheme {
        Surface {
            TaskTopBar(
                openDrawer = {},
                onFilterAllTask = {},
                onFilterActiveTask = {},
                onFilterCompletedTask = {},
                onClearCompletedTasks = {},
                onRefresh = {},
                onShowStatistic = {}
            )
        }
    }
}

@Preview
@Composable
fun TaskBackTopBarPreview() {
    ToDoTheme {
        Surface {
            TaskBackTopBar(
                title = R.string.add_task,
                onBack = {}
            )
        }
    }
}

@Preview
@Composable
fun TaskBackRightIconTopBarPreview() {
    ToDoTheme {
        Surface {
            TaskBackTopBar(
                title = R.string.task_details,
                onBack = {},
                actionIcon = Icons.Filled.Delete
            )
        }
    }
}
