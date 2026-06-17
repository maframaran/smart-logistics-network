package com.logistics.warehouse.domain;

import com.logistics.warehouse.domain.events.InventoryReceived;
import com.logistics.warehouse.domain.events.WarehouseCapacityUpdated;
import com.logistics.warehouse.domain.model.*;
import com.logistics.common.domain.DomainEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class WarehouseTest {

    private Warehouse warehouse() {
        return Warehouse.create("Main Depot", "São Paulo, SP", 10_000.0, 500.0);
    }

    private InventoryItem item(String sku, double weightKg, double volumeM3, int qty) {
        return new InventoryItem(InventoryItemId.generate(), new Sku(sku), "desc", weightKg, volumeM3, qty);
    }

    @Test
    void receiveInventory_withinCapacity_succeeds() {
        Warehouse w = warehouse();
        w.receiveInventory(item("SKU-001", 100.0, 5.0, 10));

        assertThat(w.currentWeightKg()).isEqualTo(1000.0);
        assertThat(w.currentVolumeM3()).isEqualTo(50.0);

        List<DomainEvent> events = w.pullDomainEvents();
        assertThat(events).hasSize(2);
        assertThat(events.get(0)).isInstanceOf(InventoryReceived.class);
        assertThat(events.get(1)).isInstanceOf(WarehouseCapacityUpdated.class);
    }

    @Test
    void receiveInventory_exceedsWeightCapacity_throws() {
        Warehouse w = warehouse();
        assertThatThrownBy(() -> w.receiveInventory(item("SKU-HEAVY", 10_001.0, 1.0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Weight capacity exceeded");
    }

    @Test
    void receiveInventory_exceedsVolumeCapacity_throws() {
        Warehouse w = warehouse();
        assertThatThrownBy(() -> w.receiveInventory(item("SKU-BIG", 1.0, 501.0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Volume capacity exceeded");
    }

    @Test
    void receiveInventory_sameSku_accumulatesQuantity() {
        Warehouse w = warehouse();
        w.receiveInventory(item("SKU-A", 10.0, 1.0, 5));
        w.pullDomainEvents();
        w.receiveInventory(item("SKU-A", 10.0, 1.0, 3));

        assertThat(w.findItem(new Sku("SKU-A"))).isPresent();
        assertThat(w.findItem(new Sku("SKU-A")).get().quantity()).isEqualTo(8);
    }

    @Test
    void dispatchInventory_reducesStock() {
        Warehouse w = warehouse();
        w.receiveInventory(item("SKU-B", 50.0, 2.0, 10));
        w.pullDomainEvents();

        w.dispatchInventory(new Sku("SKU-B"), 4);

        assertThat(w.findItem(new Sku("SKU-B")).get().quantity()).isEqualTo(6);
    }

    @Test
    void dispatchInventory_allUnits_removesItem() {
        Warehouse w = warehouse();
        w.receiveInventory(item("SKU-C", 50.0, 2.0, 5));
        w.pullDomainEvents();

        w.dispatchInventory(new Sku("SKU-C"), 5);

        assertThat(w.findItem(new Sku("SKU-C"))).isEmpty();
    }

    @Test
    void dispatchInventory_moreThanStock_throws() {
        Warehouse w = warehouse();
        w.receiveInventory(item("SKU-D", 10.0, 1.0, 2));

        assertThatThrownBy(() -> w.dispatchInventory(new Sku("SKU-D"), 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void dispatchInventory_unknownSku_throws() {
        Warehouse w = warehouse();
        assertThatThrownBy(() -> w.dispatchInventory(new Sku("MISSING"), 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU not found");
    }

    @Test
    void availableCapacity_reflectsCurrentUsage() {
        Warehouse w = warehouse();
        w.receiveInventory(item("SKU-E", 100.0, 10.0, 10)); // 1000kg, 100m3
        w.pullDomainEvents();

        assertThat(w.availableWeightKg()).isEqualTo(9_000.0);
        assertThat(w.availableVolumeM3()).isEqualTo(400.0);
    }

    @Test
    void create_withBlankName_throws() {
        assertThatThrownBy(() -> Warehouse.create("", "location", 1000, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }
}
