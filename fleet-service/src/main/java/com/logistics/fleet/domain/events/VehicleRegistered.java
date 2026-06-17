package com.logistics.fleet.domain.events;

import com.logistics.common.domain.DomainEvent;
import com.logistics.fleet.domain.model.Capacity;
import com.logistics.fleet.domain.model.VehicleType;

import java.time.Instant;
import java.util.UUID;

public record VehicleRegistered(
        UUID eventId,
        Instant occurredAt,
        String aggregateId,
        String licensePlate,
        VehicleType vehicleType,
        Capacity capacity,
        String carrierId
) implements DomainEvent {

    public static VehicleRegistered of(String vehicleId, String licensePlate, VehicleType type, Capacity capacity, String carrierId) {
        return new VehicleRegistered(UUID.randomUUID(), Instant.now(), vehicleId, licensePlate, type, capacity, carrierId);
    }
}
