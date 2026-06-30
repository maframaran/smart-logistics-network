package com.logistics.routing.domain;

import com.logistics.routing.domain.events.RouteCalculated;
import com.logistics.routing.domain.model.*;
import com.logistics.common.domain.DomainEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RouteTest {

    private static final Coordinates SAO_PAULO  = new Coordinates(-23.5505, -46.6333);
    private static final Coordinates RIO        = new Coordinates(-22.9068, -43.1729);
    private static final FuelEstimate FUEL      = new FuelEstimate(50.0, 310.0);

    private RouteSegment segment(Coordinates from, Coordinates to) {
        double km = from.distanceKmTo(to);
        return new RouteSegment(1, "Direct", from, to, km, (long)(km / 80 * 60));
    }

    @Test
    void calculate_raisesRouteCalculatedEvent() {
        RouteSegment seg = segment(SAO_PAULO, RIO);
        Route route = Route.calculate("ship-1", "TRUCK", SAO_PAULO, RIO,
                List.of(seg), Instant.now().plusSeconds(3600), FUEL, 25.0);

        List<DomainEvent> events = route.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(RouteCalculated.class);
        assertThat(route.getStatus()).isEqualTo(RouteStatus.CALCULATED);
    }

    @Test
    void calculate_aggregatesTotalDistanceAndDuration() {
        RouteSegment seg = segment(SAO_PAULO, RIO);
        Route route = Route.calculate("ship-2", "VAN", SAO_PAULO, RIO,
                List.of(seg), Instant.now().plusSeconds(3600), FUEL, 0.0);

        assertThat(route.getTotalDistanceKm()).isGreaterThan(300.0); // SP-RJ ~360km
        assertThat(route.getTotalDurationMinutes()).isGreaterThan(200L);
    }

    @Test
    void calculate_withNoSegments_throws() {
        List<RouteSegment> noSegments = List.of();
        Instant eta = Instant.now();
        assertThatThrownBy(() -> Route.calculate("ship-3", "TRUCK", SAO_PAULO, RIO,
                noSegments, eta, FUEL, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one segment");
    }

    @Test
    void coordinates_haversineDistance_spToRio() {
        double km = SAO_PAULO.distanceKmTo(RIO);
        assertThat(km).isBetween(350.0, 400.0); // ~360 km
    }

    @Test
    void coordinates_invalidLatitude_throws() {
        assertThatThrownBy(() -> new Coordinates(91.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("latitude");
    }

    @Test
    void coordinates_invalidLongitude_throws() {
        assertThatThrownBy(() -> new Coordinates(0.0, 181.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("longitude");
    }

    @Test
    void fuelEstimate_negative_throws() {
        assertThatThrownBy(() -> new FuelEstimate(-1.0, 10.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("litres");
    }
}
