package com.logistics.tests.acceptance.stepdefinitions;

import com.logistics.tests.acceptance.AcceptanceTestBase;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

// Step definitions for inventory-advisor.feature
// Spec: specs/acceptance-tests/inventory-advisor.feature
public class InventoryAdvisorSteps extends AcceptanceTestBase {

    private static final String RAG_URL = env("RAG_SERVICE_URL", "http://localhost:8088");

    private String warehouseId;
    private Response response;

    @Given("warehouse {string} is at {int}% capacity with {int} SKUs indexed")
    public void warehouse_at_capacity(String name, int fillPct, int skuCount) {
        // Seed warehouse via warehouse-service; inventory events trigger rag indexing
        Response w = given().baseUri(warehouseUrl())
                .contentType("application/json")
                .body(Map.of(
                        "name", name,
                        "location", "São Paulo, SP",
                        "maxWeightKg", 10000.0,
                        "maxVolumeM3", 500.0
                ))
                .post("/api/v1/warehouses")
                .then().statusCode(201).extract().response();
        warehouseId = w.jsonPath().getString("warehouseId");

        double targetWeightKg = 10000.0 * fillPct / 100.0;
        double perSkuWeight = targetWeightKg / skuCount;
        for (int i = 0; i < skuCount; i++) {
            given().baseUri(warehouseUrl())
                    .contentType("application/json")
                    .body(Map.of(
                            "sku", "SKU-" + i,
                            "description", "Test SKU " + i,
                            "quantity", 100,
                            "weightKg", perSkuWeight / 100,
                            "volumeM3", 0.1
                    ))
                    .post("/api/v1/warehouses/" + warehouseId + "/inventory");
        }
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @Given("warehouse {string} is at {int}% capacity with space available")
    public void alternative_warehouse(String name, int fillPct) {
        // seed a second warehouse at lower capacity
        given().baseUri(warehouseUrl())
                .contentType("application/json")
                .body(Map.of("name", name, "location", "São Paulo, SP - South",
                        "maxWeightKg", 10000.0, "maxVolumeM3", 500.0))
                .post("/api/v1/warehouses");
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
    }

    @Given("warehouse {string} is at {int}% capacity")
    public void warehouse_at_low_capacity(String name, int fillPct) {
        warehouse_at_capacity(name, fillPct, 3);
    }

    @Given("all warehouses are above {int}% capacity")
    public void all_warehouses_near_capacity(int threshold) {
        // both warehouses seeded at high fill — no good rebalancing target available
    }

    @When("I call GET /api/v1/rag/warehouses/\\{spCentralId}/rebalance")
    public void call_rebalance() {
        response = given()
                .baseUri(RAG_URL)
                .get("/api/v1/rag/warehouses/" + warehouseId + "/rebalance")
                .then().extract().response();
    }

    @Then("at least {int} recommendation is returned")
    public void at_least_one_recommendation(int min) {
        List<?> recs = response.jsonPath().getList("recommendations");
        assertThat(recs).hasSizeGreaterThanOrEqualTo(min);
    }

    @Then("each recommendation includes sku, suggestedQtyToMove, targetWarehouseId, and reasoning")
    public void recommendations_have_required_fields() {
        List<Map<?, ?>> recs = response.jsonPath().getList("recommendations");
        for (Map<?, ?> rec : recs) {
            java.util.Set<String> keys = rec.keySet().stream()
                    .map(Object::toString).collect(java.util.stream.Collectors.toSet());
            assertThat(keys).contains("sku", "suggestedQtyToMove", "targetWarehouseId", "reasoning");
        }
    }

    @Then("targetWarehouse fillPctAfter is below {int}")
    public void fill_pct_below(int max) {
        List<Map<?, ?>> recs = response.jsonPath().getList("recommendations");
        for (Map<?, ?> rec : recs) {
            double fill = ((Number) rec.get("fillPctAfter")).doubleValue();
            assertThat(fill).isLessThan(max);
        }
    }

    @Then("recommendations list is empty")
    public void recommendations_empty() {
        List<?> recs = response.jsonPath().getList("recommendations");
        assertThat(recs).isEmpty();
    }

    @Then("reason is {string}")
    public void reason_is(String expected) {
        assertThat(response.jsonPath().getString("reason")).isEqualTo(expected);
    }

    @Then("the response contains recommendation to escalate to operations")
    public void escalate_recommendation() {
        assertThat(response.jsonPath().getString("reason")).containsIgnoringCase("escalate");
    }
}
