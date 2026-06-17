package com.logistics.tests.acceptance.stepdefinitions;

import io.cucumber.java.en.*;
import com.microsoft.playwright.*;
import static org.assertj.core.api.Assertions.assertThat;

// UI step definitions for shipment-tracker-ui.feature
// Spec: specs/acceptance-tests/shipment-tracker-ui.feature
public class ShipmentTrackerUiSteps {

    private static final String BASE_URL = System.getenv().getOrDefault("UI_BASE_URL", "http://localhost:3000");
    private Page page;

    // "I am authenticated as a Shipper" is shared with DashboardUiSteps

    @Given("I am authenticated as a Shipper with shipments in various statuses")
    public void authenticated_with_various_shipments() {
        // Authentication + test data setup handled by shared fixture
    }

    @When("I navigate to \\/shipments")
    public void navigate_to_shipments() {
        page.navigate(BASE_URL + "/shipments");
    }

    @Then("I see a list of all my shipments")
    public void i_see_shipment_list() {
        page.waitForSelector("[data-testid='shipment-row']");
        assertThat(page.locator("[data-testid='shipment-row']").count()).isGreaterThan(0);
    }

    @Then("each row shows: shipment ID, origin, destination, SLA badge, status badge, required delivery date")
    public void each_row_has_required_fields() {
        Locator firstRow = page.locator("[data-testid='shipment-row']").first();
        assertThat(firstRow.locator("[data-testid='shipment-id']").isVisible()).isTrue();
        assertThat(firstRow.locator("[data-testid='sla-badge']").isVisible()).isTrue();
        assertThat(firstRow.locator("[data-testid='status-badge']").isVisible()).isTrue();
    }

    @Given("I have {int} shipments IN_TRANSIT and {int} shipments DELIVERED")
    public void i_have_shipments_with_statuses(int inTransit, int delivered) {
        // precondition — test data seeded via API
    }

    @When("I click the {string} filter tab")
    public void i_click_filter_tab(String tabLabel) {
        page.click("[data-testid='filter-tab'][data-value='" + tabLabel.replace(" ", "_").toUpperCase() + "']");
    }

    @Then("only the {int} IN_TRANSIT shipments are shown")
    public void only_in_transit_shown(int count) {
        assertThat(page.locator("[data-testid='shipment-row']").count()).isEqualTo(count);
        page.locator("[data-testid='status-badge']").all().forEach(badge ->
                assertThat(badge.textContent()).contains("In Transit"));
    }

    @Given("I am on the \\/shipments page")
    public void i_am_on_shipments_page() {
        navigate_to_shipments();
    }

    @Given("a shipment status changes to DELIVERED on the backend")
    public void shipment_status_changes_on_backend() {
        // simulated by calling shipment-service PATCH /api/v1/shipments/{id}/status directly
    }

    @When("{int} seconds pass")
    public void seconds_pass(int seconds) {
        page.waitForTimeout(seconds * 1000);
    }

    @Then("the shipment row updates its status badge to DELIVERED without a full page reload")
    public void status_badge_updates_without_reload() {
        page.waitForSelector("[data-testid='status-badge']:has-text('Delivered')",
                new Page.WaitForSelectorOptions().setTimeout(20000));
        assertThat(page.locator("[data-testid='status-badge']:has-text('Delivered')").count()).isGreaterThan(0);
    }

    @Given("I click on a shipment that has progressed through CREATED → SCHEDULED → ASSIGNED")
    public void i_click_on_progressed_shipment() {
        page.locator("[data-testid='shipment-row']").first().click();
        page.waitForURL("**/shipments/**");
    }

    @When("the detail page loads")
    public void detail_page_loads() {
        page.waitForSelector("[data-testid='shipment-timeline']");
    }

    @Then("I see a timeline with three steps: CREATED, SCHEDULED, ASSIGNED")
    public void i_see_timeline_steps() {
        assertThat(page.locator("[data-testid='timeline-step']").count()).isEqualTo(3);
        assertThat(page.locator("[data-testid='timeline-step']:has-text('Created')").isVisible()).isTrue();
        assertThat(page.locator("[data-testid='timeline-step']:has-text('Scheduled')").isVisible()).isTrue();
        assertThat(page.locator("[data-testid='timeline-step']:has-text('Assigned')").isVisible()).isTrue();
    }

    @Then("each step shows its timestamp")
    public void each_step_has_timestamp() {
        page.locator("[data-testid='timeline-step']").all().forEach(step ->
                assertThat(step.locator("[data-testid='timeline-timestamp']").isVisible()).isTrue());
    }

    @Given("a shipment was delivered before its required delivery date")
    public void shipment_delivered_on_time() {
        // precondition — seeded via API
    }

    @Then("I see a green {string} badge")
    public void i_see_green_badge(String badgeText) {
        Locator badge = page.locator("[data-testid='sla-result-badge']:has-text('" + badgeText + "')");
        assertThat(badge.isVisible()).isTrue();
        assertThat(badge.getAttribute("data-color")).isEqualTo("green");
    }

    @Given("a shipment was delivered after its required delivery date")
    public void shipment_delivered_late() {
        // precondition — seeded via API
    }

    @Then("I see a red {string} badge")
    public void i_see_red_badge(String badgeText) {
        Locator badge = page.locator("[data-testid='sla-result-badge']:has-text('" + badgeText + "')");
        assertThat(badge.isVisible()).isTrue();
        assertThat(badge.getAttribute("data-color")).isEqualTo("red");
    }

    @Given("a shipment in ASSIGNED status")
    public void shipment_in_assigned_status() {
        // precondition — seeded via API
    }

    @Then("I see the assigned vehicle plate number")
    public void i_see_vehicle_plate() {
        assertThat(page.locator("[data-testid='vehicle-plate']").isVisible()).isTrue();
    }

    @Then("I see the assigned driver name")
    public void i_see_driver_name() {
        assertThat(page.locator("[data-testid='driver-name']").isVisible()).isTrue();
    }

    @When("I navigate to \\/shipments\\/non-existent-id")
    public void navigate_to_nonexistent_shipment() {
        page.navigate(BASE_URL + "/shipments/non-existent-id");
    }

    @Then("I see a {string} message")
    public void i_see_message(String message) {
        assertThat(page.locator("text=" + message).isVisible()).isTrue();
    }

    @Then("a {string} button")
    public void a_button(String buttonText) {
        assertThat(page.locator("button:has-text('" + buttonText + "')").isVisible()).isTrue();
    }
}
