package com.logistics.driver.domain.events;

import com.logistics.common.domain.DomainEvent;
import com.logistics.driver.domain.model.DriverStatus;

import java.time.Instant;
import java.util.UUID;

public record DriverStatusChanged(
        UUID eventId,
        Instant occurredAt,
        String aggregateId,
        DriverStatus previousStatus,
        DriverStatus newStatus,
        String reason
) implements DomainEvent {

    public static DriverStatusChanged of(String driverId, DriverStatus from, DriverStatus to, String reason) {
        return new DriverStatusChanged(UUID.randomUUID(), Instant.now(), driverId, from, to, reason);
    }
}
