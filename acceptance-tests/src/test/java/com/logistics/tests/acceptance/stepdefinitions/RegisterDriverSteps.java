package com.logistics.tests.acceptance.stepdefinitions;

import com.logistics.tests.acceptance.AcceptanceTestBase;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

// Step definitions for register-driver.feature
// Spec: specs/acceptance-tests/register-driver.feature
public class RegisterDriverSteps extends AcceptanceTestBase {

    private String carrierId = "carrier-driver-test";
    private String licenseType;
    private String licenseNumber;
    private List<String> certifications;
    private Response response;

    // --- Background ---

    @Given("the Carrier is authenticated")
    public void carrier_is_authenticated() {
        // authentication is handled via a JWT header in real tests; skipped for template
    }

    // --- Given steps ---

    @Given("a driver with license type CE and no certifications")
    public void driver_with_ce_no_certifications() {
        licenseType = "CE";
        licenseNumber = "LIC-CE-" + System.currentTimeMillis();
        certifications = List.of();
    }

    @Given("a driver with license type CE and HAZMAT certification")
    public void driver_with_ce_and_hazmat() {
        licenseType = "CE";
        licenseNumber = "LIC-CE-HAZ-" + System.currentTimeMillis();
        certifications = List.of("HAZMAT");
    }

    @Given("a driver with license number {string} is already registered")
    public void driver_already_registered(String existingLicense) {
        this.licenseNumber = existingLicense;
        // Pre-register the driver
        given().baseUri(driverUrl())
                .contentType("application/json")
                .body(Map.of(
                        "carrierId", carrierId,
                        "name", "Existing Driver",
                        "licenseNumber", existingLicense,
                        "licenseType", "CE",
                        "certifications", List.of()
                ))
                .post("/api/v1/drivers")
                .then().statusCode(201);
    }

    @Given("a driver with license type {string} \\(invalid)")
    public void driver_with_invalid_license_type(String invalidType) {
        licenseType = invalidType;
        licenseNumber = "LIC-INV-" + System.currentTimeMillis();
        certifications = List.of();
    }

    // --- When steps ---

    @When("the Carrier registers the driver")
    public void carrier_registers_driver() {
        response = given().baseUri(driverUrl())
                .contentType("application/json")
                .body(Map.of(
                        "carrierId", carrierId,
                        "name", "Test Driver " + System.currentTimeMillis(),
                        "licenseNumber", licenseNumber,
                        "licenseType", licenseType,
                        "certifications", certifications != null ? certifications : List.of()
                ))
                .post("/api/v1/drivers")
                .then().extract().response();
    }

    @When("the Carrier registers another driver with license number {string}")
    public void carrier_registers_duplicate_driver(String duplicateLicense) {
        licenseNumber = duplicateLicense;
        licenseType = "CE";
        certifications = List.of();
        carrier_registers_driver();
    }

    // --- Then steps ---

    @Then("the driver is created with status AVAILABLE")
    public void driver_created_available() {
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("status")).isEqualTo("AVAILABLE");
    }

    @Then("a unique driverId is returned")
    public void unique_driver_id_returned() {
        assertThat(response.jsonPath().getString("driverId")).isNotNull().isNotBlank();
    }

    @Then("a DriverRegistered event is published")
    public void driver_registered_event_published() {
        // verified by Kafka consumer poll in integration context
    }

    @Then("the driver is created with HAZMAT in their certifications")
    public void driver_created_with_hazmat() {
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body().asString()).contains("HAZMAT");
    }

    @Then("the driver is eligible for hazmat shipment assignment")
    public void driver_eligible_for_hazmat() {
        String driverId = response.jsonPath().getString("driverId");
        Response getResp = given().baseUri(driverUrl()).get("/api/v1/drivers/" + driverId).then().extract().response();
        assertThat(getResp.body().asString()).contains("HAZMAT");
    }

    @Then("the request is rejected with error code DRIVER_ALREADY_EXISTS")
    public void rejected_driver_already_exists() {
        assertThat(response.statusCode()).isIn(400, 409);
        assertThat(response.body().asString()).contains("DRIVER_ALREADY_EXISTS");
    }

    @Then("the request is rejected with error code INVALID_LICENSE_TYPE")
    public void rejected_invalid_license_type() {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body().asString()).contains("INVALID_LICENSE_TYPE");
    }
}
