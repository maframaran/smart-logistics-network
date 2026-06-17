package com.logistics.routing.infrastructure.persistence;

import com.logistics.routing.domain.model.*;
import com.logistics.routing.domain.ports.out.RouteRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RouteJpaRepository implements RouteRepository {

    private final RouteJpaRepositoryPort jpa;

    public RouteJpaRepository(RouteJpaRepositoryPort jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Route route) {
        jpa.save(toEntity(route));
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
