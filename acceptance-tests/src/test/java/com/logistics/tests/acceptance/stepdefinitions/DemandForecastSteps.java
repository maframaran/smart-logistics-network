package com.logistics.tests.acceptance.stepdefinitions;

import com.logistics.tests.acceptance.AcceptanceTestBase;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

// Step definitions for demand-forecast.feature
// Spec: specs/acceptance-tests/demand-forecast.feature
public class DemandForecastSteps extends AcceptanceTestBase {

    private static final String RAG_URL = env("RAG_SERVICE_URL", "http://localhost:8088");

    private Response response;

    @Given("{int} months of shipment history for shipper {string} on São Paulo → Rio have been indexed")
    public void seed_shipment_history(int months, String shipperId) {
        for (int m = 0; m < months; m++) {

            for (int s = 0; s < 5; s++) {
                given().baseUri(shipmentUrl())
                        .contentType("application/json")
                        .body(Map.of(
                                "shipperId", shipperId,
                                "origin", Map.of("street", "Av Paulista", "city", "SaoPaulo", "country", "BR", "lat", -23.56, "lon", -46.65),
                                "destination", Map.of("street", "Copacabana", "city", "RioDeJaneiro", "country", "BR", "lat", -22.97, "lon", -43.19),
                                "cargo", Map.of("weightKg", 300, "volumeM3", 1.5, "requiresHazmat", false, "requiresColdChain", false),
                                "slaType", "STANDARD",
                                "requiredDeliveryDate", LocalDate.now().plusDays(7).toString()
                        ))
                        .post("/api/v1/shipments");
            }
        }
        awaitMillis(3000);
    }

    @Given("no history exists for shipper {string}")
    public void no_history(String shipperId) {
        // no seeding
    }

    @When("I call GET /api/v1/rag/forecast with shipperId {string} originCity {string} destinationCity {string} targetMonth {string}")
    public void call_forecast(String shipperId, String origin, String dest, String month) {
        response = given()
                .baseUri(RAG_URL)
                .queryParam("shipperId", shipperId)
                .queryParam("originCity", origin)
                .queryParam("destinationCity", dest)
                .queryParam("targetMonth", month)
                .get("/api/v1/rag/forecast")
                .then().extract().response();
    }

    @When("I call GET /api/v1/rag/forecast with shipperId {string}")
    public void call_forecast_minimal(String shipperId) {
        call_forecast(shipperId, "SaoPaulo", "RioDeJaneiro", "2026-08");
    }

    @Then("expectedShipments is greater than {int}")
    public void expected_shipments_greater_than(int threshold) {
        assertThat(response.jsonPath().getInt("expectedShipments")).isGreaterThan(threshold);
    }

    @Then("expectedShipments is {int}")
    public void expected_shipments_is(int value) {
        assertThat(response.jsonPath().getInt("expectedShipments")).isEqualTo(value);
    }

    @Then("confidenceInterval.low is less than expectedShipments")
    public void confidence_low_less_than_expected() {
        int expected = response.jsonPath().getInt("expectedShipments");
        int low = response.jsonPath().getInt("confidenceInterval.low");
        assertThat(low).isLessThan(expected);
    }

    @Then("confidenceInterval.high is greater than expectedShipments")
    public void confidence_high_greater_than_expected() {
        int expected = response.jsonPath().getInt("expectedShipments");
        int high = response.jsonPath().getInt("confidenceInterval.high");
        assertThat(high).isGreaterThan(expected);
    }

    @Then("at least {int} comparables are listed")
    public void at_least_comparables(int min) {
        List<?> comparables = response.jsonPath().getList("comparables");
        assertThat(comparables).hasSizeGreaterThanOrEqualTo(min);
    }
}
