Feature: Schedule Shipment
  As a Shipper
  I want to schedule a pickup window for my shipment
  So that warehouse and carrier operations can be coordinated

  Background:
    Given a shipment in CREATED status

  Scenario: Successful scheduling
    Given a pickup window starting 6 hours from now
    And the origin warehouse is open during that window
    When the Shipper schedules the shipment
    Then the shipment transitions to SCHEDULED status
    And a ShipmentScheduled event is published

  Scenario: Rejection — pickup window too soon
    Given a pickup window starting 2 hours from now
    When the Shipper schedules the shipment
    Then the request is rejected with error code PICKUP_WINDOW_TOO_SOON

  Scenario: Rejection — warehouse closed during pickup window
    Given a pickup window on Sunday at 3:00 AM
    And the origin warehouse is closed on Sundays
    When the Shipper schedules the shipment
    Then the request is rejected with error code WAREHOUSE_CLOSED

  Scenario: Idempotent scheduling
    Given the shipment is already in SCHEDULED status with a confirmed window
    When the Shipper submits the same scheduling request
    Then the existing schedule is returned without error

  Scenario: Rejection — shipment not in CREATED status
    Given a shipment in ASSIGNED status
    When the Shipper attempts to schedule it
    Then the request is rejected with error code INVALID_STATUS_TRANSITION
