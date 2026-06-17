package com.logistics.rag.domain.ports.out;

import com.fasterxml.jackson.databind.JsonNode;

public interface LlmPort {
    /** Sends prompt + context to the LLM and returns parsed JSON from tool_use response. */
    JsonNode complete(String systemPrompt, String userMessage, String toolName, String toolDescription, String toolInputSchema);
}
