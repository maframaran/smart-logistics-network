package com.logistics.rag.domain.model;

/** Metadata indexed alongside a warehouse's embedding in {@code rag.inventory_embeddings}. */
public record InventoryMetadata(
        String warehouseName,
        String location,
        double maxWeightKg,
        double maxVolumeM3,
        double currentWeightKg,
        double currentVolumeM3,
        double utilisationPct
) {
}
