package com.logistics.shipment.domain.events;

import com.logistics.shipment.domain.model.*;

import java.time.Instant;
import java.util.UUID;

// Domain Event — spec: messaging/topics/shipment.created.md
// All fields match the Kafka payload schema defined in the topic descriptor.
// This record is immutable by definition — never modify after construction.
public record ShipmentCreated(
        UUID eventId,           // unique event id for idempotency
        int eventVersion,       // schema version — current: 1
        ShipmentId shipmentId,
        Address origin,
        Address destination,
        CargoSpec cargoSpec,
        SlaType slaType,
        Instant requiredDeliveryDate,
        Instant occurredAt
) {
    // Convenience constructor used by the aggregate — eventId and eventVersion managed here
    public ShipmentCreated(ShipmentId shipmentId, Address origin, Address destination,
                           CargoSpec cargoSpec, SlaType slaType,
                           Instant requiredDeliveryDate, Instant occurredAt) {
        this(UUID.randomUUID(), 1, shipmentId, origin, destination,
             cargoSpec, slaType, requiredDeliveryDate, occurredAt);
    }
}
