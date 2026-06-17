package com.logistics.fleet.domain.events;

import com.logistics.common.domain.DomainEvent;
import com.logistics.fleet.domain.model.VehicleStatus;

import java.time.Instant;
import java.util.UUID;

public record VehicleStatusChanged(
        UUID eventId,
        Instant occurredAt,
        String aggregateId,
        VehicleStatus previousStatus,
        VehicleStatus newStatus,
        String reason
) implements DomainEvent {

    public static VehicleStatusChanged of(String vehicleId, VehicleStatus from, VehicleStatus to, String reason) {
        return new VehicleStatusChanged(UUID.randomUUID(), Instant.now(), vehicleId, from, to, reason);
    }
}
