package com.example.todo.repository

import com.example.todo.dto.PageRequest
import com.example.todo.model.Task
import com.example.todo.model.TaskStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import reactor.test.StepVerifier

@JdbcTest
class TaskRepositoryTest {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate
    
    private lateinit var taskRepository: TaskRepository

    @BeforeEach
    fun setUp() {
        taskRepository = TaskRepository(jdbcTemplate)
    }

    @Test
    fun `should create and return task with id`() {
        val task = Task(
            title = "Test Task", 
            description = "Description",
            status = TaskStatus.NEW
        )

        StepVerifier.create(taskRepository.save(task))
            .assertNext { savedTask ->
                assertNotNull(savedTask.id)
                assertEquals("Test Task", savedTask.title)
                assertEquals(TaskStatus.NEW, savedTask.status)
            }
            .verifyComplete()
    }

    @Test
    fun `should find task by id`() {
        val task = Task(
            title = "To Find", 
            description = null,
            status = TaskStatus.IN_PROGRESS
        )

        StepVerifier.create(taskRepository.save(task))
            .assertNext { savedTask ->
                StepVerifier.create(taskRepository.findById(savedTask.id!!))
                    .assertNext { foundTask ->
                        assertEquals("To Find", foundTask.title)
                        assertEquals(TaskStatus.IN_PROGRESS, foundTask.status)
                    }
                    .verifyComplete()
            }
            .verifyComplete()
    }

    @Test
    fun `should return empty when task not found`() {
        StepVerifier.create(taskRepository.findById(999L))
            .expectErrorMatches { it.message?.contains("not found") == true }
            .verify()
    }

    @Test
    fun `should find all tasks with pagination`() {
        repeat(5) { i ->
            Task(title = "Task $i", description = null, status = TaskStatus.NEW).let { 
                taskRepository.save(it).block() 
            }
        }

        StepVerifier.create(taskRepository.findAll(PageRequest.of(0, 3), null))
            .assertNext { result ->
                val tasks = result.first
                val total = result.second
                assertEquals(3, tasks.size)
                assertTrue(total >= 5L)
            }
            .verifyComplete()
    }

    @Test
    fun `should filter tasks by status`() {
        Task(title = "New Task", description = null, status = TaskStatus.NEW).let { 
            taskRepository.save(it).block() 
        }
        Task(title = "Done Task", description = null, status = TaskStatus.DONE).let { 
            taskRepository.save(it).block() 
        }

        StepVerifier.create(taskRepository.findAll(PageRequest.of(0, 10), TaskStatus.DONE))
            .assertNext { result ->
                val tasks = result.first
                val total = result.second
                assertEquals(1, tasks.size)
                assertEquals(TaskStatus.DONE, tasks.first().status)
                assertEquals(1L, total)
            }
            .verifyComplete()
    }

    @Test
    fun `should update task status`() {
        val task = Task(title = "Task", description = null, status = TaskStatus.NEW)
        
        StepVerifier.create(taskRepository.save(task))
            .assertNext { saved ->
                StepVerifier.create(taskRepository.updateStatus(saved.id!!, TaskStatus.DONE))
                    .assertNext { updated ->
                        assertEquals(TaskStatus.DONE, updated.status)
                    }
                    .verifyComplete()
            }
            .verifyComplete()
    }

    @Test
    fun `should delete task`() {
        val task = Task(title = "To Delete", description = null, status = TaskStatus.NEW)
        
        StepVerifier.create(taskRepository.save(task))
            .assertNext { saved ->
                StepVerifier.create(taskRepository.deleteById(saved.id!!))
                    .verifyComplete()
                
                StepVerifier.create(taskRepository.findById(saved.id!!))
                    .expectErrorMatches { it.message?.contains("not found") == true }
                    .verify()
            }
            .verifyComplete()
    }

    @Test
    fun `should return empty when deleting non-existent task`() {
        StepVerifier.create(taskRepository.deleteById(999L))
            .expectErrorMatches { it.message?.contains("not found") == true }
            .verify()
    }
}
