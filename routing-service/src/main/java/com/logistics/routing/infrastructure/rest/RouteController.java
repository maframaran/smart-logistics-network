package com.logistics.routing.infrastructure.rest;

import com.logistics.routing.domain.model.*;
import com.logistics.routing.domain.ports.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Tag(name = "Routes", description = "Route calculation (Haversine placeholder; OSRM in Phase 4)")
@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {

    private final CalculateRouteUseCase calculateRoute;
    private final GetRouteUseCase getRoute;

    public RouteController(CalculateRouteUseCase calculateRoute, GetRouteUseCase getRoute) {
        this.calculateRoute = calculateRoute;
        this.getRoute = getRoute;
    }

    @Operation(summary = "Calculate a route", description = "Computes distance, ETA, fuel estimate, and toll cost; raises RouteCalculated.")
    @ApiResponse(responseCode = "201", description = "Route calculated")
    @PostMapping
    public ResponseEntity<RouteResponse> calculate(@RequestBody CalculateRouteRequest request) {
        RouteId id = calculateRoute.calculate(new CalculateRouteUseCase.Command(
                request.shipmentId(),
                request.vehicleType(),
                new Coordinates(request.originLat(), request.originLon()),
                new Coordinates(request.destinationLat(), request.destinationLon()),
                request.requiredDeliveryBy()
        ));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(new RouteResponse(id.toString()));
    }

    @Operation(summary = "Get a route by ID")
    @GetMapping("/{id}")
    public ResponseEntity<RouteDetailResponse> get(@PathVariable String id) {
        Route route = getRoute.findById(RouteId.of(id));
        return ResponseEntity.ok(toDetail(route));
    }

    @Operation(summary = "Get a route by shipment ID")
    @GetMapping
    public ResponseEntity<RouteDetailResponse> getByShipment(@RequestParam String shipmentId) {
        return getRoute.findByShipmentId(shipmentId)
                .map(r -> ResponseEntity.ok(toDetail(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    private RouteDetailResponse toDetail(Route r) {
        List<SegmentResponse> segs = r.getSegments().stream()
                .map(s -> new SegmentResponse(s.order(), s.label(), s.distanceKm(), s.estimatedDurationMinutes()))
                .toList();
        return new RouteDetailResponse(r.getId().toString(), r.getShipmentId(), r.getVehicleType(),
                r.getTotalDistanceKm(), r.getTotalDurationMinutes(), r.getEstimatedArrival(),
                r.getFuelEstimate().litres(), r.getFuelEstimate().costBrl(), r.getTollsCostBrl(),
                r.getStatus().name(), segs);
    }

    record CalculateRouteRequest(String shipmentId, String vehicleType,
                                  double originLat, double originLon,
                                  double destinationLat, double destinationLon,
                                  Instant requiredDeliveryBy) {}
    record RouteResponse(String routeId) {}
    record SegmentResponse(int order, String label, double distanceKm, long durationMinutes) {}
    record RouteDetailResponse(String routeId, String shipmentId, String vehicleType,
                               double totalDistanceKm, long totalDurationMinutes, Instant estimatedArrival,
                               double fuelLitres, double fuelCostBrl, double tollsCostBrl,
                               String status, List<SegmentResponse> segments) {}
}
