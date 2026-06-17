package com.logistics.routing.domain.ports.out;

import com.logistics.routing.domain.model.Route;
import com.logistics.routing.domain.model.RouteId;

import java.util.Optional;

public interface RouteRepository {
    void save(Route route);
    Optional<Route> findById(RouteId id);
    Optional<Route> findByShipmentId(String shipmentId);
}
