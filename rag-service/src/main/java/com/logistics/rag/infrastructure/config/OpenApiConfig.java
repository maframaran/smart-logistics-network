package com.logistics.rag.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "RAG Intelligence Service API",
                version = "v1",
                description = "pgvector + Claude API: route similarity, waiver assistant, dynamic pricing, inventory advisor, demand forecast"
        )
)
@Configuration
public class OpenApiConfig {
}
