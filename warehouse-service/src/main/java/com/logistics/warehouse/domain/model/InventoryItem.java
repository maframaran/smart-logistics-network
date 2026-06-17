package com.logistics.warehouse.domain.model;

public record InventoryItem(
        InventoryItemId id,
        Sku sku,
        String description,
        double weightKg,
        double volumeM3,
        int quantity
) {
    public InventoryItem {
        if (weightKg <= 0) throw new IllegalArgumentException("weightKg must be positive");
        if (volumeM3 <= 0) throw new IllegalArgumentException("volumeM3 must be positive");
        if (quantity < 0) throw new IllegalArgumentException("quantity must not be negative");
    }

    public double totalWeightKg() { return weightKg * quantity; }
    public double totalVolumeM3() { return volumeM3 * quantity; }

    public InventoryItem withQuantity(int newQuantity) {
        return new InventoryItem(id, sku, description, weightKg, volumeM3, newQuantity);
    }
}
