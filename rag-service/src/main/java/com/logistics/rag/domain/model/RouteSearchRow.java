package com.logistics.rag.domain.model;

/** A row returned by {@code findSimilarRoutes} ANN search, including cosine similarity. */
public record RouteSearchRow(
        String routeId,
        String shipmentId,
        String originCity,
        String destinationCity,
        String vehicleType,
        String slaType,
        double distanceKm,
        long durationMinutes,
        double fuelCostBrl,
        double tollCostBrl,
        double totalCostBrl,
        double similarity
) {
}
