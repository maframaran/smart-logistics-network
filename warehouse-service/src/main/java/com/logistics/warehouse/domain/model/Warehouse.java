package com.logistics.warehouse.domain.model;

import com.logistics.common.domain.AggregateRoot;
import com.logistics.warehouse.domain.events.InventoryReceived;
import com.logistics.warehouse.domain.events.InventoryDispatched;
import com.logistics.warehouse.domain.events.WarehouseCapacityUpdated;

import java.util.*;

public class Warehouse extends AggregateRoot {

    private final WarehouseId id;
    private final String name;
    private final String location;
    private final double maxWeightKg;
    private final double maxVolumeM3;

    // sku → item
    private final Map<String, InventoryItem> inventory;

    private Warehouse(WarehouseId id, String name, String location, double maxWeightKg, double maxVolumeM3,
                      Map<String, InventoryItem> inventory) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.maxWeightKg = maxWeightKg;
        this.maxVolumeM3 = maxVolumeM3;
        this.inventory = new HashMap<>(inventory);
    }

    public static Warehouse create(String name, String location, double maxWeightKg, double maxVolumeM3) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (maxWeightKg <= 0) throw new IllegalArgumentException("maxWeightKg must be positive");
        if (maxVolumeM3 <= 0) throw new IllegalArgumentException("maxVolumeM3 must be positive");
        return new Warehouse(WarehouseId.generate(), name, location, maxWeightKg, maxVolumeM3, Map.of());
    }

    public static Warehouse reconstitute(WarehouseId id, String name, String location,
                                         double maxWeightKg, double maxVolumeM3,
                                         Map<String, InventoryItem> inventory) {
        return new Warehouse(id, name, location, maxWeightKg, maxVolumeM3, inventory);
    }

    // BR-006: currentCapacity + incomingInventory <= maxCapacity
    public void receiveInventory(InventoryItem incoming) {
        double projectedWeight = currentWeightKg() + incoming.totalWeightKg();
        double projectedVolume = currentVolumeM3() + incoming.totalVolumeM3();

        if (projectedWeight > maxWeightKg) {
            throw new IllegalStateException(
                    String.format("Weight capacity exceeded: %.1f + %.1f > %.1f kg", currentWeightKg(), incoming.totalWeightKg(), maxWeightKg));
        }
        if (projectedVolume > maxVolumeM3) {
            throw new IllegalStateException(
                    String.format("Volume capacity exceeded: %.1f + %.1f > %.1f m³", currentVolumeM3(), incoming.totalVolumeM3(), maxVolumeM3));
        }

        InventoryItem existing = inventory.get(incoming.sku().value());
        if (existing != null) {
            inventory.put(incoming.sku().value(), existing.withQuantity(existing.quantity() + incoming.quantity()));
        } else {
            inventory.put(incoming.sku().value(), incoming);
        }

        registerEvent(InventoryReceived.of(id.toString(), incoming.sku().value(), incoming.quantity(),
                currentWeightKg(), currentVolumeM3(), maxWeightKg, maxVolumeM3));
        registerEvent(WarehouseCapacityUpdated.of(id.toString(), currentWeightKg(), currentVolumeM3(), maxWeightKg, maxVolumeM3));
    }

    public void dispatchInventory(Sku sku, int quantity) {
        InventoryItem item = inventory.get(sku.value());
        if (item == null) throw new IllegalArgumentException("SKU not found in warehouse: " + sku);
        if (item.quantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for SKU " + sku + ": have " + item.quantity() + ", need " + quantity);
        }

        int remaining = item.quantity() - quantity;
        if (remaining == 0) {
            inventory.remove(sku.value());
        } else {
            inventory.put(sku.value(), item.withQuantity(remaining));
        }

        registerEvent(InventoryDispatched.of(id.toString(), sku.value(), quantity,
                currentWeightKg(), currentVolumeM3(), maxWeightKg, maxVolumeM3));
        registerEvent(WarehouseCapacityUpdated.of(id.toString(), currentWeightKg(), currentVolumeM3(), maxWeightKg, maxVolumeM3));
    }

    public double currentWeightKg() {
        return inventory.values().stream().mapToDouble(InventoryItem::totalWeightKg).sum();
    }

    public double currentVolumeM3() {
        return inventory.values().stream().mapToDouble(InventoryItem::totalVolumeM3).sum();
    }

    public double availableWeightKg() { return maxWeightKg - currentWeightKg(); }
    public double availableVolumeM3() { return maxVolumeM3 - currentVolumeM3(); }

    public Optional<InventoryItem> findItem(Sku sku) {
        return Optional.ofNullable(inventory.get(sku.value()));
    }

    public WarehouseId getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public double getMaxWeightKg() { return maxWeightKg; }
    public double getMaxVolumeM3() { return maxVolumeM3; }
    public Map<String, InventoryItem> getInventory() { return Collections.unmodifiableMap(inventory); }
}
