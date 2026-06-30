package com.logistics.routing.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Routing Service API",
                version = "v1",
                description = "Route calculation: distance, ETA, fuel estimate, toll cost"
        )
)
@Configuration
public class OpenApiConfig {
}
