package com.logistics.shipment.domain.model;

import java.util.UUID;

public record ShipmentId(UUID value) {

    public ShipmentId {
        if (value == null) throw new IllegalArgumentException("ShipmentId value must not be null");
    }

    public static ShipmentId generate() {
        return new ShipmentId(UUID.randomUUID());
    }

    public static ShipmentId of(String uuid) {
        return new ShipmentId(UUID.fromString(uuid));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
