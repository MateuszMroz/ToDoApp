package com.example.todo.data.repository

import app.cash.turbine.test
import com.example.todo.data.model.Task
import com.example.todo.data.source.local.TaskDao
import com.example.todo.data.source.local.TaskEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TaskRepositoryTest {

    private val taskDao = mockk<TaskDao>(relaxed = true)

    private val taskRepository = TaskRepository(taskDao)

    @Test
    fun `return list of tasks stream when getTasksStream is called`() = runTest {
        // given
        every { taskDao.observeAll() } returns flowOf(
            listOf(
                TaskEntity("1", "title", "description", false),
                TaskEntity("2", "title", "description", true)
            )
        )

        // when
        taskRepository.getTasksStream().test {
            // then
            assertThat(awaitItem()).isEqualTo(
                listOf(
                    Task("1", "title", "description", false),
                    Task("2", "title", "description", true)
                )
            )
            awaitComplete()
        }
    }

    @Test
    fun `return task stream when getTaskStream is called`() = runTest {
        // given
        every { taskDao.observeById("1") } returns flowOf(
            TaskEntity("1", "title", "description", false)
        )

        // when
        taskRepository.getTaskStream("1").test {
            // then
            assertThat(awaitItem()).isEqualTo(
                Task("1", "title", "description", false)
            )
            awaitComplete()
        }
    }

    @Test
    fun `return list of tasks when getTasks is called`() = runTest {
        // given
        coEvery { taskDao.getAll() } returns listOf(
            TaskEntity("1", "title", "description", false),
            TaskEntity("2", "title", "description", true)
        )

        // when
        val result = taskRepository.getTasks(false)

        // then
        assertThat(result).isEqualTo(
            listOf(
                Task("1", "title", "description", false),
                Task("2", "title", "description", true)
            )
        )
    }

    @Test
    fun `return task when getTask is called`() = runTest {
        // given
        coEvery { taskDao.getById("1") } returns TaskEntity("1", "title", "description", false)

        // when
        val result = taskRepository.getTask("1", false)

        // then
        assertThat(result).isEqualTo(
            Task("1", "title", "description", false)
        )
    }

    @Test
    fun `return task id when createTask is called`() = runTest {
        // given
        val title = "title"
        val description = "description"

        // when
        val taskId = taskRepository.createTask(title, description)

        // then
        assertThat(taskId).isNotEmpty()
        assertThat(taskId).hasLength(UUID_LENGTH)
        coVerify { taskDao.upsert(any()) }
    }

    @Test
    fun `update task when updateTask is called`() = runTest {
        // given
        val taskId = "1"
        val title = "title-update"
        val description = "description-update"

        // when
        taskRepository.updateTask(taskId, title, description)

        // then
        coVerify { taskDao.upsert(any()) }
    }

    @Test
    fun `complete task when completeTask is called`() = runTest {
        // given
        val taskId = "1"

        // when
        taskRepository.completeTask(taskId)

        // then
        coVerify { taskDao.updateCompleted(taskId, true) }
    }

    @Test
    fun `activate task when activateTask is called`() = runTest {
        // given
        val taskId = "1"

        // when
        taskRepository.activateTask(taskId)

        // then
        coVerify { taskDao.updateCompleted(taskId, false) }
    }

    @Test
    fun `clear completed tasks when clearCompletedTasks is called`() = runTest {
        // when
        taskRepository.clearCompletedTasks()

        // then
        coVerify { taskDao.deleteCompleted() }
    }

    @Test
    fun `delete task when deleteTask is called`() = runTest {
        // given
        val taskId = "1"

        // when
        taskRepository.deleteTask(taskId)

        // then
        coVerify { taskDao.deleteById(taskId) }
    }

    @Test
    fun `delete all tasks when deleteAllTasks is called`() = runTest {
        // when
        taskRepository.deleteAllTasks()

        // then
        coVerify { taskDao.deleteAll() }
    }

    private companion object {
        const val UUID_LENGTH = 36
    }
}
