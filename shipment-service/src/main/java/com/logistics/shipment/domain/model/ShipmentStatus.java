package com.logistics.shipment.domain.model;

public enum ShipmentStatus {
    DRAFT,
    CREATED,
    SCHEDULED,
    ASSIGNED,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}
