package com.example.todo.repository

import com.example.todo.dto.PageRequest
import com.example.todo.exception.TaskNotFoundException as RepositoryTaskNotFoundException
import com.example.todo.model.Task
import com.example.todo.model.TaskStatus
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
class TaskRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    private val rowMapper: RowMapper<Task> = RowMapper { rs, _ ->
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
        return if (task.id == null) {
            createTask(task)
        } else {
            updateTask(task)
        }
    }

    private fun createTask(task: Task): Mono<Task> {
        val now = LocalDateTime.now()
        
        val insertSql = """
            INSERT INTO tasks (title, description, status, created_at, updated_at)
            VALUES (:title, :description, :status, :created_at, :updated_at)
        """.trimIndent()
        
        return Mono.fromCallable {
            jdbcTemplate.update(
                insertSql, 
                MapSqlParameterSource().apply {
                    addValue("title", task.title)
                    addValue("description", task.description)
                    addValue("status", task.status.name)
                    addValue("created_at", now)
                    addValue("updated_at", now)
                }
            )
        }.flatMap { rowCount: Int ->
            val selectSql = """
                SELECT id FROM tasks WHERE title = :title AND status = :status 
                ORDER BY created_at DESC LIMIT 1
            """.trimIndent()
            
            Mono.fromCallable {
                jdbcTemplate.queryForObject(
                    selectSql, 
                    MapSqlParameterSource().apply {
                        addValue("title", task.title)
                        addValue("status", task.status.name)
                    },
                    Long::class.java
                ) ?: throw RuntimeException("Failed to get generated ID")
            }
        }.map { id: Long ->
            task.copy(id = id, createdAt = now, updatedAt = now)
        }
    }

    private fun updateTask(task: Task): Mono<Task> {
        val sql = """
            UPDATE tasks 
            SET title = :title, description = :description, status = :status, updated_at = :updated_at
            WHERE id = :id
        """.trimIndent()
        
        return Mono.fromCallable {
            jdbcTemplate.update(
                sql, 
                MapSqlParameterSource().apply {
                    addValue("title", task.title)
                    addValue("description", task.description)
                    addValue("status", task.status.name)
                    addValue("updated_at", task.updatedAt)
                    addValue("id", task.id!!)
                }
            )
        }.flatMap { rowCount: Int ->
            if (rowCount > 0) Mono.just(task) else Mono.empty()
        }
    }

    fun findById(id: Long): Mono<Task> {
        val sql = """
            SELECT id, title, description, status, created_at, updated_at
            FROM tasks WHERE id = :id
        """.trimIndent()
        
        return Mono.fromCallable {
            jdbcTemplate.queryForObject(
                sql, 
                MapSqlParameterSource("id", id), 
                rowMapper
            )
        }.flatMap { task: Task? ->
            if (task != null) Mono.just(task) else Mono.error(RepositoryTaskNotFoundException(id))
        }
    }

    fun findAll(pageRequest: PageRequest, status: TaskStatus? = null): Mono<Pair<List<Task>, Long>> {
        val offset = pageRequest.offset()
        
        if (status != null) {
            return Mono.fromCallable {
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tasks WHERE status = :status", 
                    MapSqlParameterSource("status", status.name), 
                    Long::class.java
                ) ?: 0L
            }.flatMap { count: Long ->
                val fetchSql = """
                    SELECT id, title, description, status, created_at, updated_at
                    FROM tasks WHERE status = :status ORDER BY created_at DESC LIMIT :size OFFSET :offset
                """.trimIndent()
                
                Mono.fromCallable {
                    jdbcTemplate.query(
                        fetchSql,
                        MapSqlParameterSource().apply {
                            addValue("status", status.name)
                            addValue("size", pageRequest.size)
                            addValue("offset", offset)
                        },
                        rowMapper
                    )
                }.map { tasks: List<Task> -> Pair(tasks, count) }
            }
        } else {
            return Mono.fromCallable {
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tasks", 
                    MapSqlParameterSource(), 
                    Long::class.java
                ) ?: 0L
            }.flatMap { count: Long ->
                val fetchSql = """
                    SELECT id, title, description, status, created_at, updated_at
                    FROM tasks ORDER BY created_at DESC LIMIT :size OFFSET :offset
                """.trimIndent()
                
                Mono.fromCallable {
                    jdbcTemplate.query(
                        fetchSql,
                        MapSqlParameterSource().apply {
                            addValue("size", pageRequest.size)
                            addValue("offset", offset)
                        },
                        rowMapper
                    )
                }.map { tasks: List<Task> -> Pair(tasks, count) }
            }
        }
    }

    fun updateStatus(id: Long, status: TaskStatus): Mono<Task> {
        val now = LocalDateTime.now()
        
        return Mono.fromCallable {
            jdbcTemplate.queryForObject(
                """
                    SELECT id, title, description, status, created_at, updated_at
                    FROM tasks WHERE id = :id
                """.trimIndent(), 
                MapSqlParameterSource("id", id), 
                rowMapper
            ) ?: throw RepositoryTaskNotFoundException(id)
        }.flatMap { task: Task ->
            val updateSql = """
                UPDATE tasks 
                SET status = :status, updated_at = :updated_at
                WHERE id = :id
            """.trimIndent()
            
            Mono.fromCallable {
                jdbcTemplate.update(
                    updateSql,
                    MapSqlParameterSource().apply {
                        addValue("status", status.name)
                        addValue("updated_at", now)
                        addValue("id", id)
                    }
                )
            }.flatMap { rowCount: Int ->
                if (rowCount > 0) {
                    val updatedTask = Task(
                        id = id,
                        title = task.title,
                        description = task.description,
                        status = status,
                        createdAt = task.createdAt,
                        updatedAt = now
                    )
                    Mono.just(updatedTask)
                } else {
                    Mono.empty()
                }
            }
        }
    }

    fun deleteById(id: Long): Mono<Void> {
        val sql = "DELETE FROM tasks WHERE id = :id"
        
        return Mono.fromCallable {
            jdbcTemplate.update(sql, MapSqlParameterSource("id", id))
        }.flatMap { rowCount: Int ->
            if (rowCount > 0) Mono.empty() else Mono.error(RepositoryTaskNotFoundException(id))
        }
    }

}
