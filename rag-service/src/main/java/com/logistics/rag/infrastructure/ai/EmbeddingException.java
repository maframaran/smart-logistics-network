package com.logistics.rag.infrastructure.ai;

/** Raised when producing or parsing an embedding fails. Callers fall back to a hash-based pseudo-embedding. */
public class EmbeddingException extends RuntimeException {
    public EmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }
}
