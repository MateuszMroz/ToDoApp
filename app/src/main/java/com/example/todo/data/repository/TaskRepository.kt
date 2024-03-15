package com.example.todo.data.repository

import com.example.todo.data.model.Task
import com.example.todo.data.source.local.TaskDao
import com.example.todo.data.source.local.TaskEntity
import com.example.todo.data.toEntity
import com.example.todo.data.toTask
import com.example.todo.data.toTasks
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class TaskRepository
@Inject
constructor(
    private val taskDao: TaskDao
) : ITaskRepository {
    override fun getTasksStream(): Flow<List<Task>> = taskDao.observeAll().map { it.toTasks() }

    override suspend fun getTasks(forceUpdate: Boolean): List<Task> = taskDao.getAll().toTasks()

    override suspend fun refresh() {
        delay(2000)
    }

    override fun getTaskStream(taskId: String): Flow<Task?> = taskDao.observeById(taskId).map {
        it.toTask()
    }

    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Task? = taskDao.getById(taskId)?.toTask()

    override suspend fun refreshTask(taskId: String) {
        refresh()
    }

    override suspend fun createTask(title: String, description: String): String {
        val taskId = UUID.randomUUID().toString()
        val task =
            TaskEntity(
                id = taskId,
                title = title,
                description = description,
                isCompleted = false
            )

        taskDao.upsert(task)

        return taskId
    }

    override suspend fun updateTask(taskId: String, title: String, description: String) {
        val task =
            getTask(taskId)?.copy(
                title = title,
                description = description
            )?.toEntity() ?: throw IllegalStateException("Task (id: $taskId) not found")

        taskDao.upsert(task)
    }

    override suspend fun completeTask(taskId: String) {
        taskDao.updateCompleted(taskId, completed = true)
    }

    override suspend fun activateTask(taskId: String) {
        taskDao.updateCompleted(taskId, completed = false)
    }

    override suspend fun clearCompletedTasks() {
        taskDao.deleteCompleted()
    }

    override suspend fun deleteAllTasks() {
        taskDao.deleteAll()
    }

    override suspend fun deleteTask(taskId: String) {
        taskDao.deleteById(taskId)
    }
}
