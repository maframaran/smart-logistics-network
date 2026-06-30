package com.logistics.rag.domain.model;

/** A row returned by {@code findAllInventory}. */
public record InventoryRow(
        String warehouseId,
        String warehouseName,
        String location,
        double maxWeightKg,
        double maxVolumeM3,
        double currentWeightKg,
        double currentVolumeM3,
        double utilisationPct
) {
}
