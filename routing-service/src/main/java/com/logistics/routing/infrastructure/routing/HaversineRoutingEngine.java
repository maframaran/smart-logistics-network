package com.logistics.routing.infrastructure.routing;

import com.logistics.routing.domain.model.*;
import com.logistics.routing.domain.ports.out.RoutingEngine;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

// In-process routing engine using Haversine distance.
// Replace with Maps API adapter in Phase 4.
@Component
public class HaversineRoutingEngine implements RoutingEngine {

    private static final double AVG_SPEED_KMH           = 80.0;
    private static final double FUEL_CONSUMPTION_L_PER_KM = 0.12;
    private static final double FUEL_PRICE_BRL_PER_L    = 6.20;
    private static final double TOLLS_BRL_PER_KM        = 0.05;

    @Override
    public Result compute(Coordinates origin, Coordinates destination, String vehicleType, Instant requiredDeliveryBy) {
        double distanceKm = origin.distanceKmTo(destination);
        long durationMinutes = (long) ((distanceKm / AVG_SPEED_KMH) * 60);
        Instant eta = Instant.now().plusSeconds(durationMinutes * 60);

        RouteSegment segment = new RouteSegment(1, "Direct route", origin, destination, distanceKm, durationMinutes);

        double litres = distanceKm * FUEL_CONSUMPTION_L_PER_KM;
        FuelEstimate fuel = new FuelEstimate(litres, litres * FUEL_PRICE_BRL_PER_L);
        double tolls = distanceKm * TOLLS_BRL_PER_KM;

        return new Result(List.of(segment), eta, fuel, tolls);
    }
}
