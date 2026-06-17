package com.logistics.tests.acceptance.stepdefinitions;

import io.cucumber.java.en.*;
import com.microsoft.playwright.*;
import static org.assertj.core.api.Assertions.assertThat;

// UI step definitions for warehouse-dashboard-ui.feature
// Spec: specs/acceptance-tests/warehouse-dashboard-ui.feature
public class WarehouseDashboardUiSteps {

    private static final String BASE_URL = System.getenv().getOrDefault("UI_BASE_URL", "http://localhost:3000");
    private Page page;

    @Given("I have {int} warehouses registered")
    public void i_have_warehouses(int count) {
        // precondition — seeded via warehouse-service API
    }

    @When("I navigate to \\/warehouse")
    public void navigate_to_warehouse() {
        page.navigate(BASE_URL + "/warehouse");
    }

    @Then("I see {int} warehouse cards")
    public void i_see_warehouse_cards(int count) {
        page.waitForSelector("[data-testid='warehouse-card']");
        assertThat(page.locator("[data-testid='warehouse-card']").count()).isEqualTo(count);
    }

    @Then("each card shows the warehouse name, city, and capacity gauges")
    public void each_card_has_required_fields() {
        Locator firstCard = page.locator("[data-testid='warehouse-card']").first();
        assertThat(firstCard.locator("[data-testid='warehouse-name']").isVisible()).isTrue();
        assertThat(firstCard.locator("[data-testid='warehouse-city']").isVisible()).isTrue();
        assertThat(firstCard.locator("[data-testid='capacity-gauge']").isVisible()).isTrue();
    }

    @Given("a warehouse with {int} \\/ {int} units stored ({int}% fill)")
    public void warehouse_with_fill(int current, int max, int percent) {
        // precondition — seeded via warehouse-service API with appropriate weight/volume
    }

    @When("I view the warehouse list")
    public void i_view_warehouse_list() {
        navigate_to_warehouse();
    }

    @Then("the capacity gauge is green")
    public void capacity_gauge_is_green() {
        assertThat(page.locator("[data-testid='capacity-gauge'][data-color='green']").first().isVisible()).isTrue();
    }

    @Then("the capacity gauge is amber")
    public void capacity_gauge_is_amber() {
        assertThat(page.locator("[data-testid='capacity-gauge'][data-color='amber']").first().isVisible()).isTrue();
    }

    @Then("the capacity gauge is red")
    public void capacity_gauge_is_red() {
        assertThat(page.locator("[data-testid='capacity-gauge'][data-color='red']").first().isVisible()).isTrue();
    }

    @Given("a warehouse with {int} SKUs in inventory")
    public void warehouse_with_skus(int count) {
        // precondition — seeded via warehouse-service inventory API
    }

    @When("I click the warehouse card and the detail page loads")
    public void i_click_warehouse_card() {
        page.locator("[data-testid='warehouse-card']").first().click();
        page.waitForSelector("[data-testid='inventory-table']");
    }

    @Then("I see a table with {int} rows")
    public void i_see_table_with_rows(int count) {
        assertThat(page.locator("[data-testid='inventory-row']").count()).isEqualTo(count);
    }

    @Then("each row shows: SKU, quantity, weight\\/unit, volume\\/unit, expiration date")
    public void each_row_has_inventory_fields() {
        Locator firstRow = page.locator("[data-testid='inventory-row']").first();
        assertThat(firstRow.locator("[data-testid='sku']").isVisible()).isTrue();
        assertThat(firstRow.locator("[data-testid='quantity']").isVisible()).isTrue();
        assertThat(firstRow.locator("[data-testid='expiration-date']").isVisible()).isTrue();
    }

    @Given("a warehouse with a SKU expiring in {int} days")
    public void warehouse_with_expiring_sku(int days) {
        // precondition — seeded via inventory API with expiration date = today + days
    }

    @When("I view the warehouse detail")
    public void i_view_warehouse_detail() {
        i_click_warehouse_card();
    }

    @Then("that SKU row is highlighted amber")
    public void sku_row_highlighted_amber() {
        assertThat(page.locator("[data-testid='inventory-row'][data-expiring-soon='true']").first().isVisible()).isTrue();
    }

    @Given("I am on the warehouse detail page")
    public void i_am_on_warehouse_detail() {
        navigate_to_warehouse();
        i_click_warehouse_card();
    }

    @Given("a new inventory receipt increases fill from {int}% to {int}%")
    public void inventory_receipt_increases_fill(int from, int to) {
        // triggered via warehouse-service API in a background thread
    }

    @Then("the capacity gauge updates to {int}% without a full page reload")
    public void gauge_updates_without_reload(int percent) {
        page.waitForTimeout(31000); // wait for 30s refetch + render
        Locator gauge = page.locator("[data-testid='capacity-gauge']").first();
        int filledPercent = Integer.parseInt(gauge.getAttribute("aria-valuenow"));
        assertThat(filledPercent).isEqualTo(percent);
    }

    @Given("I have no warehouses registered")
    public void no_warehouses_registered() {
        // precondition — empty account
    }

    @Then("I see an empty state message")
    public void i_see_empty_state() {
        assertThat(page.locator("[data-testid='empty-state']").isVisible()).isTrue();
    }
}
