package com.example.todo.controller

import com.example.todo.dto.PageRequest
import com.example.todo.dto.TaskRequest
import com.example.todo.dto.TaskStatusUpdateRequest
import com.example.todo.exception.ValidationException
import com.example.todo.model.TaskStatus
import com.example.todo.service.TaskService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class TaskControllerTest {

    private val taskService: TaskService = mock()
    private val taskController: TaskController = TaskController(taskService)

    @BeforeEach
    fun setUp() {
        // Setup before each test
    }

    @Test
    fun `should create task and return created status`() {
        val request = TaskRequest(title = "New Task", description = "Description")
        val response = com.example.todo.dto.TaskResponse(
            id = 1L, 
            title = "New Task", 
            description = "Description", 
            status = TaskStatus.NEW, 
            createdAt = LocalDateTime.now(), 
            updatedAt = LocalDateTime.now()
        )

        whenever(taskService.createTask(any())).thenReturn(Mono.just(response))

        StepVerifier.create(taskController.createTask(request))
            .assertNext { result ->
                assertEquals(response.id, (result as com.example.todo.dto.TaskResponse).id)
            }
            .verifyComplete()
    }

    @Test
    fun `should get tasks with pagination and filter`() {
        
        whenever(taskService.getTasks(eq(0), eq(10), eq(TaskStatus.NEW)))
            .thenReturn(Mono.just(com.example.todo.dto.PageResponse.of(
                listOf(com.example.todo.dto.TaskResponse(
                    id = 1L, 
                    title = "Task", 
                    description = null, 
                    status = TaskStatus.NEW, 
                    createdAt = LocalDateTime.now(), 
                    updatedAt = LocalDateTime.now()
                )), 
                PageRequest(0, 10), 
                1L
            )))

        StepVerifier.create(taskController.getTasks(0, 10, "NEW"))
            .assertNext { result ->
                assertEquals(true, result != null)
            }
            .verifyComplete()
    }

    @Test
    fun `should get tasks without status filter`() {
        whenever(taskService.getTasks(eq(0), eq(5), eq(null)))
            .thenReturn(Mono.just(com.example.todo.dto.PageResponse.of(
                listOf(), 
                PageRequest(0, 5), 
                0L
            )))

        StepVerifier.create(taskController.getTasks(0, 5, null))
            .assertNext { result ->
                assertEquals(true, result != null)
            }
            .verifyComplete()
    }

    @Test
    fun `should get task by id`() {
        val response = com.example.todo.dto.TaskResponse(
            id = 42L, 
            title = "Found Task", 
            description = null, 
            status = TaskStatus.IN_PROGRESS, 
            createdAt = LocalDateTime.now(), 
            updatedAt = LocalDateTime.now()
        )

        whenever(taskService.getTaskById(any())).thenReturn(Mono.just(response))

        StepVerifier.create(taskController.getTaskById(42L))
            .assertNext { result ->
                val typedResult = result as com.example.todo.dto.TaskResponse
                assertEquals(42L, typedResult.id)
                assertEquals("Found Task", typedResult.title)
            }
            .verifyComplete()
    }

    @Test
    fun `should update task status`() {
        val request = TaskStatusUpdateRequest(status = "DONE")
        
        val updatedTask = com.example.todo.dto.TaskResponse(
            id = 1L, 
            title = "Task", 
            description = null, 
            status = TaskStatus.DONE, 
            createdAt = LocalDateTime.now(), 
            updatedAt = LocalDateTime.now()
        )

        whenever(taskService.updateTaskStatus(eq(1L), any())).thenReturn(Mono.just(updatedTask))

        StepVerifier.create(taskController.updateTaskStatus(1L, request))
            .assertNext { result ->
                val typedResult = result as com.example.todo.dto.TaskResponse
                assertEquals(TaskStatus.DONE, typedResult.status)
            }
            .verifyComplete()
    }

    @Test
    fun `should delete task`() {
        whenever(taskService.deleteTask(any())).thenReturn(Mono.empty())

        StepVerifier.create(taskController.deleteTask(1L))
            .verifyComplete()
    }

    @Test
    fun `should handle invalid status update`() {
        val request = TaskStatusUpdateRequest(status = "INVALID_STATUS")

        try {
            taskController.updateTaskStatus(1L, request).block()
            assertEquals(false, true)
        } catch (e: Exception) {
            // Expected - ValidationException or IllegalArgumentException
        }
    }
}
