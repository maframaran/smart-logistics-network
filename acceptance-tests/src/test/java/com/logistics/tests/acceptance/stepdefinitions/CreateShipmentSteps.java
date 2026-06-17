package com.logistics.tests.acceptance.stepdefinitions;

import com.logistics.tests.acceptance.AcceptanceTestBase;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.assertj.core.api.Assertions;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

// Step definitions for create-shipment.feature
// Each method maps to a Given/When/Then line in the .feature file.
// Spec: specs/acceptance-tests/create-shipment.feature
public class CreateShipmentSteps extends AcceptanceTestBase {

    private Map<String, Object> requestBody;
    private Response response;

    // --- Given steps ---

    @Given("a cargo weighing {double}kg with volume {double}m³")
    public void a_cargo_weighing_kg_with_volume(double weightKg, double volumeM3) {
        requestBody = Map.of(
                "cargo", Map.of(
                        "weightKg", weightKg,
                        "volumeM3", volumeM3,
                        "requiresHazmat", false,
                        "requiresColdChain", false
                )
        );
    }

    @Given("origin address {string}")
    public void origin_address(String address) {
        // merge into requestBody — simplified for template
        requestBody = merge(requestBody, "origin", Map.of("street", address, "city", "Berlin", "country", "DE"));
    }

    @Given("destination address {string}")
    public void destination_address(String address) {
        requestBody = merge(requestBody, "destination", Map.of("street", address, "city", "Munich", "country", "DE"));
    }

    @Given("required delivery date is {int} days from now")
    public void required_delivery_date_is_days_from_now(int days) {
        String date = java.time.Instant.now().plusSeconds(days * 86400L).toString();
        requestBody = merge(requestBody, "requiredDeliveryDate", date);
    }

    @Given("SLA type is {word}")
    public void sla_type_is(String slaType) {
        requestBody = merge(requestBody, "slaType", slaType);
    }

    @Given("the cargo requires cold chain handling")
    public void the_cargo_requires_cold_chain_handling() {
        requestBody = mergeCargo(requestBody, "requiresColdChain", true);
    }

    @Given("required delivery date is {int} days ago")
    public void required_delivery_date_is_days_ago(int days) {
        String date = java.time.Instant.now().minusSeconds(days * 86400L).toString();
        requestBody = merge(requestBody, "requiredDeliveryDate", date);
    }

    // --- When steps ---

    @When("the Shipper submits the shipment request")
    public void the_shipper_submits_the_shipment_request() {
        response = given()
                .baseUri(shipmentUrl())
                .contentType("application/json")
                .body(requestBody)
                .post("/api/v1/shipments");
    }

    // --- Then steps ---

    // AC-001: spec: specs/features/F-001-create-shipment.md § AC-001
    @Then("the shipment is created with status CREATED")
    public void the_shipment_is_created_with_status_created() {
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("status")).isEqualTo("CREATED");
    }

    // AC-001: unique shipmentId returned
    @Then("a unique shipmentId is returned")
    public void a_unique_shipment_id_is_returned() {
        assertThat(response.jsonPath().getString("shipmentId")).isNotBlank();
    }

    // AC-002: spec: specs/features/F-001-create-shipment.md § AC-002
    @Then("a ShipmentCreated event is published to the {string} topic")
    public void a_shipment_created_event_is_published_to_topic(String topic) {
        // Use a KafkaConsumer (helper not shown) to poll the topic and assert message presence
        // The event schema must match messaging/topics/shipment.created.md
        String shipmentId = response.jsonPath().getString("shipmentId");
        ConsumerRecord<String, String> record = KafkaTestHelper.pollUntilKey(topic, shipmentId);
        assertThat(record).isNotNull();
        assertThat(record.value()).contains("\"eventType\":\"ShipmentCreated\"");
        assertThat(record.value()).contains("\"shipmentId\":\"" + shipmentId + "\"");
    }

    // EC-002: spec: specs/features/F-001-create-shipment.md § Edge Cases EC-002
    @Then("the request is rejected with error code {word}")
    public void the_request_is_rejected_with_error_code(String errorCode) {
        assertThat(response.statusCode()).isIn(400, 409, 422);
        assertThat(response.jsonPath().getString("errorCode")).isEqualTo(errorCode);
    }

    @Then("the request is rejected with a field-level geocoding error for origin")
    public void the_request_is_rejected_with_geocoding_error_for_origin() {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("errorCode")).isEqualTo("GEOCODING_FAILED");
        assertThat(response.jsonPath().getString("field")).isEqualTo("origin");
    }

    // --- Helpers ---

    @SuppressWarnings("unchecked")
    private Map<String, Object> merge(Map<String, Object> base, String key, Object value) {
        java.util.HashMap<String, Object> map = new java.util.HashMap<>(base);
        map.put(key, value);
        return map;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mergeCargo(Map<String, Object> base, String key, Object value) {
        java.util.HashMap<String, Object> map = new java.util.HashMap<>(base);
        java.util.HashMap<String, Object> cargo = new java.util.HashMap<>((Map<String, Object>) base.get("cargo"));
        cargo.put(key, value);
        map.put("cargo", cargo);
        return map;
    }
}
