package com.logistics.shipment.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Shipment Service API",
                version = "v1",
                description = "Shipment lifecycle: DRAFT → CREATED → SCHEDULED → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED"
        )
)
@Configuration
public class OpenApiConfig {
}
