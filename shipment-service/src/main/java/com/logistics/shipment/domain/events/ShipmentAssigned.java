package com.logistics.shipment.domain.events;

import com.logistics.common.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ShipmentAssigned(
        UUID eventId,
        Instant occurredAt,
        String aggregateId,
        String vehicleId,
        String driverId,
        String routeId
) implements DomainEvent {

    public static ShipmentAssigned of(String shipmentId, String vehicleId, String driverId, String routeId) {
        return new ShipmentAssigned(UUID.randomUUID(), Instant.now(), shipmentId, vehicleId, driverId, routeId);
    }
}
