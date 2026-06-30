package com.logistics.routing.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.domain.DomainEvent;
import com.logistics.routing.domain.model.*;
import com.logistics.routing.domain.ports.out.RouteRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RouteJpaRepository implements RouteRepository {

    private final RouteJpaRepositoryPort jpa;
    private final OutboxJpaRepositoryPort outboxJpa;
    private final ObjectMapper objectMapper;

    public RouteJpaRepository(RouteJpaRepositoryPort jpa, OutboxJpaRepositoryPort outboxJpa, ObjectMapper objectMapper) {
        this.jpa = jpa;
        this.outboxJpa = outboxJpa;
        this.objectMapper = objectMapper;
    }

    // Writes the aggregate and its pulled domain events as outbox rows in the same
    // transaction (ADR-030) — atomic with the aggregate write since this method has
    // no @Transactional of its own and inherits the calling use case's boundary.
    @Override
    public void save(Route route) {
        jpa.save(toEntity(route));
        for (DomainEvent event : route.pullDomainEvents()) {
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
    public Optional<Route> findById(RouteId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Route> findByShipmentId(String shipmentId) {
        return jpa.findByShipmentId(shipmentId).map(this::toDomain);
    }

    private RouteJpaEntity toEntity(Route r) {
        RouteJpaEntity e = new RouteJpaEntity();
        e.id = r.getId().value();
        e.shipmentId = r.getShipmentId();
        e.vehicleType = r.getVehicleType();
        e.originLatitude = r.getOrigin().latitude();
        e.originLongitude = r.getOrigin().longitude();
        e.destinationLatitude = r.getDestination().latitude();
        e.destinationLongitude = r.getDestination().longitude();
        e.totalDistanceKm = r.getTotalDistanceKm();
        e.totalDurationMinutes = r.getTotalDurationMinutes();
        e.estimatedArrival = r.getEstimatedArrival();
        e.fuelLitres = r.getFuelEstimate().litres();
        e.fuelCostBrl = r.getFuelEstimate().costBrl();
        e.tollsCostBrl = r.getTollsCostBrl();
        e.status = r.getStatus().name();
        e.segments = new ArrayList<>();
        r.getSegments().forEach(s -> {
            RouteSegmentJpaEntity se = new RouteSegmentJpaEntity();
            se.routeId = r.getId().value();
            se.segmentOrder = s.order();
            se.label = s.label();
            se.fromLatitude = s.from().latitude();
            se.fromLongitude = s.from().longitude();
            se.toLatitude = s.to().latitude();
            se.toLongitude = s.to().longitude();
            se.distanceKm = s.distanceKm();
            se.estimatedDurationMinutes = s.estimatedDurationMinutes();
            e.segments.add(se);
        });
        return e;
    }

    private Route toDomain(RouteJpaEntity e) {
        List<RouteSegment> segments = e.segments.stream()
                .map(s -> new RouteSegment(s.segmentOrder, s.label,
                        new Coordinates(s.fromLatitude, s.fromLongitude),
                        new Coordinates(s.toLatitude, s.toLongitude),
                        s.distanceKm, s.estimatedDurationMinutes))
                .toList();
        return Route.reconstitute(
                new RouteId(e.id), e.shipmentId, e.vehicleType,
                new Coordinates(e.originLatitude, e.originLongitude),
                new Coordinates(e.destinationLatitude, e.destinationLongitude),
                segments, e.totalDistanceKm, e.totalDurationMinutes,
                e.estimatedArrival, new FuelEstimate(e.fuelLitres, e.fuelCostBrl),
                e.tollsCostBrl, RouteStatus.valueOf(e.status));
    }
}
