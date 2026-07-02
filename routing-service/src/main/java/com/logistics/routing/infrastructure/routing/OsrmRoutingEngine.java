package com.logistics.routing.infrastructure.routing;

import com.logistics.routing.domain.model.*;
import com.logistics.routing.domain.ports.out.RoutingEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

// Calls the self-hosted OSRM HTTP API for real road distance/duration (ADR-033).
// Falls back to HaversineRoutingEngine on any connectivity or protocol error.
class OsrmRoutingEngine implements RoutingEngine {

    private static final Logger log = LoggerFactory.getLogger(OsrmRoutingEngine.class);

    private static final double FUEL_CONSUMPTION_L_PER_KM = 0.12;
    private static final double FUEL_PRICE_BRL_PER_L      = 6.20;
    private static final double TOLLS_BRL_PER_KM          = 0.05;

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient webClient;
    private final HaversineRoutingEngine fallback;

    OsrmRoutingEngine(WebClient webClient, HaversineRoutingEngine fallback) {
        this.webClient = webClient;
        this.fallback = fallback;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result compute(Coordinates origin, Coordinates destination, String vehicleType, Instant requiredDeliveryBy) {
        // OSRM expects longitude,latitude order
        String path = "/route/v1/driving/%s,%s;%s,%s?overview=false".formatted(
                origin.longitude(), origin.latitude(),
                destination.longitude(), destination.latitude());
        try {
            Map<String, Object> response = webClient.get()
                    .uri(path)
                    .retrieve()
                    .bodyToMono(MAP_TYPE)
                    .block(Duration.ofSeconds(5));

            if (response == null || !"Ok".equals(response.get("code"))) {
                log.warn("OSRM returned non-OK response, falling back to Haversine");
                return fallback.compute(origin, destination, vehicleType, requiredDeliveryBy);
            }

            List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
            if (routes == null || routes.isEmpty()) {
                log.warn("OSRM returned no routes, falling back to Haversine");
                return fallback.compute(origin, destination, vehicleType, requiredDeliveryBy);
            }

            Map<String, Object> route = routes.get(0);
            double distanceM   = ((Number) route.get("distance")).doubleValue();
            double durationS   = ((Number) route.get("duration")).doubleValue();
            double distanceKm  = distanceM / 1000.0;
            long   durationMin = (long) (durationS / 60.0);

            Instant eta = Instant.now().plusSeconds((long) durationS);
            RouteSegment segment = new RouteSegment(1, "Road route via OSRM", origin, destination, distanceKm, durationMin);
            double litres = distanceKm * FUEL_CONSUMPTION_L_PER_KM;
            FuelEstimate fuel = new FuelEstimate(litres, litres * FUEL_PRICE_BRL_PER_L);
            double tolls = distanceKm * TOLLS_BRL_PER_KM;

            return new Result(List.of(segment), eta, fuel, tolls);

        } catch (WebClientException | IllegalStateException e) {
            log.warn("OSRM unreachable ({}), falling back to Haversine", e.getMessage());
            return fallback.compute(origin, destination, vehicleType, requiredDeliveryBy);
        }
    }
}
