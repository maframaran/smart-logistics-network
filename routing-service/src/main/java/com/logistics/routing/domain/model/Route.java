package com.logistics.routing.domain.model;

import com.logistics.common.domain.AggregateRoot;
import com.logistics.routing.domain.events.RouteCalculated;

import java.time.Instant;
import java.util.List;

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

    private Route(RouteId id, String shipmentId, String vehicleType,
                  Coordinates origin, Coordinates destination,
                  List<RouteSegment> segments, double totalDistanceKm, long totalDurationMinutes,
                  Instant estimatedArrival, FuelEstimate fuelEstimate, double tollsCostBrl,
                  RouteStatus status) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.vehicleType = vehicleType;
        this.origin = origin;
        this.destination = destination;
        this.segments = List.copyOf(segments);
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
        Route route = new Route(id, shipmentId, vehicleType, origin, destination,
                segments, totalKm, totalMin, estimatedArrival, fuelEstimate, tollsCostBrl, RouteStatus.CALCULATED);

        route.registerEvent(RouteCalculated.of(id.toString(), shipmentId, vehicleType,
                totalKm, totalMin, estimatedArrival, fuelEstimate.litres(), fuelEstimate.costBrl(), tollsCostBrl));
        return route;
    }

    public static Route reconstitute(RouteId id, String shipmentId, String vehicleType,
                                      Coordinates origin, Coordinates destination,
                                      List<RouteSegment> segments, double totalDistanceKm,
                                      long totalDurationMinutes, Instant estimatedArrival,
                                      FuelEstimate fuelEstimate, double tollsCostBrl, RouteStatus status) {
        return new Route(id, shipmentId, vehicleType, origin, destination, segments,
                totalDistanceKm, totalDurationMinutes, estimatedArrival, fuelEstimate, tollsCostBrl, status);
    }

    public RouteId getId() { return id; }
    public String getShipmentId() { return shipmentId; }
    public String getVehicleType() { return vehicleType; }
    public Coordinates getOrigin() { return origin; }
    public Coordinates getDestination() { return destination; }
    public List<RouteSegment> getSegments() { return segments; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public long getTotalDurationMinutes() { return totalDurationMinutes; }
    public Instant getEstimatedArrival() { return estimatedArrival; }
    public FuelEstimate getFuelEstimate() { return fuelEstimate; }
    public double getTollsCostBrl() { return tollsCostBrl; }
    public RouteStatus getStatus() { return status; }
}
