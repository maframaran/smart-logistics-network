package com.logistics.rag.domain.model;

/** Metadata indexed alongside a route's embedding in {@code rag.route_embeddings}. */
public record RouteMetadata(
        String shipmentId,
        String originCity,
        String destinationCity,
        String vehicleType,
        String slaType,
        double distanceKm,
        long durationMinutes,
        double fuelCostBrl,
        double tollCostBrl,
        double totalCostBrl
) {
}
