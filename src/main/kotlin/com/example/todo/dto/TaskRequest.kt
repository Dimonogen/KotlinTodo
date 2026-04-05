package com.example.todo.dto

data class TaskRequest(
    val title: String,
    val description: String? = null
)


