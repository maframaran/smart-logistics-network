package com.logistics.routing.domain.ports.in;

import com.logistics.routing.domain.model.Route;
import com.logistics.routing.domain.model.RouteId;

import java.util.Optional;

public interface GetRouteUseCase {
    Route findById(RouteId id);
    Optional<Route> findByShipmentId(String shipmentId);
}
