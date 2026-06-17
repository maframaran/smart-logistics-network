package com.logistics.warehouse.infrastructure.persistence;

import com.logistics.warehouse.domain.model.*;
import com.logistics.warehouse.domain.ports.out.WarehouseRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WarehouseJpaRepository implements WarehouseRepository {

    private final WarehouseJpaRepositoryPort jpa;

    public WarehouseJpaRepository(WarehouseJpaRepositoryPort jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Warehouse w) {
        WarehouseJpaEntity e = toEntity(w);
        jpa.save(e);
    }

    @Override
    public Optional<Warehouse> findById(WarehouseId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Warehouse> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    private WarehouseJpaEntity toEntity(Warehouse w) {
        WarehouseJpaEntity e = new WarehouseJpaEntity();
        e.id = w.getId().value();
        e.name = w.getName();
        e.location = w.getLocation();
        e.maxWeightKg = w.getMaxWeightKg();
        e.maxVolumeM3 = w.getMaxVolumeM3();
        e.items = new ArrayList<>();
        w.getInventory().values().forEach(item -> {
            InventoryItemJpaEntity ie = new InventoryItemJpaEntity();
            ie.id = item.id().value();
            ie.warehouseId = w.getId().value();
            ie.sku = item.sku().value();
            ie.description = item.description();
            ie.weightKg = item.weightKg();
            ie.volumeM3 = item.volumeM3();
            ie.quantity = item.quantity();
            e.items.add(ie);
        });
        return e;
    }

    private Warehouse toDomain(WarehouseJpaEntity e) {
        Map<String, InventoryItem> inventory = new HashMap<>();
        e.items.forEach(ie -> inventory.put(ie.sku,
                new InventoryItem(new InventoryItemId(ie.id), new Sku(ie.sku), ie.description, ie.weightKg, ie.volumeM3, ie.quantity)));
        return Warehouse.reconstitute(new WarehouseId(e.id), e.name, e.location, e.maxWeightKg, e.maxVolumeM3, inventory);
    }
}
