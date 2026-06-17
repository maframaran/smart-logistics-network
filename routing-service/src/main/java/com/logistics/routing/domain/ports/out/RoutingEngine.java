package com.logistics.routing.domain.ports.out;

import com.logistics.routing.domain.model.*;

import java.time.Instant;
import java.util.List;

// Outbound port — implemented by an infrastructure adapter (stub or Maps API client)
public interface RoutingEngine {

    Result compute(Coordinates origin, Coordinates destination, String vehicleType, Instant requiredDeliveryBy);

    record Result(
            List<RouteSegment> segments,
            Instant estimatedArrival,
            FuelEstimate fuelEstimate,
            double tollsCostBrl
    ) {}
}
