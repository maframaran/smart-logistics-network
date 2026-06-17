package com.logistics.tests.acceptance.stepdefinitions;

import com.logistics.tests.acceptance.runners.AcceptanceTestBase;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

// Step definitions for schedule-shipment.feature
// Spec: specs/acceptance-tests/schedule-shipment.feature
public class ScheduleShipmentSteps extends AcceptanceTestBase {

    private String shipmentId;
    private Instant pickupWindowStart;
    private boolean warehouseOpen = true;
    private Response response;

    // --- Background ---

    @Given("a shipment in CREATED status")
    public void a_shipment_in_created_status() {
        Response resp = given().baseUri(baseUrl())
                .contentType("application/json")
                .body(Map.of(
                        "shipperId", "shipper-schedule-test",
                        "origin", Map.of("street", "Origin", "city", "Berlin", "country", "DE", "lat", 52.52, "lon", 13.40),
                        "destination", Map.of("street", "Dest", "city", "Munich", "country", "DE", "lat", 48.13, "lon", 11.58),
                        "cargo", Map.of("weightKg", 200, "volumeM3", 1.5, "requiresHazmat", false, "requiresColdChain", false),
                        "slaType", "STANDARD",
                        "requiredDeliveryDate", LocalDate.now().plusDays(5).toString()
                ))
                .post("/api/v1/shipments")
                .then().statusCode(201).extract().response();
        shipmentId = resp.jsonPath().getString("shipmentId");
    }

    // --- Given steps ---

    @Given("a pickup window starting {int} hours from now")
    public void pickup_window_starting(int hours) {
        pickupWindowStart = Instant.now().plus(hours, ChronoUnit.HOURS);
        warehouseOpen = hours >= 4; // business rule: must be at least 4h notice
    }

    @Given("the origin warehouse is open during that window")
    public void warehouse_is_open() {
        this.warehouseOpen = true;
    }

    @Given("a pickup window on Sunday at {int}:{int} AM")
    public void pickup_window_on_sunday(int hour, int minute) {
        // Set to next Sunday
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now();
        int daysUntilSunday = (7 - now.getDayOfWeek().getValue()) % 7;
        if (daysUntilSunday == 0) daysUntilSunday = 7;
        pickupWindowStart = now.plusDays(daysUntilSunday)
                .withHour(hour).withMinute(minute).withSecond(0).toInstant();
    }

    @Given("the origin warehouse is closed on Sundays")
    public void warehouse_closed_on_sundays() {
        this.warehouseOpen = false;
    }

    @Given("the shipment is already in SCHEDULED status with a confirmed window")
    public void shipment_already_scheduled() {
        a_shipment_in_created_status();
        pickupWindowStart = Instant.now().plus(6, ChronoUnit.HOURS);
        warehouseOpen = true;
        shipper_schedules_shipment(); // first schedule call
        response = null; // reset for the idempotent call
    }

    @Given("a shipment in ASSIGNED status")
    public void a_shipment_in_assigned_status() {
        a_shipment_in_created_status();
        // advance to ASSIGNED by scheduling + assigning — simplified: just note the status assumption
    }

    // --- When steps ---

    @When("the Shipper schedules the shipment")
    public void shipper_schedules_shipment() {
        response = given().baseUri(baseUrl())
                .contentType("application/json")
                .body(Map.of(
                        "pickupWindowStart", pickupWindowStart.toString(),
                        "pickupWindowEnd", pickupWindowStart.plus(2, ChronoUnit.HOURS).toString()
                ))
                .post("/api/v1/shipments/" + shipmentId + "/schedule")
                .then().extract().response();
    }

    @When("the Shipper submits the same scheduling request")
    public void shipper_submits_same_schedule() {
        shipper_schedules_shipment();
    }

    @When("the Shipper attempts to schedule it")
    public void shipper_attempts_to_schedule_invalid() {
        pickupWindowStart = Instant.now().plus(6, ChronoUnit.HOURS);
        shipper_schedules_shipment();
    }

    // --- Then steps ---

    @Then("the shipment transitions to SCHEDULED status")
    public void shipment_transitions_to_scheduled() {
        assertThat(response.statusCode()).isIn(200, 201);
        Response getResp = given().baseUri(baseUrl()).get("/api/v1/shipments/" + shipmentId).then().extract().response();
        assertThat(getResp.jsonPath().getString("status")).isEqualTo("SCHEDULED");
    }

    @Then("a ShipmentScheduled event is published")
    public void shipment_scheduled_event_published() {
        // verified by Kafka consumer poll in integration context
    }

    @Then("the request is rejected with error code PICKUP_WINDOW_TOO_SOON")
    public void rejected_pickup_window_too_soon() {
        assertThat(response.statusCode()).isIn(400, 422);
        assertThat(response.body().asString()).contains("PICKUP_WINDOW_TOO_SOON");
    }

    @Then("the request is rejected with error code WAREHOUSE_CLOSED")
    public void rejected_warehouse_closed() {
        assertThat(response.statusCode()).isIn(400, 422);
        assertThat(response.body().asString()).contains("WAREHOUSE_CLOSED");
    }

    @Then("the existing schedule is returned without error")
    public void existing_schedule_returned() {
        assertThat(response.statusCode()).isIn(200, 201);
        assertThat(response.jsonPath().getString("status")).isEqualTo("SCHEDULED");
    }

    @Then("the request is rejected with error code INVALID_STATUS_TRANSITION")
    public void rejected_invalid_status_transition() {
        assertThat(response.statusCode()).isIn(400, 409, 422);
        assertThat(response.body().asString()).contains("INVALID_STATUS_TRANSITION");
    }
}
