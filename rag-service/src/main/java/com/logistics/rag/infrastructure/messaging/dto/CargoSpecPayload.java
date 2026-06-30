package com.logistics.rag.infrastructure.messaging.dto;

/** Mirrors shipment-service's {@code CargoSpec} value object JSON shape. */
public record CargoSpecPayload(
        double weightKg,
        double volumeM3,
        boolean requiresHazmat,
        boolean requiresColdChain
) {
}
