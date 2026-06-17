package com.logistics.routing.domain.events;

import com.logistics.common.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record RouteCalculated(
        UUID eventId,
        Instant occurredAt,
        String aggregateId,
        String shipmentId,
        String vehicleType,
        double totalDistanceKm,
        long totalDurationMinutes,
        Instant estimatedArrival,
        double fuelLitres,
        double fuelCostBrl,
        double tollsCostBrl
) implements DomainEvent {

    public static RouteCalculated of(String routeId, String shipmentId, String vehicleType,
                                      double distanceKm, long durationMin, Instant eta,
                                      double fuelLitres, double fuelCost, double tolls) {
        return new RouteCalculated(UUID.randomUUID(), Instant.now(), routeId,
                shipmentId, vehicleType, distanceKm, durationMin, eta, fuelLitres, fuelCost, tolls);
    }
}
