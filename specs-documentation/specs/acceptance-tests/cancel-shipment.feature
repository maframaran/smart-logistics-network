Feature: Cancel Shipment
  As a Shipper or Platform Administrator
  I want to cancel a shipment
  So that resources are released and appropriate fees are applied based on status

  Scenario: Cancel shipment in DRAFT status — no fee
    Given a shipment in DRAFT status
    When the Shipper requests cancellation
    Then the shipment transitions to CANCELLED status
    And no cancellation fee is charged
    And a ShipmentCancelled event is published

  Scenario: Cancel shipment in CREATED status — no fee
    Given a shipment in CREATED status
    When the Shipper requests cancellation
    Then the shipment transitions to CANCELLED status
    And no cancellation fee is charged

  Scenario: Cancel shipment in SCHEDULED status — fee applies
    Given a shipment in SCHEDULED status
    When the Shipper requests cancellation
    Then the shipment transitions to CANCELLED status
    And a cancellation fee invoice line item is generated
    And a ShipmentCancelled event is published

  Scenario: Cancel shipment in ASSIGNED status — requires admin approval
    Given a shipment in ASSIGNED status
    When the Shipper requests cancellation
    Then a pending approval request is created
    And the Platform Administrator is notified
    And the shipment remains in ASSIGNED status until approval

  Scenario: Admin approves cancellation of ASSIGNED shipment
    Given a shipment in ASSIGNED status with a pending cancellation request
    When the Platform Administrator approves the cancellation
    Then the shipment transitions to CANCELLED status
    And the assigned vehicle transitions back to AVAILABLE
    And the assigned driver transitions back to AVAILABLE
    And a ShipmentCancelled event is published

  Scenario: Cancellation forbidden — shipment IN_TRANSIT
    Given a shipment in IN_TRANSIT status
    When the Shipper requests cancellation
    Then the request is rejected with error code CANCELLATION_FORBIDDEN

  Scenario: Cancellation forbidden — shipment DELIVERED
    Given a shipment in DELIVERED status
    When the Shipper requests cancellation
    Then the request is rejected with error code CANCELLATION_FORBIDDEN
