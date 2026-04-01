package com.example.todo.controller

import com.example.todo.dto.PageRequest
import com.example.todo.dto.TaskRequest
import com.example.todo.dto.TaskStatusUpdateRequest
import com.example.todo.exception.ValidationException
import com.example.todo.model.TaskStatus
import com.example.todo.service.TaskService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val taskService: TaskService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTask(@RequestBody request: TaskRequest): Mono<*> = 
        taskService.createTask(request)

    @GetMapping
    fun getTasks(
        @RequestParam page: Int,
        @RequestParam size: Int,
        @RequestParam status: String? = null
    ): Mono<*> {
        val parsedStatus = status?.let { tryOrNull { TaskStatus.valueOf(it.uppercase()) } }
        return taskService.getTasks(page, size, parsedStatus)
    }

    @GetMapping("/{id}")
    fun getTaskById(@PathVariable id: Long): Mono<*> = 
        taskService.getTaskById(id)

    @PatchMapping("/{id}/status")
    fun updateTaskStatus(
        @PathVariable id: Long,
        @RequestBody request: TaskStatusUpdateRequest
    ): Mono<*> {
        val status = tryOrNull { TaskStatus.valueOf(request.status.uppercase()) }
            ?: throw ValidationException("Invalid status value. Must be one of: NEW, IN_PROGRESS, DONE, CANCELLED")
        
        return taskService.updateTaskStatus(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTask(@PathVariable id: Long): Mono<*> = 
        taskService.deleteTask(id)
}

fun <T> tryOrNull(block: () -> T): T? {
    return try {
        block()
    } catch (e: Exception) {
        null
    }
}
