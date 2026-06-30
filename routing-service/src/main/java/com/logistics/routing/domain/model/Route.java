package com.logistics.routing.domain.model;

import com.logistics.common.domain.AggregateRoot;
import com.logistics.routing.domain.events.RouteCalculated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.time.Instant;
import java.util.List;

@Getter
public class Route extends AggregateRoot {

    private final RouteId id;
    private final String shipmentId;
    private final String vehicleType;
    private final Coordinates origin;
    private final Coordinates destination;
    private final List<RouteSegment> segments;
    private final double totalDistanceKm;
    private final long totalDurationMinutes;
    private final Instant estimatedArrival;
    private final FuelEstimate fuelEstimate;
    private final double tollsCostBrl;
    private RouteStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    private Route(RouteId id, String shipmentId, String vehicleType,
                  Coordinates origin, Coordinates destination,
                  @Singular List<RouteSegment> segments, double totalDistanceKm, long totalDurationMinutes,
                  Instant estimatedArrival, FuelEstimate fuelEstimate, double tollsCostBrl,
                  RouteStatus status) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.vehicleType = vehicleType;
        this.origin = origin;
        this.destination = destination;
        this.segments = segments;
        this.totalDistanceKm = totalDistanceKm;
        this.totalDurationMinutes = totalDurationMinutes;
        this.estimatedArrival = estimatedArrival;
        this.fuelEstimate = fuelEstimate;
        this.tollsCostBrl = tollsCostBrl;
        this.status = status;
    }

    // Domain factory — routing engine produces a Route from inputs
    public static Route calculate(String shipmentId, String vehicleType,
                                   Coordinates origin, Coordinates destination,
                                   List<RouteSegment> segments, Instant estimatedArrival,
                                   FuelEstimate fuelEstimate, double tollsCostBrl) {
        if (segments == null || segments.isEmpty()) throw new IllegalArgumentException("Route must have at least one segment");

        double totalKm = segments.stream().mapToDouble(RouteSegment::distanceKm).sum();
        long totalMin = segments.stream().mapToLong(RouteSegment::estimatedDurationMinutes).sum();

        RouteId id = RouteId.generate();
        Route route = Route.builder()
                .id(id).shipmentId(shipmentId).vehicleType(vehicleType)
                .origin(origin).destination(destination)
                .segments(segments).totalDistanceKm(totalKm).totalDurationMinutes(totalMin)
                .estimatedArrival(estimatedArrival).fuelEstimate(fuelEstimate).tollsCostBrl(tollsCostBrl)
                .status(RouteStatus.CALCULATED)
                .build();

        route.registerEvent(RouteCalculated.of(id.toString(), shipmentId, vehicleType,
                totalKm, totalMin, estimatedArrival, fuelEstimate.litres(), fuelEstimate.costBrl(), tollsCostBrl));
        return route;
    }

    public static Route reconstitute(RouteId id, String shipmentId, String vehicleType,
                                      Coordinates origin, Coordinates destination,
                                      List<RouteSegment> segments, double totalDistanceKm,
                                      long totalDurationMinutes, Instant estimatedArrival,
                                      FuelEstimate fuelEstimate, double tollsCostBrl, RouteStatus status) {
        return Route.builder()
                .id(id).shipmentId(shipmentId).vehicleType(vehicleType)
                .origin(origin).destination(destination)
                .segments(segments).totalDistanceKm(totalDistanceKm).totalDurationMinutes(totalDurationMinutes)
                .estimatedArrival(estimatedArrival).fuelEstimate(fuelEstimate).tollsCostBrl(tollsCostBrl)
                .status(status)
                .build();
    }
}
