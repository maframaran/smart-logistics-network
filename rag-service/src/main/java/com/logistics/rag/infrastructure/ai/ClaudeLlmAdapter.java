package com.logistics.rag.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.rag.domain.ports.out.LlmPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class ClaudeLlmAdapter implements LlmPort {

    private static final Logger log = LoggerFactory.getLogger(ClaudeLlmAdapter.class);

    private final RestClient restClient;
    private final AnthropicProperties props;
    private final ObjectMapper objectMapper;

    public ClaudeLlmAdapter(AnthropicProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader("x-api-key", props.apiKey() != null ? props.apiKey() : "")
                .defaultHeader("anthropic-version", props.apiVersion())
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public JsonNode complete(String systemPrompt, String userMessage,
                              String toolName, String toolDescription, String toolInputSchema) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            log.warn("ANTHROPIC_API_KEY not set — returning empty LLM response (dev mode)");
            return objectMapper.createObjectNode();
        }
        try {
            Map<String, Object> request = Map.of(
                    "model", props.completionModel(),
                    "max_tokens", 2048,
                    "system", systemPrompt,
                    "tools", new Object[]{Map.of(
                            "name", toolName,
                            "description", toolDescription,
                            "input_schema", objectMapper.readTree(toolInputSchema)
                    )},
                    "tool_choice", Map.of("type", "tool", "name", toolName),
                    "messages", new Object[]{Map.of("role", "user", "content", userMessage)}
            );

            String responseBody = restClient.post()
                    .uri("/v1/messages")
                    .body(request)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("content").get(0).path("input");
        } catch (Exception e) {
            log.error("Claude LLM call failed: {}", e.getMessage());
            return objectMapper.createObjectNode();
        }
    }
}
