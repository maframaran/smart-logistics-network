package com.logistics.warehouse.domain.model;

import java.util.UUID;

public record InventoryItemId(UUID value) {
    public InventoryItemId { if (value == null) throw new IllegalArgumentException("InventoryItemId must not be null"); }
    public static InventoryItemId generate() { return new InventoryItemId(UUID.randomUUID()); }
    public static InventoryItemId of(String uuid) { return new InventoryItemId(UUID.fromString(uuid)); }
    @Override public String toString() { return value.toString(); }
}
