package com.logistics.warehouse.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.domain.DomainEvent;
import com.logistics.warehouse.domain.model.*;
import com.logistics.warehouse.domain.ports.out.WarehouseRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WarehouseJpaRepository implements WarehouseRepository {

    private final WarehouseJpaRepositoryPort jpa;
    private final OutboxJpaRepositoryPort outboxJpa;
    private final ObjectMapper objectMapper;

    public WarehouseJpaRepository(WarehouseJpaRepositoryPort jpa, OutboxJpaRepositoryPort outboxJpa, ObjectMapper objectMapper) {
        this.jpa = jpa;
        this.outboxJpa = outboxJpa;
        this.objectMapper = objectMapper;
    }

    // Writes the aggregate and its pulled domain events as outbox rows in the same
    // transaction (ADR-030) — atomic with the aggregate write since this method has
    // no @Transactional of its own and inherits the calling use case's boundary.
    @Override
    public void save(Warehouse w) {
        WarehouseJpaEntity e = toEntity(w);
        jpa.save(e);
        for (DomainEvent event : w.pullDomainEvents()) {
            outboxJpa.save(toOutboxEntity(event));
        }
    }

    private OutboxEventEntity toOutboxEntity(DomainEvent event) {
        OutboxEventEntity e = new OutboxEventEntity();
        e.aggregateId = event.aggregateId();
        e.eventType = event.getClass().getSimpleName();
        e.occurredAt = event.occurredAt();
        try {
            e.payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize domain event for outbox: " + event.getClass().getSimpleName(), ex);
        }
        return e;
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
