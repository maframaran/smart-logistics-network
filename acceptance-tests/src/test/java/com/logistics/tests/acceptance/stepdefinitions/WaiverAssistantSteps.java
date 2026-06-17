package com.logistics.tests.acceptance.stepdefinitions;

import com.logistics.tests.acceptance.AcceptanceTestBase;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

// Step definitions for waiver-assistant.feature
// Spec: specs/acceptance-tests/waiver-assistant.feature
public class WaiverAssistantSteps extends AcceptanceTestBase {

    private static final String RAG_URL = env("RAG_SERVICE_URL", "http://localhost:8088");

    private String invoiceId;
    private Response response;

    @Given("{int} historical PRIORITY invoices with weather-delay waivers have been indexed")
    public void seed_historical_invoices(int count) {
        // Seed invoices via billing-service; rag-service indexes them via billing.invoice-generated Kafka event
        for (int i = 0; i < count; i++) {
            given().baseUri(billingUrl())
                    .contentType("application/json")
                    .body(Map.of(
                            "shipmentId", java.util.UUID.randomUUID().toString(),
                            "shipperId", "test-shipper", "carrierId", "test-carrier",
                            "slaType", "PRIORITY", "baseAmountBrl", 800.00,
                            "daysLate", 2, "cancellationReason", "weather delay"
                    ))
                    .post("/api/v1/invoices");
        }
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @Given("a PRIORITY invoice with {int} days late and penalty {int} BRL exists")
    public void a_late_invoice_exists(int daysLate, int penalty) {
        Response r = given().baseUri(billingUrl())
                .contentType("application/json")
                .body(Map.of(
                        "shipmentId", java.util.UUID.randomUUID().toString(),
                        "shipperId", "test-shipper", "carrierId", "test-carrier",
                        "slaType", "PRIORITY", "baseAmountBrl", 800.00,
                        "daysLate", daysLate
                ))
                .post("/api/v1/invoices")
                .then().statusCode(201).extract().response();
        invoiceId = r.jsonPath().getString("invoiceId");
    }

    @Given("a STANDARD invoice with {int} day late and penalty {int} BRL exists")
    public void a_standard_late_invoice(int daysLate, int penalty) {
        Response r = given().baseUri(billingUrl())
                .contentType("application/json")
                .body(Map.of(
                        "shipmentId", java.util.UUID.randomUUID().toString(),
                        "shipperId", "test-shipper", "carrierId", "test-carrier",
                        "slaType", "STANDARD", "baseAmountBrl", 300.00,
                        "daysLate", daysLate
                ))
                .post("/api/v1/invoices")
                .then().statusCode(201).extract().response();
        invoiceId = r.jsonPath().getString("invoiceId");
    }

    @Given("no similar invoices have been indexed")
    public void no_similar_invoices() {
        // no seeding
    }

    @When("Finance POSTs /api/v1/rag/invoices/\\{invoiceId}/waiver with reason {string}")
    public void post_waiver_with_reason(String reason) {
        response = given()
                .baseUri(RAG_URL)
                .contentType("application/json")
                .body(Map.of("reason", reason))
                .post("/api/v1/rag/invoices/" + invoiceId + "/waiver")
                .then().extract().response();
    }

    @When("Finance POSTs a waiver with reason {string}")
    public void post_waiver(String reason) {
        post_waiver_with_reason(reason);
    }

    @When("Finance POSTs a waiver request")
    public void post_waiver_no_reason() {
        post_waiver_with_reason("no reason provided");
    }

    @Then("recommendation is {string}")
    public void recommendation_is(String expected) {
        assertThat(response.jsonPath().getString("recommendation")).isEqualTo(expected);
    }

    @Then("confidence is greater than {double}")
    public void confidence_greater_than(double threshold) {
        assertThat(response.jsonPath().getDouble("confidence")).isGreaterThan(threshold);
    }

    @Then("at least {int} precedents are listed in the response")
    public void precedents_listed(int min) {
        List<?> precedents = response.jsonPath().getList("precedents");
        assertThat(precedents).hasSizeGreaterThanOrEqualTo(min);
    }
}
