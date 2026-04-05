package com.example.todo

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SdkConfig {

    @Bean
    fun api(): OpenAPI = OpenAPI().info(
        Info()
            .title("Kotlin Todo API")
            .version("1.0")
            .description("REST API for task management built with Kotlin and Spring WebFlux")
            .contact(
                Contact()
                    .name("API Support")
            )
    )

    @Bean
    fun tasksApi(): GroupedOpenApi = GroupedOpenApi.builder()
        .group("tasks-api")
        .pathsToMatch("/api/**")
        .build()
}
