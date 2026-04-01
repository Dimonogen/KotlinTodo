package com.example.todo.exception

open class TaskException(message: String) : RuntimeException(message)

class TaskNotFoundException(id: Long) : TaskException("Task not found with id: $id")

class ValidationException(message: String) : TaskException(message)
