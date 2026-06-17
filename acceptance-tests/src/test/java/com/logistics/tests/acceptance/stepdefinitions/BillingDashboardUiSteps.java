package com.logistics.tests.acceptance.stepdefinitions;

import io.cucumber.java.en.*;
import com.microsoft.playwright.*;
import static org.assertj.core.api.Assertions.assertThat;

// UI step definitions for billing-dashboard-ui.feature
// Spec: specs/acceptance-tests/billing-dashboard-ui.feature
public class BillingDashboardUiSteps {

    private static final String BASE_URL = System.getenv().getOrDefault("UI_BASE_URL", "http://localhost:3000");
    private Page page;

    @Given("I have {int} invoices ({int} PAID, {int} PENDING, {int} OVERDUE)")
    public void i_have_invoices(int total, int paid, int pending, int overdue) {
        // precondition — seeded via billing-service API
    }

    @When("I navigate to \\/billing")
    public void navigate_to_billing() {
        page.navigate(BASE_URL + "/billing");
    }

    @Then("I see all {int} invoices in the table")
    public void i_see_all_invoices(int count) {
        page.waitForSelector("[data-testid='invoice-row']");
        assertThat(page.locator("[data-testid='invoice-row']").count()).isEqualTo(count);
    }

    @Given("an invoice with an SLA penalty of BRL {int}")
    public void invoice_with_sla_penalty(int penalty) {
        // precondition — seeded via billing-service API with slaPenaltyBrl > 0
    }

    @When("I view the invoice list")
    public void i_view_invoice_list() {
        navigate_to_billing();
    }

    @Then("that invoice row has a red left border")
    public void invoice_row_has_red_border() {
        assertThat(page.locator("[data-testid='invoice-row'][data-has-penalty='true']").first().isVisible()).isTrue();
        // CSS class assertion: row must have class 'border-l-4 border-red-500'
        assertThat(page.locator("[data-testid='invoice-row'][data-has-penalty='true']").first()
                .getAttribute("class")).contains("border-red");
    }

    @Given("an invoice with SLA penalty of BRL {int}")
    public void invoice_without_penalty(int penalty) {
        // precondition — seeded with penalty = 0
    }

    @Then("that invoice row has no special highlighting")
    public void invoice_row_no_highlighting() {
        Locator row = page.locator("[data-testid='invoice-row'][data-has-penalty='false']").first();
        assertThat(row.getAttribute("class")).doesNotContain("border-red");
        assertThat(row.getAttribute("class")).doesNotContain("bg-amber");
    }

    @Given("an invoice in OVERDUE status")
    public void invoice_in_overdue_status() {
        // precondition — seeded via billing-service API with status = OVERDUE
    }

    @Then("that invoice row has an amber background")
    public void invoice_row_amber_background() {
        Locator row = page.locator("[data-testid='invoice-row'][data-status='OVERDUE']").first();
        assertThat(row.getAttribute("class")).contains("bg-amber");
    }

    @Given("I have {int} PENDING and {int} PAID invoices")
    public void i_have_pending_and_paid_invoices(int pending, int paid) {
        // precondition — seeded via billing-service API
    }

    @Then("only the {int} PENDING invoice is shown")
    public void only_pending_shown(int count) {
        assertThat(page.locator("[data-testid='invoice-row']").count()).isEqualTo(count);
        page.locator("[data-testid='invoice-row']").all().forEach(row ->
                assertThat(row.locator("[data-testid='status-badge']").textContent()).containsIgnoringCase("Pending"));
    }

    @Given("an invoice with base cost BRL {int} and SLA penalty BRL {int}")
    public void invoice_with_costs(int baseCost, int penalty) {
        // precondition — seeded via billing-service API
    }

    @When("I click the invoice and the detail page loads")
    public void i_click_invoice_and_detail_loads() {
        page.locator("[data-testid='invoice-row']").first().click();
        page.waitForSelector("[data-testid='invoice-line-items']");
    }

    @Then("I see a {string} line item of BRL {double}")
    public void i_see_line_item(String label, double amount) {
        Locator item = page.locator("[data-testid='invoice-line-item']:has-text('" + label + "')");
        assertThat(item.isVisible()).isTrue();
        assertThat(item.locator("[data-testid='line-item-amount']").textContent())
                .contains(String.format("%.2f", amount));
    }

    @Then("I see a {string} of BRL {double}")
    public void i_see_total(String label, double amount) {
        assertThat(page.locator("[data-testid='invoice-total']").textContent())
                .contains(String.format("%.2f", amount));
    }

    @Given("a PRIORITY shipment delivered {int} hours late")
    public void priority_shipment_late(int hours) {
        // precondition — seeded via billing-service API
    }

    @When("I view the invoice detail")
    public void i_view_invoice_detail() {
        i_click_invoice_and_detail_loads();
    }

    @Then("I see the promised delivery date")
    public void i_see_promised_date() {
        assertThat(page.locator("[data-testid='promised-delivery-date']").isVisible()).isTrue();
    }

    @Then("I see the actual delivery date")
    public void i_see_actual_date() {
        assertThat(page.locator("[data-testid='actual-delivery-date']").isVisible()).isTrue();
    }

    @Then("I see {string}")
    public void i_see_text(String text) {
        assertThat(page.locator("text=" + text).first().isVisible()).isTrue();
    }

    @Given("I have no invoices yet")
    public void no_invoices_yet() {
        // precondition — empty account
    }

    @Then("I see {string}")
    public void i_see_empty_state_message(String message) {
        assertThat(page.locator("text=" + message).isVisible()).isTrue();
    }
}
