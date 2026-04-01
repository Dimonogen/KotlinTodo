package com.example.todo.repository

import com.example.todo.dto.PageRequest
import com.example.todo.model.Task
import com.example.todo.model.TaskStatus
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
class TaskRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    private val rowMapper = { rs: java.sql.ResultSet, _: Int ->
        Task(
            id = rs.getLong("id"),
            title = rs.getString("title"),
            description = rs.getString("description") ?: null,
            status = TaskStatus.valueOf(rs.getString("status")),
            createdAt = rs.getTimestamp("created_at")?.toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at")?.toLocalDateTime()
        )
    }

    fun save(task: Task): Mono<Task> {
        val now = LocalDateTime.now()
        return if (task.id == null) {
            createTask(task.copy(createdAt = now, updatedAt = now))
        } else {
            updateTask(task.copy(updatedAt = now))
        }
    }

    private fun createTask(task: Task): Mono<Task> {
        val sql = """
            INSERT INTO tasks (title, description, status, created_at, updated_at)
            VALUES (:title, :description, :status, :created_at, :updated_at)
            RETURNING id, title, description, status, created_at, updated_at
        """.trimIndent()
        
        val parameters = mapOf(
            "title" to task.title,
            "description" to task.description,
            "status" to task.status.name,
            "created_at" to task.createdAt,
            "updated_at" to task.updatedAt
        )
        
        return Mono.fromCallable {
            jdbcTemplate.queryForObject(sql, parameters, rowMapper) ?: throw Exception("Failed to create task")
        }
    }

    private fun updateTask(task: Task): Mono<Task> {
        val sql = """
            UPDATE tasks 
            SET title = :title, description = :description, status = :status, updated_at = :updated_at
            WHERE id = :id
            RETURNING id, title, description, status, created_at, updated_at
        """.trimIndent()
        
        val parameters = mapOf(
            "title" to task.title,
            "description" to task.description,
            "status" to task.status.name,
            "updated_at" to task.updatedAt,
            "id" to task.id!!
        )
        
        return Mono.fromCallable {
            jdbcTemplate.queryForObject(sql, parameters, rowMapper) ?: throw Exception("Failed to update task")
        }
    }

    fun findById(id: Long): Mono<Task> {
        val sql = """
            SELECT id, title, description, status, created_at, updated_at
            FROM tasks WHERE id = :id
        """.trimIndent()
        
        return Mono.fromCallable {
            jdbcTemplate.queryForObject(sql, mapOf("id" to id), rowMapper) 
                ?: throw Exception("Task not found with id: $id")
        }
    }

    fun findAll(pageRequest: PageRequest, status: TaskStatus? = null): Mono<Pair<List<Task>, Long>> {
        val offset = pageRequest.offset()
        
        return if (status != null) {
            val countSql = "SELECT COUNT(*) as total FROM tasks WHERE status = :status"
            val fetchSql = """
                SELECT id, title, description, status, created_at, updated_at
                FROM tasks WHERE status = :status ORDER BY created_at DESC LIMIT :size OFFSET :offset
            """.trimIndent()
            
            Mono.fromCallable {
                jdbcTemplate.queryForObject(countSql, mapOf("status" to status.name), Long::class.java) 
                    ?: 0L
            }.flatMap { count: Long ->
                val tasks = jdbcTemplate.query(fetchSql, 
                    mapOf("status" to status.name, "size" to pageRequest.size, "offset" to offset),
                    rowMapper
                )
                Mono.just(Pair(tasks, count))
            }
        } else {
            val countSql = "SELECT COUNT(*) as total FROM tasks"
            val fetchSql = """
                SELECT id, title, description, status, created_at, updated_at
                FROM tasks ORDER BY created_at DESC LIMIT :size OFFSET :offset
            """.trimIndent()
            
            Mono.fromCallable {
                jdbcTemplate.queryForObject(countSql, mapOf<String, Any>(), Long::class.java) ?: 0L
            }.flatMap { count: Long ->
                val tasks = jdbcTemplate.query(fetchSql, 
                    mapOf("size" to pageRequest.size, "offset" to offset),
                    rowMapper
                )
                Mono.just(Pair(tasks, count))
            }
        }
    }

    fun updateStatus(id: Long, status: TaskStatus): Mono<Task> {
        val now = LocalDateTime.now()
        val sql = """
            UPDATE tasks 
            SET status = :status, updated_at = :updated_at
            WHERE id = :id AND status != :old_status
            RETURNING id, title, description, status, created_at, updated_at
        """.trimIndent()
        
        return Mono.fromCallable {
            jdbcTemplate.queryForObject(sql, mapOf(
                "status" to status.name, 
                "updated_at" to now, 
                "id" to id,
                "old_status" to status.name
            ), rowMapper)
        }
    }

    fun deleteById(id: Long): Mono<Void> {
        val sql = "DELETE FROM tasks WHERE id = :id"
        
        return Mono.fromCallable {
            jdbcTemplate.update(sql, mapOf("id" to id))
        }.flatMap { rowCount ->
            if (rowCount > 0) Mono.empty() else Mono.error(Exception("Task not found"))
        }
    }
}
