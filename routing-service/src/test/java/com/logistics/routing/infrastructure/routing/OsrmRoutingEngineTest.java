package com.logistics.routing.infrastructure.routing;

import com.logistics.routing.domain.model.*;
import com.logistics.routing.domain.ports.out.RoutingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class OsrmRoutingEngineTest {

    private static final Coordinates SP  = new Coordinates(-23.5505, -46.6333);
    private static final Coordinates RIO = new Coordinates(-22.9068, -43.1729);

    private WebClient.RequestHeadersUriSpec uriSpec;
    private WebClient.RequestHeadersSpec    headersSpec;
    private WebClient.ResponseSpec          responseSpec;
    private WebClient                       webClient;

    private HaversineRoutingEngine haversine;
    private OsrmRoutingEngine      engine;

    @BeforeEach
    void setUp() {
        webClient    = mock(WebClient.class);
        uriSpec      = mock(WebClient.RequestHeadersUriSpec.class);
        headersSpec  = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString());
        doReturn(responseSpec).when(headersSpec).retrieve();

        haversine = new HaversineRoutingEngine();
        engine    = new OsrmRoutingEngine(webClient, haversine);
    }

    @Test
    void usesRealDistanceFromOsrmResponse() {
        // ~432 km road distance SP→RIO, 300 min travel time
        Map<String, Object> body = parseJson(
                "{\"code\":\"Ok\",\"routes\":[{\"distance\":432000.0,\"duration\":18000.0,\"legs\":[]}]}");
        doReturn(Mono.just(body)).when(responseSpec).bodyToMono(any(ParameterizedTypeReference.class));

        RoutingEngine.Result result = engine.compute(SP, RIO, "TRUCK", Instant.now().plusSeconds(86400));

        assertThat(result.segments()).hasSize(1);
        assertThat(result.segments().get(0).distanceKm()).isEqualTo(432.0, within(0.01));
        assertThat(result.segments().get(0).estimatedDurationMinutes()).isEqualTo(300L);
        // fuel: 432 km × 0.12 L/km = 51.84 L × 6.20 BRL/L ≈ 321.41
        assertThat(result.fuelEstimate().costBrl()).isCloseTo(321.41, within(0.1));
        // tolls: 432 km × 0.05 BRL/km = 21.60
        assertThat(result.tollsCostBrl()).isCloseTo(21.60, within(0.01));
    }

    @Test
    void fallsBackToHaversineOnNonOkCode() {
        Map<String, Object> body = parseJson("{\"code\":\"NoRoute\"}");
        doReturn(Mono.just(body)).when(responseSpec).bodyToMono(any(ParameterizedTypeReference.class));

        RoutingEngine.Result result = engine.compute(SP, RIO, "TRUCK", Instant.now().plusSeconds(86400));

        assertThat(result.segments()).isNotEmpty();
        assertThat(result.segments().get(0).distanceKm()).isPositive();
    }

    @Test
    void fallsBackToHaversineOnNullResponse() {
        doReturn(Mono.empty()).when(responseSpec).bodyToMono(any(ParameterizedTypeReference.class));

        RoutingEngine.Result result = engine.compute(SP, RIO, "TRUCK", Instant.now().plusSeconds(86400));

        assertThat(result.segments()).isNotEmpty();
    }

    @Test
    void fallsBackToHaversineOnWebClientException() {
        doThrow(new WebClientException("connection refused") {}).when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));

        RoutingEngine.Result result = engine.compute(SP, RIO, "TRUCK", Instant.now().plusSeconds(86400));

        assertThat(result.segments()).isNotEmpty();
    }

    private static Map<String, Object> parseJson(String json) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
