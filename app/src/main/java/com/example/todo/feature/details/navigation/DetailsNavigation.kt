package com.example.todo.feature.details.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.todo.feature.details.TaskDetailsRoute

const val TASK_DETAILS_SCREEN = "task_details"
const val TASK_ID_ARG = "task_id_arg"
const val TASK_DETAILS_ROUTE = "${TASK_DETAILS_SCREEN}/{${TASK_ID_ARG}}"

fun NavController.navigateToTaskDetail(taskId: String) {
    val route = "$TASK_DETAILS_SCREEN/$taskId"

    navigate(route) {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.detailsScreen(onEditTask: (String) -> Unit, onDeleteTask: () -> Unit, onBack: () -> Unit) {
    composable(route = TASK_DETAILS_ROUTE) {
        TaskDetailsRoute(
            onEditTask = onEditTask,
            onDeleteTask = onDeleteTask,
            onBack = onBack
        )
    }
}
