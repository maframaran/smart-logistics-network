package com.logistics.rag.domain.model;

public record PriceRecommendation(
        double suggestedPriceBrl,
        double lowerBound,
        double upperBound,
        double confidencePct,
        int comparables
) {}
