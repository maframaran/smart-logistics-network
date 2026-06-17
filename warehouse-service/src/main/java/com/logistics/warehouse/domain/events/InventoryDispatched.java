package com.logistics.warehouse.domain.events;

import com.logistics.common.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record InventoryDispatched(
        UUID eventId, Instant occurredAt, String aggregateId,
        String sku, int quantity,
        double currentWeightKg, double currentVolumeM3,
        double maxWeightKg, double maxVolumeM3
) implements DomainEvent {

    public static InventoryDispatched of(String warehouseId, String sku, int quantity,
                                         double currentWeight, double currentVolume,
                                         double maxWeight, double maxVolume) {
        return new InventoryDispatched(UUID.randomUUID(), Instant.now(), warehouseId,
                sku, quantity, currentWeight, currentVolume, maxWeight, maxVolume);
    }
}
