package com.logistics.rag.infrastructure.messaging.dto;

/**
 * Mirrors shipment-service's {@code ShipmentCreated} domain event JSON shape.
 * The producer's nested cargo field is actually named "cargoSpec", not "cargo" — this
 * mismatch already existed in the prior Map-based code (cargo always deserialized as
 * null there too) and is preserved as-is rather than silently fixed here.
 */
public record ShipmentCreatedPayload(
        String shipperId,
        String slaType,
        AddressPayload origin,
        AddressPayload destination,
        CargoSpecPayload cargo
) {
}
