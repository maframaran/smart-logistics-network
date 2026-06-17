package com.logistics.rag.domain.model;

import java.util.List;

public record RouteSearchResult(
        double estimatedCostBrl,
        long estimatedDurationMinutes,
        List<RouteComparable> comparables,
        boolean lowConfidence
) {
    public record RouteComparable(
            String routeId,
            String shipmentId,
            double costBrl,
            long durationMinutes,
            double similarity
    ) {}
}
