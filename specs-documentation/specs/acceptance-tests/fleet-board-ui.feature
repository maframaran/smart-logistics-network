@ui
Feature: Fleet Board UI
  As a Carrier
  I want to see all my vehicles and drivers on a single board
  So that I can manage assignments and monitor driver hours without calling APIs

  Background:
    Given I am authenticated as a Carrier

  Scenario: Fleet board shows vehicles and drivers side by side
    Given I have 3 vehicles and 2 drivers registered
    When I navigate to /fleet
    Then I see 3 vehicle cards on the left
    And I see 2 driver cards on the right

  Scenario: Vehicle card shows correct status badge
    Given I have an AVAILABLE vehicle and an ASSIGNED vehicle
    When I view the fleet board
    Then the AVAILABLE vehicle card shows a green "Available" badge
    And the ASSIGNED vehicle card shows a blue "Assigned" badge

  Scenario: HAZMAT vehicle shows hazmat badge
    Given I have a vehicle of type HAZMAT_TRUCK
    When I view the fleet board
    Then the vehicle card shows a "HAZMAT" badge

  Scenario: Refrigerated vehicle shows refrigerated badge
    Given I have a vehicle of type REFRIGERATED_TRUCK
    When I view the fleet board
    Then the vehicle card shows a "Refrigerated" badge

  Scenario: Driver progress bar reflects daily hours
    Given a driver has driven 6 hours today
    When I view the fleet board
    Then the driver card shows a progress bar at 67% (6 of 9 hours)
    And the progress bar is green

  Scenario: Driver progress bar turns red near limit
    Given a driver has driven 8.5 hours today
    When I view the fleet board
    Then the driver's progress bar is red
    And shows "8.5 / 9h"

  Scenario: Driver at 9h limit shows RESTING status
    Given a driver has driven exactly 9 hours today
    When I view the fleet board
    Then the driver shows status "RESTING"
    And the progress bar is full red

  Scenario: Filter vehicles by status
    Given I have 2 AVAILABLE and 1 MAINTENANCE vehicle
    When I select the "Available" filter on the vehicle column
    Then only the 2 AVAILABLE vehicles are shown

  Scenario: No vehicles registered shows empty state
    Given I have no vehicles registered
    When I navigate to /fleet
    Then I see "Register your first vehicle" call-to-action
