package com.example.todo.service

import com.example.todo.dto.PageRequest
import com.example.todo.dto.PageResponse
import com.example.todo.dto.TaskMapper
import com.example.todo.dto.TaskRequest
import com.example.todo.dto.TaskStatusUpdateRequest
import com.example.todo.dto.TaskResponse
import com.example.todo.exception.TaskNotFoundException
import com.example.todo.exception.ValidationException
import com.example.todo.model.TaskStatus
import com.example.todo.repository.TaskRepository
import reactor.core.publisher.Mono

interface TaskService {
    fun createTask(request: TaskRequest): Mono<TaskResponse>
    fun getTaskById(id: Long): Mono<TaskResponse>
    fun getTasks(page: Int, size: Int, status: TaskStatus?): Mono<PageResponse<TaskResponse>>
    fun updateTaskStatus(id: Long, request: TaskStatusUpdateRequest): Mono<TaskResponse>
    fun deleteTask(id: Long): Mono<Void>
}
