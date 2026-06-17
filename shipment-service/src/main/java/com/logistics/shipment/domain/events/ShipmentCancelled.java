package com.logistics.shipment.domain.events;

import com.logistics.common.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ShipmentCancelled(
        UUID eventId,
        Instant occurredAt,
        String aggregateId,
        String reason,
        boolean cancellationFeeApplied
) implements DomainEvent {

    public static ShipmentCancelled of(String shipmentId, String reason, boolean cancellationFeeApplied) {
        return new ShipmentCancelled(UUID.randomUUID(), Instant.now(), shipmentId, reason, cancellationFeeApplied);
    }
}
