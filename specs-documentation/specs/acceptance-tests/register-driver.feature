Feature: Register Driver
  As a Carrier
  I want to register my drivers on the platform
  So that they can be assigned to shipments

  Background:
    Given the Carrier is authenticated

  Scenario: Successful driver registration
    Given a driver with license type CE and no certifications
    When the Carrier registers the driver
    Then the driver is created with status AVAILABLE
    And a unique driverId is returned
    And a DriverRegistered event is published

  Scenario: Successful registration with HAZMAT certification
    Given a driver with license type CE and HAZMAT certification
    When the Carrier registers the driver
    Then the driver is created with HAZMAT in their certifications
    And the driver is eligible for hazmat shipment assignment

  Scenario: Rejection — duplicate license number
    Given a driver with license number "DE-DRV-123456" is already registered
    When the Carrier registers another driver with license number "DE-DRV-123456"
    Then the request is rejected with error code DRIVER_ALREADY_EXISTS

  Scenario: Rejection — invalid license type
    Given a driver with license type "X" (invalid)
    When the Carrier registers the driver
    Then the request is rejected with error code INVALID_LICENSE_TYPE
