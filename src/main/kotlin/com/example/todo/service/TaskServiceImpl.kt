package com.example.todo.service

import org.springframework.stereotype.Service
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

@Service
class TaskServiceImpl(private val taskRepository: TaskRepository) : TaskService {

    override fun createTask(request: TaskRequest): Mono<TaskResponse> {
        return validateTitle(request.title)
            .then(Mono.fromCallable { TaskMapper.toTask(request) })
            .flatMap(taskRepository::save)
            .map(TaskMapper::toResponse)
            .switchIfEmpty(Mono.error(ValidationException("Failed to create task")))
    }

    override fun getTaskById(id: Long): Mono<TaskResponse> {
        return taskRepository.findById(id)
            .map(TaskMapper::toResponse)
            .switchIfEmpty(Mono.error(TaskNotFoundException(id)))
    }

    override fun getTasks(page: Int, size: Int, status: TaskStatus?): Mono<PageResponse<TaskResponse>> {
        val pageRequest = PageRequest.of(page, size)
        
        return taskRepository.findAll(pageRequest, status)
            .map { (tasks, totalElements) ->
                PageResponse.of(
                    content = tasks.map(TaskMapper::toResponse),
                    pageRequest = pageRequest,
                    totalElements = totalElements
                )
            }
    }

    override fun updateTaskStatus(id: Long, request: TaskStatusUpdateRequest): Mono<TaskResponse> {
        return validateTitle(request.status)
            .then(taskRepository.findById(id))
            .switchIfEmpty(Mono.error(TaskNotFoundException(id)))
            .flatMap { task ->
                val newStatus = TaskStatus.valueOf(request.status.uppercase())
                if (task.status == newStatus) {
                    Mono.just(task)
                } else {
                    taskRepository.updateStatus(id, newStatus)
                        .switchIfEmpty(Mono.error(TaskNotFoundException(id)))
                }
            }
            .map(TaskMapper::toResponse)
    }

    override fun deleteTask(id: Long): Mono<Void> {
        return taskRepository.deleteById(id)
            .onErrorMap { TaskNotFoundException(id) }
    }

    private fun validateTitle(title: String): Mono<Unit> {
        val trimmed = title.trim()
        
        if (trimmed.isEmpty()) {
            return Mono.error(ValidationException("Title cannot be empty"))
        }
        
        if (trimmed.length < 3 || trimmed.length > 100) {
            return Mono.error(ValidationException("Title length must be between 3 and 100 characters"))
        }
        
        return Mono.empty()
    }
}
