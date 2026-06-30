package com.logistics.notification.infrastructure.messaging.dto;

/**
 * Mirrors the documented {@code shipment.delivered} topic schema (consumed fields only).
 * No producer publishes this event yet — shipment-service has no ShipmentDelivered domain
 * event or deliver() lifecycle method, so this listener is currently never invoked in
 * production. See specs-documentation/messaging/topics/shipment.delivered.md and ADR-026.
 */
public record ShipmentDeliveredPayload(String shipperId) {
}
