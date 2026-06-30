package com.logistics.routing.application.usecases;

import com.logistics.routing.domain.model.Route;
import com.logistics.routing.domain.model.RouteId;
import com.logistics.routing.domain.ports.in.CalculateRouteUseCase;
import com.logistics.routing.domain.ports.out.RouteRepository;
import com.logistics.routing.domain.ports.out.RoutingEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CalculateRouteService implements CalculateRouteUseCase {

    private final RoutingEngine routingEngine;
    private final RouteRepository repository;

    public CalculateRouteService(RoutingEngine routingEngine, RouteRepository repository) {
        this.routingEngine = routingEngine;
        this.repository = repository;
    }

    @Override
    public RouteId calculate(Command command) {
        RoutingEngine.Result result = routingEngine.compute(
                command.origin(), command.destination(), command.vehicleType(), command.requiredDeliveryBy());

        Route route = Route.calculate(
                command.shipmentId(), command.vehicleType(),
                command.origin(), command.destination(),
                result.segments(), result.estimatedArrival(),
                result.fuelEstimate(), result.tollsCostBrl());

        // repository.save() persists the aggregate and writes its domain events to the
        // outbox in the same transaction; OutboxRelayScheduler publishes them (ADR-030).
        repository.save(route);
        return route.getId();
    }
}
