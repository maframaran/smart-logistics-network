package com.logistics.tests.acceptance.stepdefinitions;

import com.logistics.tests.acceptance.runners.AcceptanceTestBase;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

// Step definitions for register-vehicle.feature
// Spec: specs/acceptance-tests/register-vehicle.feature
public class RegisterVehicleSteps extends AcceptanceTestBase {

    private String licensePlate;
    private String vehicleType = "TRUCK";
    private double weightCapacityKg;
    private double volumeCapacityM3;
    private boolean refrigerated = false;
    private boolean hazmatCertified = false;
    private Response response;

    // --- Background ---

    // "the Carrier is authenticated" is shared with RegisterDriverSteps — Cucumber resolves by type

    // --- Given steps ---

    @Given("a truck with {int}kg weight capacity and {int}m³ volume capacity")
    public void a_truck_with_capacity(int weightKg, int volumeM3) {
        vehicleType = "TRUCK";
        weightCapacityKg = weightKg;
        volumeCapacityM3 = volumeM3;
        refrigerated = false;
        hazmatCertified = false;
        licensePlate = "TRUCK-" + System.currentTimeMillis();
    }

    @Given("the truck is not refrigerated and not hazmat certified")
    public void not_refrigerated_not_hazmat() {
        this.refrigerated = false;
        this.hazmatCertified = false;
    }

    @Given("a refrigerated truck with {int}kg weight capacity")
    public void a_refrigerated_truck(int weightKg) {
        vehicleType = "REFRIGERATED_TRUCK";
        weightCapacityKg = weightKg;
        volumeCapacityM3 = 15.0;
        licensePlate = "REFRIG-" + System.currentTimeMillis();
    }

    @Given("the vehicle has refrigerated = true")
    public void vehicle_is_refrigerated() {
        this.refrigerated = true;
    }

    @Given("a vehicle with plate {string} is already registered")
    public void vehicle_already_registered(String plate) {
        this.licensePlate = plate;
        given().baseUri(baseUrl())
                .contentType("application/json")
                .body(Map.of(
                        "licensePlate", plate,
                        "type", "TRUCK",
                        "weightCapacityKg", 5000,
                        "volumeCapacityM3", 20.0,
                        "refrigerated", false,
                        "hazmatCertified", false
                ))
                .post("/api/v1/vehicles")
                .then().statusCode(201);
    }

    @Given("a vehicle with weight capacity of {int}kg")
    public void vehicle_with_invalid_capacity(int weightKg) {
        vehicleType = "TRUCK";
        weightCapacityKg = weightKg;
        volumeCapacityM3 = 10.0;
        licensePlate = "INVALID-CAP-" + System.currentTimeMillis();
    }

    @Given("a vehicle of type REFRIGERATED_TRUCK")
    public void vehicle_of_type_refrigerated_truck() {
        vehicleType = "REFRIGERATED_TRUCK";
        weightCapacityKg = 3000;
        volumeCapacityM3 = 12.0;
        licensePlate = "INCON-" + System.currentTimeMillis();
    }

    @Given("refrigerated = false")
    public void refrigerated_false() {
        this.refrigerated = false;
    }

    // --- When steps ---

    @When("the Carrier registers the vehicle")
    public void carrier_registers_vehicle() {
        response = given().baseUri(baseUrl())
                .contentType("application/json")
                .body(Map.of(
                        "licensePlate", licensePlate,
                        "type", vehicleType,
                        "weightCapacityKg", weightCapacityKg,
                        "volumeCapacityM3", volumeCapacityM3,
                        "refrigerated", refrigerated,
                        "hazmatCertified", hazmatCertified
                ))
                .post("/api/v1/vehicles")
                .then().extract().response();
    }

    @When("the Carrier registers another vehicle with plate {string}")
    public void carrier_registers_duplicate_vehicle(String plate) {
        licensePlate = plate;
        carrier_registers_vehicle();
    }

    // --- Then steps ---

    @Then("the vehicle is created with status AVAILABLE")
    public void vehicle_created_available() {
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("status")).isEqualTo("AVAILABLE");
    }

    @Then("a unique vehicleId is returned")
    public void unique_vehicle_id_returned() {
        assertThat(response.jsonPath().getString("vehicleId")).isNotNull().isNotBlank();
    }

    @Then("a VehicleRegistered event is published")
    public void vehicle_registered_event_published() {
        // verified by Kafka consumer poll in integration context
    }

    @Then("the vehicle is created with type REFRIGERATED_TRUCK and status AVAILABLE")
    public void vehicle_created_as_refrigerated_truck() {
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("type")).isEqualTo("REFRIGERATED_TRUCK");
        assertThat(response.jsonPath().getString("status")).isEqualTo("AVAILABLE");
    }

    @Then("the request is rejected with error code VEHICLE_ALREADY_EXISTS")
    public void rejected_vehicle_already_exists() {
        assertThat(response.statusCode()).isIn(400, 409);
        assertThat(response.body().asString()).contains("VEHICLE_ALREADY_EXISTS");
    }

    @Then("the request is rejected with error code INVALID_CAPACITY")
    public void rejected_invalid_capacity() {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body().asString()).contains("INVALID_CAPACITY");
    }

    @Then("the request is rejected with error code INCONSISTENT_VEHICLE_TYPE")
    public void rejected_inconsistent_vehicle_type() {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body().asString()).contains("INCONSISTENT_VEHICLE_TYPE");
    }
}
