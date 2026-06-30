package com.logistics.warehouse.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Warehouse Service API",
                version = "v1",
                description = "Inventory management and BR-006 capacity"
        )
)
@Configuration
public class OpenApiConfig {
}
