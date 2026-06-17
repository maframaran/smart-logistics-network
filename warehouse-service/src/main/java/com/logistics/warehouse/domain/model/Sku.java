package com.logistics.warehouse.domain.model;

public record Sku(String value) {
    public Sku {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("SKU must not be blank");
    }
    @Override public String toString() { return value; }
}
