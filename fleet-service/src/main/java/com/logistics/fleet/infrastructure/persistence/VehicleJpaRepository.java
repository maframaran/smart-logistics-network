package com.logistics.fleet.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.domain.DomainEvent;
import com.logistics.fleet.domain.model.*;
import com.logistics.fleet.domain.ports.out.VehicleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VehicleJpaRepository implements VehicleRepository {

    private final VehicleJpaRepositoryPort jpa;
    private final OutboxJpaRepositoryPort outboxJpa;
    private final ObjectMapper objectMapper;

    public VehicleJpaRepository(VehicleJpaRepositoryPort jpa, OutboxJpaRepositoryPort outboxJpa, ObjectMapper objectMapper) {
        this.jpa = jpa;
        this.outboxJpa = outboxJpa;
        this.objectMapper = objectMapper;
    }

    // Writes the aggregate and its pulled domain events as outbox rows in the same
    // transaction (ADR-030) — atomic with the aggregate write since this method has
    // no @Transactional of its own and inherits the calling use case's boundary.
    @Override
    public void save(Vehicle v) {
        jpa.save(toEntity(v));
        for (DomainEvent event : v.pullDomainEvents()) {
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
    public Optional<Vehicle> findById(VehicleId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Vehicle> findByStatus(VehicleStatus status) {
        return jpa.findByStatus(status.name()).stream().map(this::toDomain).toList();
    }

    private VehicleJpaEntity toEntity(Vehicle v) {
        VehicleJpaEntity e = new VehicleJpaEntity();
        e.id = v.getId().value();
        e.licensePlate = v.getLicensePlate();
        e.type = v.getType().name();
        e.maxWeightKg = v.getCapacity().maxWeightKg();
        e.maxVolumeM3 = v.getCapacity().maxVolumeM3();
        e.carrierId = v.getCarrierId();
        e.status = v.getStatus().name();
        return e;
    }

    private Vehicle toDomain(VehicleJpaEntity e) {
        return Vehicle.reconstitute(
                new VehicleId(e.id),
                e.licensePlate,
                VehicleType.valueOf(e.type),
                new Capacity(e.maxWeightKg, e.maxVolumeM3),
                e.carrierId,
                VehicleStatus.valueOf(e.status)
        );
    }
}
