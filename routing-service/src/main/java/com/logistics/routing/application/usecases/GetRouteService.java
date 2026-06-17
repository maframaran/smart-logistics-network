package com.logistics.routing.application.usecases;

import com.logistics.routing.domain.model.Route;
import com.logistics.routing.domain.model.RouteId;
import com.logistics.routing.domain.ports.in.GetRouteUseCase;
import com.logistics.routing.domain.ports.out.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetRouteService implements GetRouteUseCase {

    private final RouteRepository repository;

    public GetRouteService(RouteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Route findById(RouteId id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));
    }

    @Override
    public Optional<Route> findByShipmentId(String shipmentId) {
        return repository.findByShipmentId(shipmentId);
    }
}
