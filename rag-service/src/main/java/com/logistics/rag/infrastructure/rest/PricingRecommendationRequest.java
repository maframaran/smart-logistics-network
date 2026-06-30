package com.logistics.rag.infrastructure.rest;

public record PricingRecommendationRequest(
        String originCity,
        String destinationCity,
        double weightKg,
        String slaType,
        Integer warehouseUtilizationPct
) {
}
