package com.logistics.driver.domain.model;

import java.util.UUID;

public record DriverId(UUID value) {

    public DriverId {
        if (value == null) throw new IllegalArgumentException("DriverId must not be null");
    }

    public static DriverId generate() { return new DriverId(UUID.randomUUID()); }
    public static DriverId of(String uuid) { return new DriverId(UUID.fromString(uuid)); }

    @Override
    public String toString() { return value.toString(); }
}
