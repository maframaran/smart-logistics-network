package com.logistics.routing.infrastructure.routing;

import com.logistics.routing.domain.ports.out.RoutingEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class RoutingEngineConfig {

    @Bean
    HaversineRoutingEngine haversineRoutingEngine() {
        return new HaversineRoutingEngine();
    }

    // Selects OSRM when routing.osrm.url is set; otherwise uses Haversine.
    // OsrmRoutingEngine also falls back to Haversine on any OSRM connectivity failure.
    @Bean
    RoutingEngine routingEngine(
            @Value("${routing.osrm.url:}") String osrmUrl,
            HaversineRoutingEngine haversine) {
        if (osrmUrl == null || osrmUrl.isBlank()) {
            return haversine;
        }
        WebClient client = WebClient.builder().baseUrl(osrmUrl).build();
        return new OsrmRoutingEngine(client, haversine);
    }
}
