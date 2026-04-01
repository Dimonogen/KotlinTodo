package com.example.todo.dto

import java.util.function.Function

data class PageRequest(
    val page: Int,
    val size: Int
) {
    fun offset(): Int = page * size
    
    companion object {
        fun of(page: Int, size: Int): PageRequest {
            require(page >= 0) { "Page must be greater than or equal to 0" }
            require(size > 0 && size <= 100) { "Size must be between 1 and 100" }
            return PageRequest(page, size)
        }
    }
}

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    companion object {
        fun <T> of(content: List<T>, pageRequest: PageRequest, totalElements: Long): PageResponse<T> {
            val totalPages = if (totalElements == 0L) 1 else (totalElements / pageRequest.size + (if (totalElements % pageRequest.size != 0L) 1 else 0)).toInt()
            return PageResponse(content, pageRequest.page, pageRequest.size, totalElements, totalPages)
        }
    }
}
