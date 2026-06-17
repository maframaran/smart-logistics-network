package com.logistics.fleet.domain.model;

import java.util.UUID;

public record VehicleId(UUID value) {

    public VehicleId {
        if (value == null) throw new IllegalArgumentException("VehicleId must not be null");
    }

    public static VehicleId generate() { return new VehicleId(UUID.randomUUID()); }
    public static VehicleId of(String uuid) { return new VehicleId(UUID.fromString(uuid)); }

    @Override
    public String toString() { return value.toString(); }
}
