package com.logistics.warehouse.domain.events;

import com.logistics.common.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record WarehouseCapacityUpdated(
        UUID eventId, Instant occurredAt, String aggregateId,
        double currentWeightKg, double currentVolumeM3,
        double maxWeightKg, double maxVolumeM3,
        double utilisationPct
) implements DomainEvent {

    public static WarehouseCapacityUpdated of(String warehouseId,
                                               double currentWeight, double currentVolume,
                                               double maxWeight, double maxVolume) {
        double utilisation = maxWeight > 0 ? currentWeight / maxWeight * 100.0 : 0.0;
        return new WarehouseCapacityUpdated(UUID.randomUUID(), Instant.now(), warehouseId,
                currentWeight, currentVolume, maxWeight, maxVolume, utilisation);
    }
}
