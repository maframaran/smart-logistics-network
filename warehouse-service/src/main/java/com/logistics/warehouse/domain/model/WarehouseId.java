package com.logistics.warehouse.domain.model;

import java.util.UUID;

public record WarehouseId(UUID value) {
    public WarehouseId { if (value == null) throw new IllegalArgumentException("WarehouseId must not be null"); }
    public static WarehouseId generate() { return new WarehouseId(UUID.randomUUID()); }
    public static WarehouseId of(String uuid) { return new WarehouseId(UUID.fromString(uuid)); }
    @Override public String toString() { return value.toString(); }
}
