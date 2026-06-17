package com.logistics.rag.domain.ports.out;

public interface EmbeddingPort {
    float[] embed(String text);
}
