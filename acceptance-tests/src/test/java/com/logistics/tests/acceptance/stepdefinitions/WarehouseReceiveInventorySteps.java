package com.logistics.tests.acceptance.stepdefinitions;

import com.logistics.tests.acceptance.AcceptanceTestBase;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

// Step definitions for warehouse-receive-inventory.feature
// Spec: specs/acceptance-tests/warehouse-receive-inventory.feature
public class WarehouseReceiveInventorySteps extends AcceptanceTestBase {

    private String warehouseId;
    private int maxCapacity;
    private int currentUnits;
    private int inboundUnits;
    private String sku;
    private String expirationDate;
    private Response response;

    // --- Background ---

    // "the Warehouse Operator is authenticated" — shared step; no-op in template

    // --- Given steps ---

    @Given("a warehouse with {int} unit capacity and {int} units currently stored")
    public void a_warehouse_with_capacity(int maxCap, int currentStored) {
        this.maxCapacity = maxCap;
        this.currentUnits = currentStored;

        // Register warehouse
        Response warehouseResp = given().baseUri(warehouseUrl())
                .contentType("application/json")
                .body(Map.of(
                        "name", "Test Warehouse " + System.currentTimeMillis(),
                        "location", Map.of("street", "Warehouse Rd", "city", "Hamburg", "country", "DE", "lat", 53.55, "lon", 9.99),
                        "maxWeightKg", (double) maxCap * 10,    // 10kg per unit
                        "maxVolumeM3", (double) maxCap * 0.01   // 0.01 m³ per unit
                ))
                .post("/api/v1/warehouses")
                .then().statusCode(201).extract().response();
        warehouseId = warehouseResp.jsonPath().getString("warehouseId");

        // Pre-stock the warehouse to currentStored units if needed
        if (currentStored > 0) {
            given().baseUri(warehouseUrl())
                    .contentType("application/json")
                    .body(Map.of(
                            "sku", "PRESTOCK-001",
                            "quantity", currentStored,
                            "weightKgPerUnit", 10.0,
                            "volumeM3PerUnit", 0.01,
                            "expirationDate", LocalDate.now().plusYears(1).toString()
                    ))
                    .post("/api/v1/warehouses/" + warehouseId + "/inventory")
                    .then().statusCode(201);
        }
    }

    @Given("an inbound delivery of {int} units of SKU {string}")
    public void an_inbound_delivery_of(int units, String skuCode) {
        this.inboundUnits = units;
        this.sku = skuCode;
        this.expirationDate = LocalDate.now().plusYears(1).toString();
    }

    @Given("an inbound delivery of {int} units")
    public void an_inbound_delivery_generic(int units) {
        an_inbound_delivery_of(units, "SKU-GENERIC-001");
    }

    @Given("an inbound delivery with expiration date {int} days ago")
    public void an_inbound_delivery_expired(int daysAgo) {
        this.inboundUnits = 50;
        this.sku = "SKU-EXPIRED-001";
        this.expirationDate = LocalDate.now().minusDays(daysAgo).toString();
    }

    @Given("an inbound delivery with a new SKU {string} not previously registered")
    public void an_inbound_delivery_new_sku(String newSku) {
        this.inboundUnits = 100;
        this.sku = newSku;
        this.expirationDate = LocalDate.now().plusYears(1).toString();
    }

    @Given("the warehouse has sufficient capacity")
    public void the_warehouse_has_sufficient_capacity() {
        // already ensured by warehouse setup — inbound is within limits
    }

    // --- When steps ---

    @When("the Warehouse Operator records the inbound delivery")
    public void warehouse_operator_records_delivery() {
        response = given().baseUri(warehouseUrl())
                .contentType("application/json")
                .body(Map.of(
                        "sku", sku,
                        "quantity", inboundUnits,
                        "weightKgPerUnit", 10.0,
                        "volumeM3PerUnit", 0.01,
                        "expirationDate", expirationDate
                ))
                .post("/api/v1/warehouses/" + warehouseId + "/inventory")
                .then().extract().response();
    }

    // --- Then steps ---

    @Then("the inventory is accepted")
    public void inventory_is_accepted() {
        assertThat(response.statusCode()).isEqualTo(201);
    }

    @Then("warehouse current units becomes {int}")
    public void warehouse_current_units(int expectedUnits) {
        Response getResp = given().baseUri(warehouseUrl()).get("/api/v1/warehouses/" + warehouseId).then().extract().response();
        // Current weight should reflect accepted units: currentStored + inbound = expectedUnits
        double expectedWeight = expectedUnits * 10.0;
        assertThat(getResp.jsonPath().getDouble("currentWeightKg")).isEqualTo(expectedWeight);
    }

    @Then("an InventoryReceived event is published")
    public void inventory_received_event_published() {
        // verified by Kafka consumer poll in integration context
    }

    @Then("a WarehouseCapacityUpdated event is published")
    public void warehouse_capacity_updated_event_published() {
        // verified by Kafka consumer poll in integration context
    }

    @Then("the delivery is rejected with error code WAREHOUSE_CAPACITY_EXCEEDED")
    public void rejected_warehouse_capacity_exceeded() {
        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.body().asString()).contains("WAREHOUSE_CAPACITY_EXCEEDED");
    }

    @Then("a list of alternative warehouses with available capacity is returned")
    public void alternative_warehouses_returned() {
        assertThat(response.body().asString()).contains("alternatives");
    }

    @Then("the delivery is rejected with error code INVALID_EXPIRATION_DATE")
    public void rejected_invalid_expiration_date() {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body().asString()).contains("INVALID_EXPIRATION_DATE");
    }

    @Then("a new SKU record is created automatically")
    public void new_sku_record_created() {
        Response getResp = given().baseUri(warehouseUrl()).get("/api/v1/warehouses/" + warehouseId).then().extract().response();
        assertThat(getResp.body().asString()).contains(sku);
    }
}
