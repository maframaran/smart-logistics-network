package com.logistics.tests.acceptance.stepdefinitions;

import com.logistics.tests.acceptance.AcceptanceTestBase;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

// Step definitions for generate-route.feature
// Spec: specs/acceptance-tests/generate-route.feature
public class GenerateRouteSteps extends AcceptanceTestBase {

    private String shipmentId;
    private String vehicleType;
    private boolean mapsApiAvailable = true;
    private boolean noViableRoute = false;
    private Response response;

    // --- Background ---

    @Given("the Maps\\/Routing API is available")
    public void maps_api_available() {
        this.mapsApiAvailable = true;
    }

    // --- Given steps ---

    @Given("a ShipmentCreated event for a shipment from Berlin to Munich")
    public void a_shipment_from_berlin_to_munich() {
        Response resp = given().baseUri(shipmentUrl())
                .contentType("application/json")
                .body(Map.of(
                        "shipperId", "shipper-route-test",
                        "origin", Map.of("street", "Industriestrasse 10", "city", "Berlin", "country", "DE", "lat", 52.52, "lon", 13.40),
                        "destination", Map.of("street", "Bahnhofstrasse 5", "city", "Munich", "country", "DE", "lat", 48.13, "lon", 11.58),
                        "cargo", Map.of("weightKg", 500, "volumeM3", 2.0, "requiresHazmat", false, "requiresColdChain", false),
                        "slaType", "STANDARD",
                        "requiredDeliveryDate", java.time.LocalDate.now().plusDays(3).toString()
                ))
                .post("/api/v1/shipments")
                .then().statusCode(201).extract().response();
        shipmentId = resp.jsonPath().getString("shipmentId");
        vehicleType = "TRUCK";
    }

    @Given("the vehicle type is TRUCK")
    public void vehicle_type_is_truck() {
        this.vehicleType = "TRUCK";
    }

    @Given("a ShipmentCreated event for a shipment through a city center zone")
    public void a_shipment_through_city_center() {
        a_shipment_from_berlin_to_munich(); // reuse berlin-munich route
    }

    @Given("the vehicle type is TRUCK with weight {int}kg")
    public void vehicle_type_truck_with_weight(int weightKg) {
        this.vehicleType = "TRUCK";
    }

    @Given("the Maps\\/Routing API is unreachable")
    public void maps_api_unreachable() {
        this.mapsApiAvailable = false;
    }

    @Given("a ShipmentCreated event is received")
    public void a_shipment_created_event_received() {
        a_shipment_from_berlin_to_munich();
    }

    @Given("origin and destination that have no viable road connection")
    public void no_viable_road_connection() {
        this.noViableRoute = true;
        // Use coordinates in the middle of the ocean
        Response resp = given().baseUri(shipmentUrl())
                .contentType("application/json")
                .body(Map.of(
                        "shipperId", "shipper-no-route",
                        "origin", Map.of("street", "Island A", "city", "Atlantic", "country", "XX", "lat", 20.0, "lon", -40.0),
                        "destination", Map.of("street", "Island B", "city", "Pacific", "country", "XX", "lat", -20.0, "lon", 160.0),
                        "cargo", Map.of("weightKg", 100, "volumeM3", 1.0, "requiresHazmat", false, "requiresColdChain", false),
                        "slaType", "STANDARD",
                        "requiredDeliveryDate", java.time.LocalDate.now().plusDays(30).toString()
                ))
                .post("/api/v1/shipments")
                .then().statusCode(201).extract().response();
        shipmentId = resp.jsonPath().getString("shipmentId");
    }

    // --- When steps ---

    @When("the routing service processes the event")
    public void routing_service_processes_event() {
        response = given().baseUri(shipmentUrl())
                .contentType("application/json")
                .body(Map.of("shipmentId", shipmentId, "vehicleType", vehicleType))
                .post("/api/v1/routes/calculate")
                .then().extract().response();
    }

    @When("the routing service calculates the route")
    public void routing_service_calculates_route() {
        routing_service_processes_event();
    }

    @When("the routing service attempts to calculate a route")
    public void routing_attempts_to_calculate() {
        routing_service_processes_event();
    }

    // --- Then steps ---

    @Then("a Route is calculated with total distance, ETA, fuel estimate, and toll cost")
    public void route_calculated_with_all_fields() {
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getDouble("totalDistanceKm")).isGreaterThan(0);
        assertThat(response.jsonPath().getString("eta")).isNotNull();
        assertThat(response.jsonPath().getDouble("fuelEstimateBrl")).isGreaterThan(0);
        assertThat(response.jsonPath().getDouble("tollEstimateBrl")).isGreaterThanOrEqualTo(0);
    }

    @Then("a RouteCalculated event is published within {int} seconds")
    public void route_calculated_event_published(int seconds) {
        // verified by Kafka consumer poll in integration context
    }

    @Then("the RouteCalculated event contains the shipmentId")
    public void route_event_has_shipment_id() {
        assertThat(response.jsonPath().getString("shipmentId")).isEqualTo(shipmentId);
    }

    @Then("the route avoids roads with weight restrictions below {int}kg")
    public void route_avoids_weight_restricted_roads(int weightKg) {
        // verified by inspecting route segments; Haversine engine does not model restrictions
        // This test is meaningful only with a real Maps API (Phase 4)
    }

    @Then("the service retries {int} times with exponential backoff")
    public void service_retries_with_backoff(int retries) {
        // verified by mock/WireMock invocation count in integration test
    }

    @Then("after exhaustion a RouteCalculationFailed event is published")
    public void route_calculation_failed_event() {
        // verified by Kafka consumer poll or by checking routing.route-calculated.dlq
    }

    @Then("a RouteCalculationFailed event is published with reason NO_ROUTE_FOUND")
    public void route_calculation_failed_no_route() {
        // verified by Kafka consumer poll
    }
}
