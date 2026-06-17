package com.logistics.routing.domain.ports.in;

import com.logistics.routing.domain.model.Coordinates;
import com.logistics.routing.domain.model.RouteId;

import java.time.Instant;

public interface CalculateRouteUseCase {

    RouteId calculate(Command command);

    record Command(
            String shipmentId,
            String vehicleType,
            Coordinates origin,
            Coordinates destination,
            Instant requiredDeliveryBy
    ) {}
}
