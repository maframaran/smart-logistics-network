package com.logistics.fleet.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Fleet Service API",
                version = "v1",
                description = "Vehicle registration and capacity management"
        )
)
@Configuration
public class OpenApiConfig {
}
