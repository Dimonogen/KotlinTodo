package com.example.todo.service

import com.example.todo.dto.PageRequest
import com.example.todo.dto.PageResponse
import com.example.todo.dto.TaskMapper
import com.example.todo.dto.TaskRequest
import com.example.todo.dto.TaskStatusUpdateRequest
import com.example.todo.dto.TaskResponse
import com.example.todo.exception.TaskNotFoundException
import com.example.todo.exception.ValidationException
import com.example.todo.model.Task
import com.example.todo.model.TaskStatus
import com.example.todo.repository.TaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class TaskServiceTest {

    @Mock
    private lateinit var taskRepository: TaskRepository

    private lateinit var taskService: TaskService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        taskService = TaskServiceImpl(taskRepository)
    }

    @Test
    fun `should create task successfully`() {
        val request = TaskRequest(title = "New Task", description = "Description")
        val savedTask = Task(id = 1L, title = "New Task", description = "Description", status = TaskStatus.NEW)

        whenever(taskRepository.save(any())).thenReturn(Mono.just(savedTask))

        StepVerifier.create(taskService.createTask(request))
            .assertNext { response ->
                assertEquals(1L, response.id)
                assertEquals("New Task", response.title)
                assertEquals(TaskStatus.NEW, response.status)
            }
            .verifyComplete()

        verify(taskRepository).save(any())
    }

    @Test
    fun `should fail to create task with empty title`() {
        val request = TaskRequest(title = "", description = "Description")

        StepVerifier.create(taskService.createTask(request))
            .expectError(ValidationException::class.java)
            .verify()
    }

    @Test
    fun `should fail to create task with short title`() {
        val request = TaskRequest(title = "ab", description = "Description")

        StepVerifier.create(taskService.createTask(request))
            .expectError(ValidationException::class.java)
            .verify()
    }

    @Test
    fun `should get task by id successfully`() {
        val task = Task(id = 1L, title = "Task", description = null, status = TaskStatus.NEW)
        whenever(taskRepository.findById(eq(1L))).thenReturn(Mono.just(task))

        StepVerifier.create(taskService.getTaskById(1L))
            .assertNext { response ->
                assertEquals(1L, response.id)
                assertEquals("Task", response.title)
            }
            .verifyComplete()
    }

    @Test
    fun `should fail to get task by id when not found`() {
        whenever(taskRepository.findById(any())).thenReturn(Mono.empty())

        StepVerifier.create(taskService.getTaskById(999L))
            .expectError(TaskNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `should get paginated tasks with status filter`() {
        val task1 = Task(id = 1L, title = "Task 1", description = null, status = TaskStatus.NEW)
        
        whenever(taskRepository.findAll(any(), eq(TaskStatus.NEW)))
            .thenReturn(Mono.just(Pair(listOf(task1), 1L)))

        StepVerifier.create(taskService.getTasks(0, 10, TaskStatus.NEW))
            .assertNext { response ->
                assertEquals(1, response.content.size)
                assertEquals(1L, response.totalElements)
                assertEquals(0, response.page)
                assertEquals(10, response.size)
            }
            .verifyComplete()
    }

    @Test
    fun `should get paginated tasks without filter`() {
        val task1 = Task(id = 1L, title = "Task 1", description = null, status = TaskStatus.NEW)
        
        whenever(taskRepository.findAll(any(), eq(null)))
            .thenReturn(Mono.just(Pair(listOf(task1), 1L)))

        StepVerifier.create(taskService.getTasks(0, 10, null))
            .assertNext { response ->
                assertEquals(1, response.content.size)
                assertEquals(1L, response.totalElements)
            }
            .verifyComplete()
    }

    @Test
    fun `should update task status successfully`() {
        val existingTask = Task(id = 1L, title = "Task", description = null, status = TaskStatus.NEW)
        val updatedTask = Task(id = 1L, title = "Task", description = null, status = TaskStatus.DONE)

        whenever(taskRepository.findById(eq(1L))).thenReturn(Mono.just(existingTask))
        whenever(taskRepository.updateStatus(eq(1L), eq(TaskStatus.DONE)))
            .thenReturn(Mono.just(updatedTask))

        val request = TaskStatusUpdateRequest(status = "DONE")

        StepVerifier.create(taskService.updateTaskStatus(1L, request))
            .assertNext { response ->
                assertEquals(1L, response.id)
                assertEquals(TaskStatus.DONE, response.status)
            }
            .verifyComplete()
    }

    @Test
    fun `should fail to update task status when not found`() {
        whenever(taskRepository.findById(any())).thenReturn(Mono.empty())

        val request = TaskStatusUpdateRequest(status = "DONE")

        StepVerifier.create(taskService.updateTaskStatus(999L, request))
            .expectError(TaskNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `should not update status if already same`() {
        val existingTask = Task(id = 1L, title = "Task", description = null, status = TaskStatus.DONE)
        
        whenever(taskRepository.findById(eq(1L))).thenReturn(Mono.just(existingTask))

        val request = TaskStatusUpdateRequest(status = "DONE")

        StepVerifier.create(taskService.updateTaskStatus(1L, request))
            .assertNext { response ->
                assertEquals(TaskStatus.DONE, response.status)
            }
            .verifyComplete()
    }

    @Test
    fun `should delete task successfully`() {
        whenever(taskRepository.deleteById(eq(1L))).thenReturn(Mono.empty())

        StepVerifier.create(taskService.deleteTask(1L))
            .verifyComplete()
    }
}
