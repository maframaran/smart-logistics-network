package com.logistics.shipment.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.domain.DomainEvent;
import com.logistics.shipment.domain.model.*;
import com.logistics.shipment.domain.ports.out.ShipmentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ShipmentJpaRepository implements ShipmentRepository {

    private final ShipmentJpaRepositoryPort jpa;
    private final OutboxJpaRepositoryPort outboxJpa;
    private final ObjectMapper objectMapper;

    public ShipmentJpaRepository(ShipmentJpaRepositoryPort jpa, OutboxJpaRepositoryPort outboxJpa, ObjectMapper objectMapper) {
        this.jpa = jpa;
        this.outboxJpa = outboxJpa;
        this.objectMapper = objectMapper;
    }

    // Writes the aggregate and its pulled domain events as outbox rows in the same
    // transaction (ADR-030) — atomic with the aggregate write since this method has
    // no @Transactional of its own and inherits the calling use case's boundary.
    @Override
    public void save(Shipment shipment) {
        jpa.save(toEntity(shipment));
        for (DomainEvent event : shipment.pullDomainEvents()) {
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
    public Optional<Shipment> findById(ShipmentId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Shipment> findByStatus(ShipmentStatus status) {
        List<ShipmentJpaEntity> entities = status == null
                ? jpa.findAll()
                : jpa.findByStatus(status.name());
        return entities.stream().map(this::toDomain).toList();
    }

    private ShipmentJpaEntity toEntity(Shipment s) {
        ShipmentJpaEntity e = new ShipmentJpaEntity();
        e.id = s.getId().value();
        e.shipperId = s.getShipperId();
        e.originStreet = s.getOrigin().street();
        e.originCity = s.getOrigin().city();
        e.originState = s.getOrigin().state();
        e.originPostalCode = s.getOrigin().postalCode();
        e.originCountry = s.getOrigin().country();
        e.originLatitude = s.getOrigin().latitude();
        e.originLongitude = s.getOrigin().longitude();
        e.destinationStreet = s.getDestination().street();
        e.destinationCity = s.getDestination().city();
        e.destinationState = s.getDestination().state();
        e.destinationPostalCode = s.getDestination().postalCode();
        e.destinationCountry = s.getDestination().country();
        e.destinationLatitude = s.getDestination().latitude();
        e.destinationLongitude = s.getDestination().longitude();
        e.weightKg = s.getCargoSpec().weightKg();
        e.volumeM3 = s.getCargoSpec().volumeM3();
        e.requiresHazmat = s.getCargoSpec().requiresHazmat();
        e.requiresColdChain = s.getCargoSpec().requiresColdChain();
        e.slaType = s.getSlaType().name();
        e.status = s.getStatus().name();
        e.requiredDeliveryDate = s.getRequiredDeliveryDate();
        e.assignedVehicleId = s.getAssignedVehicleId();
        e.assignedDriverId = s.getAssignedDriverId();
        e.routeId = s.getRouteId();
        return e;
    }

    private Shipment toDomain(ShipmentJpaEntity e) {
        return Shipment.reconstitute(
                new ShipmentId(e.id),
                e.shipperId,
                new Address(e.originStreet, e.originCity, e.originState, e.originPostalCode, e.originCountry, e.originLatitude, e.originLongitude),
                new Address(e.destinationStreet, e.destinationCity, e.destinationState, e.destinationPostalCode, e.destinationCountry, e.destinationLatitude, e.destinationLongitude),
                new CargoSpec(e.weightKg, e.volumeM3, e.requiresHazmat, e.requiresColdChain),
                SlaType.valueOf(e.slaType),
                e.requiredDeliveryDate,
                ShipmentStatus.valueOf(e.status),
                e.assignedVehicleId,
                e.assignedDriverId,
                e.routeId
        );
    }
}
