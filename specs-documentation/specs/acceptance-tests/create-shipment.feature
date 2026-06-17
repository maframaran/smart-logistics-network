Feature: Create Shipment
  As a Shipper
  I want to create a shipment request
  So that the platform can arrange transportation for my goods

  Background:
    Given the Shipper is authenticated
    And the platform geocoding service is available

  Scenario: Successful shipment creation
    Given a cargo weighing 800kg with volume 2.5m³
    And origin address "Warehouse A, Berlin, Germany"
    And destination address "Distribution Center, Munich, Germany"
    And required delivery date is 3 days from now
    And SLA type is STANDARD
    When the Shipper submits the shipment request
    Then the shipment is created with status CREATED
    And a unique shipmentId is returned
    And a ShipmentCreated event is published to the "shipment.created" topic

  Scenario: Shipment creation with cold chain requirement
    Given a cargo weighing 200kg with volume 1.0m³
    And the cargo requires cold chain handling
    And origin address "Farm, Hamburg, Germany"
    And destination address "Supermarket, Frankfurt, Germany"
    And required delivery date is 2 days from now
    And SLA type is PRIORITY
    When the Shipper submits the shipment request
    Then the shipment is created with status CREATED
    And the cargo spec has requiresColdChain = true

  Scenario: Shipment creation with hazmat requirement
    Given a cargo weighing 500kg with volume 3.0m³
    And the cargo requires hazmat handling
    When the Shipper submits the shipment request
    Then the shipment is created with status CREATED
    And the cargo spec has requiresHazmat = true

  Scenario: Rejection — required delivery date in the past
    Given a cargo weighing 800kg with volume 2.5m³
    And required delivery date is 2 days ago
    When the Shipper submits the shipment request
    Then the request is rejected with error code INVALID_DELIVERY_DATE

  Scenario: Rejection — negative cargo weight
    Given a cargo weighing -100kg
    When the Shipper submits the shipment request
    Then the request is rejected with error code INVALID_CARGO_SPEC

  Scenario: Rejection — geocoding fails for origin address
    Given an invalid origin address "XXXXXXXXXXX"
    When the Shipper submits the shipment request
    Then the request is rejected with a field-level geocoding error for origin
