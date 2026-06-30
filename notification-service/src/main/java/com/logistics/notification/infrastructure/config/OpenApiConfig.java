package com.logistics.notification.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Notification Service API",
                version = "v1",
                description = "Email delivery via Kafka event consumption (read-only audit API)"
        )
)
@Configuration
public class OpenApiConfig {
}
