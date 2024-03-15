package com.example.todo.feature.addedit.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType.Companion.IntType
import androidx.navigation.NavType.Companion.StringType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.todo.ADD_RESULT_OK
import com.example.todo.EDIT_RESULT_OK
import com.example.todo.feature.addedit.AddEditTaskRoute

const val ADD_EDIT_TASK_SCREEN = "add_edit_task"
const val TASK_ID_ARG = "task_id_arg"
const val TITLE_ARG = "title"
const val ADD_EDIT_TASK_ROUTE =
    "${ADD_EDIT_TASK_SCREEN}/{${TITLE_ARG}}?${TASK_ID_ARG}={${TASK_ID_ARG}}"

fun NavController.navigateToAddEditTask(title: Int, taskId: String? = null) {
    val route =
        "$ADD_EDIT_TASK_SCREEN/$title".let { if (taskId != null) "$it?$TASK_ID_ARG=$taskId" else it }

    navigate(route) {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.addEditScreen(onTaskUpdate: (Int) -> Unit, onBack: () -> Unit) {
    composable(
        route = ADD_EDIT_TASK_ROUTE,
        arguments =
        listOf(
            navArgument(TITLE_ARG) { type = IntType },
            navArgument(TASK_ID_ARG) {
                type = StringType
                nullable = true
            }
        )
    ) { entry ->
        val title = entry.arguments?.getInt(TITLE_ARG) ?: 0
        val taskId = entry.arguments?.getString(TASK_ID_ARG)

        AddEditTaskRoute(
            topBarTitle = title,
            onTaskUpdate = { onTaskUpdate(if (taskId != null) EDIT_RESULT_OK else ADD_RESULT_OK) },
            onBack = onBack
        )
    }
}
