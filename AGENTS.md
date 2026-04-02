# Agent Guidelines for Kotlin Todo Project

## Project Overview
Spring Boot REST API for task management built with Kotlin using reactive stack (WebFlux).

## Tech Stack
- Kotlin 1.9.24
- Spring Boot 3.2.5
- Spring WebFlux (reactive)
- PostgreSQL / H2 databases
- Flyway (database migrations)
- Reactor Kotlin

## Requirements
- Java 17+
- Gradle 8.x

## Project Structure
```
src/main/kotlin/com/example/todo/
├── controller/      - REST controllers
├── service/         - Service layer
├── repository/      - Database repositories
├── model/           - Data models
├── dto/             - DTO classes
└── exception/       - Exception handling
```

## Development Commands
- Build and run tests: `gradle build`
- Run application: `gradle bootRun`
- Run tests only: `gradle test`

## API Endpoints
- POST /api/tasks - Create task
- GET /api/tasks?page=0&size=10&status=NEW - List tasks with pagination and filtering
- GET /api/tasks/{id} - Get task by ID
- PATCH /api/tasks/{id}/status - Update task status
- DELETE /api/tasks/{id} - Delete task

## Database Configuration
Default: H2 in-memory database
PostgreSQL: Configure in application.properties with connection details

## Task Statuses
NEW, IN_PROGRESS, DONE, CANCELLED
