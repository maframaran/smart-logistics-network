package com.logistics.rag.infrastructure.messaging.dto;

/**
 * Mirrors routing-service's {@code RouteCalculated} domain event JSON shape.
 * originCity/destinationCity are not currently emitted by the producer event and
 * will deserialize as null — pre-existing gap, preserved as-is from the prior Map-based code.
 */
public record RouteCalculatedPayload(
        String shipmentId,
        String originCity,
        String destinationCity,
        String vehicleType,
        double totalDistanceKm,
        long totalDurationMinutes,
        double fuelCostBrl,
        double tollsCostBrl
) {
}
