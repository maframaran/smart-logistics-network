Feature: Generate Invoice
  As the billing service
  I want to automatically generate an invoice after shipment delivery
  So that the shipper is billed correctly and promptly

  Scenario: Invoice generated for on-time delivery — no penalty
    Given a ShipmentDelivered event for a shipment delivered on time
    And route cost data is available (distance 500km, fuel €40, tolls €15)
    When the billing service processes the delivery event
    Then an invoice is generated within 1 minute
    And the invoice contains a base transportation cost line item
    And the invoice does not contain a penalty line item
    And an InvoiceGenerated event is published

  Scenario: Invoice generated for late delivery — penalty applied
    Given a ShipmentDelivered event for a PRIORITY shipment delivered 2 hours late
    And a base transportation cost of €200.00
    When the billing service processes the delivery event
    Then the invoice contains a base cost line item of €200.00
    And the invoice contains an SLA penalty line item of €60.00
    And the invoice total is €260.00
    And a SlaPenaltyApplied event is published

  Scenario: Invoice notifies shipper
    Given a shipment delivered and invoiced
    When the InvoiceGenerated event is published
    Then the notification service sends the shipper an invoice notification

  Scenario: Invoice generation retried on failure
    Given a ShipmentDelivered event
    And the invoice generation fails on the first attempt
    When the billing service retries
    Then the invoice is generated successfully on retry
    And no duplicate invoice is created (idempotency)
