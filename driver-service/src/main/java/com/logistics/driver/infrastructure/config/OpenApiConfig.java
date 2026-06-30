package com.logistics.driver.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Driver Service API",
                version = "v1",
                description = "Driver management and BR-005 hour tracking"
        )
)
@Configuration
public class OpenApiConfig {
}
