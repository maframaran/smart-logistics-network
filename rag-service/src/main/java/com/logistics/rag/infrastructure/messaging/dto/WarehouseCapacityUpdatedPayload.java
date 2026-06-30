package com.logistics.rag.infrastructure.messaging.dto;

/**
 * Mirrors warehouse-service's {@code WarehouseCapacityUpdated} domain event JSON shape.
 * warehouseName/location are not currently emitted by the producer event and will
 * deserialize as null — pre-existing gap, preserved as-is from the prior Map-based code.
 */
public record WarehouseCapacityUpdatedPayload(
        String warehouseName,
        String location,
        double maxWeightKg,
        double maxVolumeM3,
        double currentWeightKg,
        double currentVolumeM3,
        double utilisationPct
) {
}
