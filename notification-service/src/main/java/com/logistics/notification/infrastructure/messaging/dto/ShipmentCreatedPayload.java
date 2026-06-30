package com.logistics.notification.infrastructure.messaging.dto;

/** Mirrors shipment-service's {@code ShipmentCreated} domain event JSON shape (consumed fields only). */
public record ShipmentCreatedPayload(String shipperId) {
}
