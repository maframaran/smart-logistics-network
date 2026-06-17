package com.logistics.rag.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.rag.domain.ports.out.EmbeddingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.security.MessageDigest;
import java.util.Map;

/**
 * Produces 1536-dim embeddings via the Anthropic messages API (tool_use).
 * Falls back to a deterministic hash-based pseudo-embedding when ANTHROPIC_API_KEY
 * is absent (dev/test mode). In production, replace this with a dedicated embedding
 * service (e.g., voyage-2, text-embedding-3-small) once Anthropic releases one.
 */
@Component
public class ClaudeEmbeddingAdapter implements EmbeddingPort {

    private static final Logger log = LoggerFactory.getLogger(ClaudeEmbeddingAdapter.class);
    private static final int DIMENSIONS = 1536;

    private final RestClient restClient;
    private final AnthropicProperties props;
    private final ObjectMapper objectMapper;

    public ClaudeEmbeddingAdapter(AnthropicProperties props, ObjectMapper objectMapper) {
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
    public float[] embed(String text) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            log.warn("ANTHROPIC_API_KEY not set — using hash-based pseudo-embedding (dev mode)");
            return hashEmbedding(text);
        }
        try {
            return callClaudeEmbedding(text);
        } catch (Exception e) {
            log.error("Claude embedding call failed, falling back to hash embedding: {}", e.getMessage());
            return hashEmbedding(text);
        }
    }

    private float[] callClaudeEmbedding(String text) throws Exception {
        String toolSchema = """
                {
                  "type": "object",
                  "properties": {
                    "embedding": {
                      "type": "array",
                      "items": { "type": "number" },
                      "description": "1536-dimensional embedding vector"
                    }
                  },
                  "required": ["embedding"]
                }
                """;

        Map<String, Object> request = Map.of(
                "model", props.embeddingModel(),
                "max_tokens", 4096,
                "tools", new Object[]{Map.of(
                        "name", "produce_embedding",
                        "description", "Produce a 1536-dimensional semantic embedding vector (floats in [-1,1]) for the given text.",
                        "input_schema", objectMapper.readTree(toolSchema)
                )},
                "tool_choice", Map.of("type", "tool", "name", "produce_embedding"),
                "messages", new Object[]{Map.of(
                        "role", "user",
                        "content", "Produce a semantic embedding for:\n\n" + text
                )}
        );

        String responseBody = restClient.post()
                .uri("/v1/messages")
                .body(request)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode inputNode = root.path("content").get(0).path("input").path("embedding");
        float[] result = new float[DIMENSIONS];
        int size = Math.min(inputNode.size(), DIMENSIONS);
        for (int i = 0; i < size; i++) {
            result[i] = (float) inputNode.get(i).asDouble();
        }
        return result;
    }

    /** Deterministic pseudo-embedding from SHA-256 hash. For dev/test only. */
    private float[] hashEmbedding(String text) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            float[] result = new float[DIMENSIONS];
            byte[] seed = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            for (int block = 0; block < DIMENSIONS / 32; block++) {
                byte[] input = new byte[seed.length + 4];
                System.arraycopy(seed, 0, input, 0, seed.length);
                input[seed.length]     = (byte) (block >> 24);
                input[seed.length + 1] = (byte) (block >> 16);
                input[seed.length + 2] = (byte) (block >> 8);
                input[seed.length + 3] = (byte) block;
                byte[] hash = sha.digest(input);
                for (int i = 0; i < 32; i++) {
                    result[block * 32 + i] = hash[i] / 128.0f;
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Hash embedding failed", e);
        }
    }
}
