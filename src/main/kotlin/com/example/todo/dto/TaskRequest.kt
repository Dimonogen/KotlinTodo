package com.example.todo.dto

import com.example.todo.model.TaskStatus

data class TaskRequest(
    val title: String,
    val description: String? = null
)


