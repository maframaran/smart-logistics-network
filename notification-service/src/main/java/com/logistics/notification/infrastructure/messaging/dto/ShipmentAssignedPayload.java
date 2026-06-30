package com.logistics.notification.infrastructure.messaging.dto;

/** Mirrors shipment-service's {@code ShipmentAssigned} domain event JSON shape (consumed fields only). */
public record ShipmentAssignedPayload(String driverId) {
}
