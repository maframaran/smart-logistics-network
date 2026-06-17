@ui
Feature: Billing Dashboard UI
  As a Shipper
  I want to view my invoices and SLA penalty details
  So that I can track costs and identify unexpected charges

  Background:
    Given I am authenticated as a Shipper

  Scenario: Invoice list shows all invoices for the Shipper
    Given I have 4 invoices (2 PAID, 1 PENDING, 1 OVERDUE)
    When I navigate to /billing
    Then I see all 4 invoices in the table

  Scenario: Invoice with SLA penalty has red left border
    Given an invoice with an SLA penalty of BRL 150
    When I view the invoice list
    Then that invoice row has a red left border

  Scenario: Invoice without penalty has no highlight
    Given an invoice with SLA penalty of BRL 0
    When I view the invoice list
    Then that invoice row has no special highlighting

  Scenario: OVERDUE invoice has amber background
    Given an invoice in OVERDUE status
    When I view the invoice list
    Then that invoice row has an amber background

  Scenario: Filter by Pending shows only pending invoices
    Given I have 1 PENDING and 3 PAID invoices
    When I click the "Pending" filter tab
    Then only the 1 PENDING invoice is shown

  Scenario: Invoice detail shows itemised line items
    Given an invoice with base cost BRL 200 and SLA penalty BRL 60
    When I click the invoice and the detail page loads
    Then I see a "Base Transportation Cost" line item of BRL 200.00
    And I see an "SLA Penalty" line item of BRL 60.00
    And I see a "Total" of BRL 260.00

  Scenario: SLA penalty section shows breakdown
    Given a PRIORITY shipment delivered 2 hours late
    When I view the invoice detail
    Then I see the promised delivery date
    And I see the actual delivery date
    And I see "2 hours late"
    And I see the PRIORITY penalty rate (BRL 150/day)

  Scenario: No invoices shows empty state
    Given I have no invoices yet
    When I navigate to /billing
    Then I see "Your invoices will appear here after your first delivery"
