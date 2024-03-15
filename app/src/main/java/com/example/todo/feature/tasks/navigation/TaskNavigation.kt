package com.example.todo.feature.tasks.navigation

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.todo.data.model.Task
import com.example.todo.feature.tasks.TasksRoute

const val TASKS_SCREEN = "tasks"
const val USER_MESSAGE_ARG = "user_message"
const val TASK_ROUTE = "${TASKS_SCREEN}?${USER_MESSAGE_ARG}={${USER_MESSAGE_ARG}}"

fun NavController.navigateToTasks(userMessage: Int? = 0) {
    val route = TASKS_SCREEN.let { if (userMessage != 0) "$it?$USER_MESSAGE_ARG" else it }

    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = false
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavGraphBuilder.tasksScreen(context: Context, onAddTask: () -> Unit, onTaskClick: (Task) -> Unit, onShowStatistics: () -> Unit) {
    composable(
        route = TASK_ROUTE,
        arguments =
        listOf(
            navArgument(USER_MESSAGE_ARG) {
                type = NavType.IntType
                defaultValue = 0
            }
        )
    ) { entry ->
        TasksRoute(
            userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG) ?: 0,
            onAddTask = onAddTask,
            onTaskClick = onTaskClick,
            openDrawer = {
                Toast.makeText(
                    context,
                    "Open Drawer - Not implemented",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onShowStatistic = onShowStatistics
        )
    }
}
