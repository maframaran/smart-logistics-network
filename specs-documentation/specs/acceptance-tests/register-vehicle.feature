Feature: Register Vehicle
  As a Carrier
  I want to register my vehicles on the platform
  So that they can be assigned to shipments

  Background:
    Given the Carrier is authenticated

  Scenario: Successful vehicle registration
    Given a truck with 5000kg weight capacity and 20m³ volume capacity
    And the truck is not refrigerated and not hazmat certified
    When the Carrier registers the vehicle
    Then the vehicle is created with status AVAILABLE
    And a unique vehicleId is returned
    And a VehicleRegistered event is published

  Scenario: Successful refrigerated truck registration
    Given a refrigerated truck with 3000kg weight capacity
    And the vehicle has refrigerated = true
    When the Carrier registers the vehicle
    Then the vehicle is created with type REFRIGERATED_TRUCK and status AVAILABLE

  Scenario: Rejection — duplicate plate number
    Given a vehicle with plate "AB-123-CD" is already registered
    When the Carrier registers another vehicle with plate "AB-123-CD"
    Then the request is rejected with error code VEHICLE_ALREADY_EXISTS

  Scenario: Rejection — invalid capacity
    Given a vehicle with weight capacity of 0kg
    When the Carrier registers the vehicle
    Then the request is rejected with error code INVALID_CAPACITY

  Scenario: Rejection — REFRIGERATED_TRUCK with refrigerated = false
    Given a vehicle of type REFRIGERATED_TRUCK
    And refrigerated = false
    When the Carrier registers the vehicle
    Then the request is rejected with error code INCONSISTENT_VEHICLE_TYPE
