# Kotlin Todo Application

Spring Boot REST API для управления задачами, написанная на Kotlin с использованием реактивного стека (WebFlux).

## Возможности

- Создание задач
- Получение списка задач с пагинацией и фильтрацией по статусу
- Получение отдельной задачи по ID
- Обновление статуса задачи
- Удаление задачи
- Поддержка PostgreSQL и H2 баз данных

## Технологии

- Kotlin 1.9.24
- Spring Boot 3.2.5
- Spring WebFlux (реактивный)
- PostgreSQL / H2
- Flyway (миграции БД)
- Reactor Kotlin

## Требования

- Java 17+
- Gradle 8.x (установлен системно)

## Установка

### Ubuntu/Debian

```bash
sudo apt update
sudo apt install openjdk-17-jdk gradle
```

### macOS (Homebrew)

```bash
brew install openjdk@17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/bin/java
brew install gradle
```

### Windows (Chocolatey)

```powershell
choco install openjdk17
choco install gradle
```

### Проверка установки

```bash
java -version
gradle --version
```

## Запуск проекта

### Сборка и запуск тестов

```bash
gradle build
```

### Запуск приложения

```bash
gradle bootRun
```

Приложение будет доступно по адресу: `http://localhost:8080`

### База данных

По умолчанию используется H2 в памяти. Для переключения на PostgreSQL измените `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/todo_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## API Endpoints

### Создание задачи

```http
POST /api/tasks
Content-Type: application/json

{
  "title": "Новая задача",
  "description": "Описание задачи"
}
```

### Получение списка задач

```http
GET /api/tasks?page=0&size=10&status=NEW
```

Параметры:
- `page` - номер страницы (обязательный)
- `size` - размер страницы (обязательный)
- `status` - фильтрация по статусу (опциональный): NEW, IN_PROGRESS, DONE, CANCELLED

### Получение задачи по ID

```http
GET /api/tasks/{id}
```

### Обновление статуса задачи

```http
PATCH /api/tasks/{id}/status
Content-Type: application/json

{
  "status": "DONE"
}
```

### Удаление задачи

```http
DELETE /api/tasks/{id}
```

## Структура проекта

```
src/main/kotlin/com/example/todo/
├── controller/      - REST контроллеры
├── service/         - Сервисный слой
├── repository/      - Репозитории для работы с БД
├── model/           - Модели данных
├── dto/             - DTO классы
└── exception/       - Обработка исключений
```

## Тестирование

Запустить тесты:

```bash
./gradlew test
```
