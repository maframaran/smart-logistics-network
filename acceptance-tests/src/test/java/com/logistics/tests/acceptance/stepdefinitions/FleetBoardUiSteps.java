package com.logistics.tests.acceptance.stepdefinitions;

import io.cucumber.java.en.*;
import com.microsoft.playwright.*;
import static org.assertj.core.api.Assertions.assertThat;

// UI step definitions for fleet-board-ui.feature
// Spec: specs/acceptance-tests/fleet-board-ui.feature
public class FleetBoardUiSteps {

    private static final String BASE_URL = System.getenv().getOrDefault("UI_BASE_URL", "http://localhost:3000");
    private Page page;

    // "I am authenticated as a Carrier" — shared step

    @Given("I have {int} vehicles and {int} drivers registered")
    public void i_have_vehicles_and_drivers(int vehicles, int drivers) {
        // precondition — seeded via fleet-service and driver-service APIs
    }

    @When("I navigate to \\/fleet")
    public void navigate_to_fleet() {
        page.navigate(BASE_URL + "/fleet");
    }

    @When("I view the fleet board")
    public void i_view_fleet_board() {
        navigate_to_fleet();
    }

    @Then("I see {int} vehicle cards on the left")
    public void i_see_vehicle_cards(int count) {
        page.waitForSelector("[data-testid='vehicle-card']");
        assertThat(page.locator("[data-testid='vehicle-card']").count()).isEqualTo(count);
    }

    @Then("I see {int} driver cards on the right")
    public void i_see_driver_cards(int count) {
        assertThat(page.locator("[data-testid='driver-card']").count()).isEqualTo(count);
    }

    @Given("I have an AVAILABLE vehicle and an ASSIGNED vehicle")
    public void i_have_available_and_assigned_vehicle() {
        // precondition — seeded via API
    }

    @Then("the AVAILABLE vehicle card shows a green {string} badge")
    public void available_vehicle_green_badge(String badgeText) {
        Locator card = page.locator("[data-testid='vehicle-card'][data-status='AVAILABLE']");
        assertThat(card.locator("[data-testid='status-badge']:has-text('" + badgeText + "')").isVisible()).isTrue();
    }

    @Then("the ASSIGNED vehicle card shows a blue {string} badge")
    public void assigned_vehicle_blue_badge(String badgeText) {
        Locator card = page.locator("[data-testid='vehicle-card'][data-status='ASSIGNED']");
        assertThat(card.locator("[data-testid='status-badge']:has-text('" + badgeText + "')").isVisible()).isTrue();
    }

    @Given("I have a vehicle of type HAZMAT_TRUCK")
    public void i_have_hazmat_truck() {
        // precondition — seeded via API
    }

    @Then("the vehicle card shows a {string} badge")
    public void vehicle_card_shows_badge(String badgeText) {
        assertThat(page.locator("[data-testid='vehicle-card'] [data-testid='" + badgeText.toLowerCase() + "-badge']")
                .first().isVisible()).isTrue();
    }

    @Given("I have a vehicle of type REFRIGERATED_TRUCK")
    public void i_have_refrigerated_truck() {
        // precondition — seeded via API
    }

    @Given("a driver has driven {double} hours today")
    public void driver_has_driven(double hours) {
        // precondition — driving session recorded via driver-service API
    }

    @Then("the driver card shows a progress bar at {int}% ({int} of {int} hours)")
    public void driver_progress_bar_percentage(int percent, int driven, int max) {
        Locator bar = page.locator("[data-testid='driver-card'] [data-testid='hours-bar']").first();
        assertThat(bar.getAttribute("aria-valuenow")).isEqualTo(String.valueOf(driven));
        assertThat(bar.getAttribute("aria-valuemax")).isEqualTo(String.valueOf(max));
    }

    @Then("the progress bar is green")
    public void progress_bar_is_green() {
        assertThat(page.locator("[data-testid='hours-bar'][data-color='green']").first().isVisible()).isTrue();
    }

    @Then("the driver's progress bar is red")
    public void progress_bar_is_red() {
        assertThat(page.locator("[data-testid='hours-bar'][data-color='red']").first().isVisible()).isTrue();
    }

    @Then("shows {string}")
    public void progress_bar_shows_label(String label) {
        assertThat(page.locator("[data-testid='hours-label']").first().textContent()).isEqualTo(label);
    }

    @Then("the driver shows status {string}")
    public void driver_shows_status(String status) {
        assertThat(page.locator("[data-testid='driver-card'] [data-testid='status-badge']:has-text('" + status + "')")
                .first().isVisible()).isTrue();
    }

    @Then("the progress bar is full red")
    public void progress_bar_full_red() {
        Locator bar = page.locator("[data-testid='hours-bar'][data-color='red']").first();
        assertThat(bar.getAttribute("aria-valuenow")).isEqualTo("9");
    }

    @Given("I have {int} AVAILABLE and {int} MAINTENANCE vehicle")
    public void i_have_mixed_vehicles(int available, int maintenance) {
        // precondition — seeded via API
    }

    @When("I select the {string} filter on the vehicle column")
    public void i_select_vehicle_filter(String filterLabel) {
        page.click("[data-testid='vehicle-filter'][data-value='" + filterLabel.toUpperCase() + "']");
    }

    @Then("only the {int} AVAILABLE vehicles are shown")
    public void only_available_vehicles_shown(int count) {
        assertThat(page.locator("[data-testid='vehicle-card']").count()).isEqualTo(count);
        page.locator("[data-testid='vehicle-card']").all().forEach(card ->
                assertThat(card.getAttribute("data-status")).isEqualTo("AVAILABLE"));
    }

    @Given("I have no vehicles registered")
    public void no_vehicles_registered() {
        // precondition — empty account
    }

    @Then("I see {string} call-to-action")
    public void i_see_cta(String text) {
        assertThat(page.locator("text=" + text).isVisible()).isTrue();
    }
}
