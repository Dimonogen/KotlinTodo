package com.example.todo.dto

import com.example.todo.model.Task
import com.example.todo.model.TaskStatus
import java.time.LocalDateTime

object TaskMapper {
    fun toRequest(title: String, description: String?): TaskRequest {
        return TaskRequest(title.trim(), description?.takeIf { it.isNotBlank() })
    }
    
    fun toResponse(task: Task): TaskResponse {
        requireNotNull(task.id) { "Task must have an id" }
        return TaskResponse(
            id = task.id!!,
            title = task.title,
            description = task.description,
            status = task.status,
            createdAt = task.createdAt ?: LocalDateTime.now(),
            updatedAt = task.updatedAt ?: LocalDateTime.now()
        )
    }
    
    fun toTask(request: TaskRequest): Task {
        return Task(
            title = request.title.trim(),
            description = request.description,
            status = TaskStatus.NEW
        )
    }
}
