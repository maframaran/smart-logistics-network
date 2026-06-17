package com.logistics.routing.application;

import com.logistics.routing.application.usecases.CalculateRouteService;
import com.logistics.routing.domain.model.*;
import com.logistics.routing.domain.ports.in.CalculateRouteUseCase;
import com.logistics.routing.domain.ports.out.RouteEventPublisher;
import com.logistics.routing.domain.ports.out.RouteRepository;
import com.logistics.routing.domain.ports.out.RoutingEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateRouteServiceTest {

    @Mock RoutingEngine routingEngine;
    @Mock RouteRepository repository;
    @Mock RouteEventPublisher eventPublisher;

    @InjectMocks CalculateRouteService service;

    private static final Coordinates SP  = new Coordinates(-23.5505, -46.6333);
    private static final Coordinates RIO = new Coordinates(-22.9068, -43.1729);

    @Test
    void calculate_savesRouteAndPublishesEvent() {
        RouteSegment seg = new RouteSegment(1, "Direct", SP, RIO, 360.0, 270L);
        RoutingEngine.Result result = new RoutingEngine.Result(
                List.of(seg), Instant.now().plusSeconds(16200),
                new FuelEstimate(43.2, 268.0), 18.0);
        when(routingEngine.compute(any(), any(), any(), any())).thenReturn(result);

        CalculateRouteUseCase.Command command = new CalculateRouteUseCase.Command(
                "ship-1", "TRUCK", SP, RIO, Instant.now().plusSeconds(86400));

        RouteId id = service.calculate(command);

        assertThat(id).isNotNull();
        verify(repository).save(any(Route.class));
        verify(eventPublisher).publish(any());
    }
}
