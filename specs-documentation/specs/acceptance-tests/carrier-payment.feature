Feature: Carrier Payment
  As the billing service
  I want to process carrier payments after shipment delivery
  So that carriers are compensated promptly and accurately

  Scenario: Successful automatic carrier payment
    Given a shipment has been delivered and invoiced
    And the carrier has a registered payment account
    When 24 hours pass after the delivery confirmation
    Then the carrier payment transitions from PENDING to APPROVED
    And the Payment Gateway is called to transfer funds
    And the carrier payment transitions to PAID
    And a CarrierPaymentApproved event is published

  Scenario: Payment amount reflects platform commission deduction
    Given a base transportation cost of €200.00
    And the platform commission rate is 10%
    When the carrier payment is calculated
    Then the carrier receives €180.00

  Scenario: Payment Gateway failure — retry and recover
    Given a carrier payment in APPROVED status
    And the Payment Gateway returns an error on the first call
    When the billing service retries up to 3 times
    And the Payment Gateway succeeds on the second attempt
    Then the carrier payment transitions to PAID

  Scenario: Payment Gateway failure — exhausted retries
    Given the Payment Gateway fails on all 3 retry attempts
    When retries are exhausted
    Then the carrier payment transitions to FAILED
    And the billing operations team is alerted

  Scenario: Payment blocked — carrier has no payment account
    Given a carrier without a registered payment account
    When a shipment is delivered
    Then the carrier payment remains in PENDING
    And the carrier is notified to add banking details
