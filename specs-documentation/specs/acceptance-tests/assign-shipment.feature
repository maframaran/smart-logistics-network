Feature: Assign Shipment
  As the platform
  I want to automatically assign an eligible vehicle and driver to a scheduled shipment
  So that the shipment can be picked up and delivered within the promised SLA

  Background:
    Given a shipment in SCHEDULED status with required delivery date 48 hours from now
    And SLA type is STANDARD

  # Scenarios from docs/overview.md example
  Scenario: Successful assignment
    Given a shipment weighing 800kg
    And a vehicle with 1000kg capacity that is AVAILABLE
    And an available driver with no certifications required
    And a route ETA of 24 hours from now
    When assignment is requested
    Then the shipment is assigned to the vehicle and driver
    And the shipment status becomes ASSIGNED
    And a ShipmentAssigned event is published to the "shipment.assigned" topic

  Scenario: Capacity exceeded — weight
    Given a shipment weighing 1500kg
    And a vehicle with 1000kg capacity that is AVAILABLE
    When assignment is requested
    Then the assignment fails
    And the reason CAPACITY_WEIGHT_EXCEEDED is returned

  Scenario: Capacity exceeded — volume
    Given a shipment with volume 10m³
    And a vehicle with maximum volume 5m³ that is AVAILABLE
    When assignment is requested
    Then the assignment fails
    And the reason CAPACITY_VOLUME_EXCEEDED is returned

  Scenario: Hazmat — driver not certified
    Given a shipment requiring hazmat handling
    And a vehicle that is AVAILABLE and hazmat capable
    And an available driver without HAZMAT certification
    When assignment is requested
    Then the assignment fails
    And the reason HAZMAT_CERTIFICATION_REQUIRED is returned

  Scenario: Cold chain — vehicle not refrigerated
    Given a shipment requiring cold chain handling
    And a standard (non-refrigerated) vehicle that is AVAILABLE
    And an available driver
    When assignment is requested
    Then the assignment fails
    And the reason COLD_CHAIN_REQUIRED is returned

  Scenario: SLA infeasible — route ETA exceeds promised delivery date
    Given a shipment with required delivery date 2 hours from now
    And SLA type is STANDARD
    And the best available route has an ETA of 10 hours from now
    When assignment is requested
    Then the assignment fails
    And the reason SLA_INFEASIBLE is returned

  Scenario: No vehicles available
    Given all vehicles are ASSIGNED or MAINTENANCE
    When assignment is requested
    Then the assignment fails
    And the reason NO_VEHICLES_AVAILABLE is returned

  Scenario: Driver hours exceeded
    Given a driver who has already driven 8 hours today
    And the estimated trip is 2 hours
    And a vehicle that is AVAILABLE
    When assignment is requested
    Then the assignment fails
    And the reason DRIVER_HOURS_EXCEEDED is returned

  Scenario: Manual override by Platform Administrator
    Given a shipment that failed automated assignment
    And the Platform Administrator selects a specific vehicle and driver
    When the administrator submits a manual override assignment
    Then the shipment is assigned to the selected vehicle and driver
    And an audit log entry is created recording the administrator's action
