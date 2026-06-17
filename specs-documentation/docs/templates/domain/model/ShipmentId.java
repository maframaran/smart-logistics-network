package com.logistics.shipment.domain.model;

import java.util.UUID;

// Value Object — spec: architecture/domains.md § Shipment Domain
// Wraps UUID to give the identifier a meaningful type in the domain.
public record ShipmentId(UUID value) {

    public ShipmentId {
        if (value == null) throw new IllegalArgumentException("ShipmentId cannot be null");
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
