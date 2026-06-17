package com.logistics.tests.acceptance.stepdefinitions;

import com.logistics.tests.acceptance.AcceptanceTestBase;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

// Step definitions for route-similarity.feature
// Spec: specs/acceptance-tests/route-similarity.feature
public class RouteSimilaritySteps extends AcceptanceTestBase {

    private static final String RAG_URL = env("RAG_SERVICE_URL", "http://localhost:8088");

    private Response response;

    @Given("the rag-service is running")
    public void rag_service_is_running() {
        given().baseUri(RAG_URL).get("/actuator/health").then().statusCode(200);
    }

    @Given("the following routes have been indexed:")
    public void routes_have_been_indexed(io.cucumber.datatable.DataTable table) {
        // Routes are indexed by the rag-service Kafka consumer when RouteCalculated events arrive.
        // In acceptance test context, seed via the routing-service REST API to trigger the event.
        List<Map<String, String>> rows = table.asMaps();
        for (Map<String, String> row : rows) {
            given().baseUri(routingUrl())
                    .contentType("application/json")
                    .body(Map.of(
                            "shipmentId", java.util.UUID.randomUUID().toString(),
                            "vehicleType", row.get("vehicleType"),
                            "originLatitude", -23.5505, "originLongitude", -46.6333,
                            "destinationLatitude", -25.4284, "destinationLongitude", -49.2733
                    ))
                    .post("/api/v1/routes/calculate")
                    .then().statusCode(201);
        }
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @Given("no routes have been indexed")
    public void no_routes_indexed() {
        // No seeding — relies on a clean test database
    }

    @When("I call GET /api/v1/rag/routes/similar with originCity {string} destinationCity {string} vehicleType {string} slaType {string}")
    public void call_route_similarity(String originCity, String destinationCity, String vehicleType, String slaType) {
        response = given()
                .baseUri(RAG_URL)
                .queryParam("originCity", originCity)
                .queryParam("destinationCity", destinationCity)
                .queryParam("vehicleType", vehicleType)
                .queryParam("slaType", slaType)
                .get("/api/v1/rag/routes/similar")
                .then().extract().response();
    }

    @Then("the response contains at least {int} comparable")
    public void response_contains_comparables(int minCount) {
        List<?> comparables = response.jsonPath().getList("comparables");
        assertThat(comparables).hasSizeGreaterThanOrEqualTo(minCount);
    }

    @Then("estimatedCostBrl is greater than {int}")
    public void estimated_cost_greater_than(int threshold) {
        double cost = response.jsonPath().getDouble("estimatedCostBrl");
        assertThat(cost).isGreaterThan(threshold);
    }

    @Then("estimatedDurationMinutes is greater than {int}")
    public void estimated_duration_greater_than(int threshold) {
        long duration = response.jsonPath().getLong("estimatedDurationMinutes");
        assertThat(duration).isGreaterThan(threshold);
    }

    @Then("comparables list is empty")
    public void comparables_list_empty() {
        List<?> comparables = response.jsonPath().getList("comparables");
        assertThat(comparables).isEmpty();
    }

    @Then("lowConfidence is true")
    public void low_confidence_true() {
        assertThat(response.jsonPath().getBoolean("lowConfidence")).isTrue();
    }
}
