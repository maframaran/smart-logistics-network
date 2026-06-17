@rag
Feature: SLA Penalty Waiver Assistant

  Background:
    Given the rag-service is running
    And 5 historical PRIORITY invoices with weather-delay waivers have been indexed

  Scenario: Waiver recommended for weather delay
    Given a PRIORITY invoice with 2 days late and penalty 300 BRL exists
    When Finance POSTs /api/v1/rag/invoices/{invoiceId}/waiver with reason "weather delay on BR-116"
    Then the response status is 200
    And recommendation is "WAIVE"
    And confidence is greater than 0.6
    And at least 2 precedents are listed in the response

  Scenario: Uphold recommended for shipper error
    Given a STANDARD invoice with 1 day late and penalty 50 BRL exists
    When Finance POSTs a waiver with reason "shipper provided wrong address"
    Then recommendation is "UPHOLD"

  Scenario: Escalate when no precedents exist
    Given no similar invoices have been indexed
    When Finance POSTs a waiver request
    Then recommendation is "ESCALATE"
    And lowConfidence is true

  Scenario: Waiver page is accessible
    Given the Carrier is authenticated as Finance
    When they navigate to /billing/{invoiceId}/waiver
    Then a reason input form is visible
    And after submission a recommendation badge is shown
