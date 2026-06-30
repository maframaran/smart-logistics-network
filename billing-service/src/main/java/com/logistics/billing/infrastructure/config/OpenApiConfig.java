package com.logistics.billing.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Billing Service API",
                version = "v1",
                description = "Invoicing, SLA penalties (BRL 50/150/300/day), carrier payments"
        )
)
@Configuration
public class OpenApiConfig {
}
