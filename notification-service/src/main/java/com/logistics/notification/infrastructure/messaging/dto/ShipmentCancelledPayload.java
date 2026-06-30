package com.logistics.notification.infrastructure.messaging.dto;

/** Mirrors shipment-service's {@code ShipmentCancelled} domain event JSON shape (consumed fields only). */
public record ShipmentCancelledPayload(String reason) {
}
