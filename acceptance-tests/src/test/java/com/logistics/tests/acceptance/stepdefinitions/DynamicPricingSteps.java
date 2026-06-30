package com.logistics.tests.acceptance.stepdefinitions;

import com.logistics.tests.acceptance.AcceptanceTestBase;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

// Step definitions for dynamic-pricing.feature
// Spec: specs/acceptance-tests/dynamic-pricing.feature
public class DynamicPricingSteps extends AcceptanceTestBase {

    private static final String RAG_URL = env("RAG_SERVICE_URL", "http://localhost:8088");

    private Response response;

    @Given("{int} PAID STANDARD invoices for São Paulo → Rio de Janeiro have been indexed")
    public void seed_paid_invoices(int count) {
        for (int i = 0; i < count; i++) {
            given().baseUri(billingUrl())
                    .contentType("application/json")
                    .body(Map.of(
                            "shipmentId", java.util.UUID.randomUUID().toString(),
                            "shipperId", "test-shipper", "carrierId", "test-carrier",
                            "slaType", "STANDARD", "baseAmountBrl", 350.00 + (i * 10),
                            "daysLate", 0, "status", "PAID",
                            "originCity", "SaoPaulo", "destinationCity", "RioDeJaneiro"
                    ))
                    .post("/api/v1/invoices");
        }
        awaitMillis(2000);
    }

    @Given("fewer than {int} paid comparables exist for the route")
    public void fewer_comparables(int count) {
        // no seeding — relies on clean DB
    }

    @When("I POST /api/v1/rag/pricing/recommend with originCity {string} destinationCity {string} weightKg {int} slaType {string} warehouseUtilizationPct {int}")
    public void post_pricing(String origin, String dest, int weight, String sla, int util) {
        response = given()
                .baseUri(RAG_URL)
                .contentType("application/json")
                .body(Map.of(
                        "originCity", origin, "destinationCity", dest,
                        "weightKg", weight, "slaType", sla,
                        "warehouseUtilizationPct", util
                ))
                .post("/api/v1/rag/pricing/recommend")
                .then().extract().response();
    }

    @When("I POST /api/v1/rag/pricing/recommend")
    public void post_pricing_default() {
        post_pricing("SaoPaulo", "RioDeJaneiro", 500, "STANDARD", 75);
    }

    @When("I POST /api/v1/rag/pricing/recommend for an EXPRESS shipment")
    public void post_pricing_express() {
        post_pricing("SaoPaulo", "RioDeJaneiro", 200, "EXPRESS", 85);
    }

    @Then("suggestedPriceBrl is greater than {int}")
    public void suggested_price_greater_than(int threshold) {
        assertThat(response.jsonPath().getDouble("suggestedPriceBrl")).isGreaterThan(threshold);
    }

    @Then("confidencePct is at least {int}")
    public void confidence_at_least(int min) {
        assertThat(response.jsonPath().getDouble("confidencePct")).isGreaterThanOrEqualTo(min);
    }

    @Then("confidencePct is at most {int}")
    public void confidence_at_most(int max) {
        assertThat(response.jsonPath().getDouble("confidencePct")).isLessThanOrEqualTo(max);
    }

    @Then("lowerBound is less than suggestedPriceBrl")
    public void lower_bound_less_than_suggested() {
        double suggested = response.jsonPath().getDouble("suggestedPriceBrl");
        double lower = response.jsonPath().getDouble("lowerBound");
        assertThat(lower).isLessThan(suggested);
    }

    @Then("upperBound is greater than suggestedPriceBrl")
    public void upper_bound_greater_than_suggested() {
        double suggested = response.jsonPath().getDouble("suggestedPriceBrl");
        double upper = response.jsonPath().getDouble("upperBound");
        assertThat(upper).isGreaterThan(suggested);
    }

    @Then("suggestedPriceBrl is at most {double}")
    public void suggested_price_at_most(double cap) {
        assertThat(response.jsonPath().getDouble("suggestedPriceBrl")).isLessThanOrEqualTo(cap);
    }
}
