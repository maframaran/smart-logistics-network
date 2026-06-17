package com.logistics.rag.infrastructure.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.anthropic")
public record AnthropicProperties(
        String apiKey,
        String baseUrl,
        String embeddingModel,
        String completionModel,
        String apiVersion
) {}
