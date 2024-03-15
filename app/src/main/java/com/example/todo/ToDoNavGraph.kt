package com.example.todo

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.todo.feature.addedit.navigation.addEditScreen
import com.example.todo.feature.addedit.navigation.navigateToAddEditTask
import com.example.todo.feature.details.navigation.detailsScreen
import com.example.todo.feature.details.navigation.navigateToTaskDetail
import com.example.todo.feature.statistic.navigation.navigateToStatistic
import com.example.todo.feature.statistic.navigation.statisticsScreen
import com.example.todo.feature.tasks.navigation.TASK_ROUTE
import com.example.todo.feature.tasks.navigation.navigateToTasks
import com.example.todo.feature.tasks.navigation.tasksScreen

@Composable
fun ToDoNavGraph(modifier: Modifier = Modifier, startDestination: String = TASK_ROUTE) {
    val context = LocalContext.current
    val navController: NavHostController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        tasksScreen(
            context = context,
            onAddTask = { navController.navigateToAddEditTask(R.string.add_task) },
            onTaskClick = { navController.navigateToTaskDetail(it.id) },
            onShowStatistics = { navController.navigateToStatistic() }
        )

        addEditScreen(
            onTaskUpdate = { result -> navController.navigateToTasks(result) },
            onBack = { navController.popBackStack() }
        )

        detailsScreen(
            onEditTask = { taskId ->
                navController.navigateToAddEditTask(
                    R.string.edit_task,
                    taskId
                )
            },
            onDeleteTask = { navController.navigateToTasks(DELETE_RESULT_OK) },
            onBack = { navController.popBackStack() }
        )

        statisticsScreen(
            onBack = { navController.popBackStack() }
        )
    }
}

const val ADD_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 3
