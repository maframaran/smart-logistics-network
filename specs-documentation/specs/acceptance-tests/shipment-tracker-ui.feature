@ui
Feature: Shipment Tracker UI
  As a Shipper
  I want to view and filter my shipments in the web portal
  So that I can track the status of every order without calling APIs

  Background:
    Given I am authenticated as a Shipper with shipments in various statuses

  Scenario: Shipment list renders with all shipments
    When I navigate to /shipments
    Then I see a list of all my shipments
    And each row shows: shipment ID, origin, destination, SLA badge, status badge, required delivery date

  Scenario: Filter by status shows only matching shipments
    Given I have 3 shipments IN_TRANSIT and 2 shipments DELIVERED
    When I click the "In Transit" filter tab
    Then only the 3 IN_TRANSIT shipments are shown

  Scenario: List refreshes automatically without page reload
    Given I am on the /shipments page
    And a shipment status changes to DELIVERED on the backend
    When 15 seconds pass
    Then the shipment row updates its status badge to DELIVERED without a full page reload

  Scenario: Shipment detail shows status timeline
    Given I click on a shipment that has progressed through CREATED → SCHEDULED → ASSIGNED
    When the detail page loads
    Then I see a timeline with three steps: CREATED, SCHEDULED, ASSIGNED
    And each step shows its timestamp

  Scenario: On-time delivery shows green badge
    Given a shipment was delivered before its required delivery date
    When I view the shipment detail
    Then I see a green "On Time" badge

  Scenario: Late delivery shows red badge
    Given a shipment was delivered after its required delivery date
    When I view the shipment detail
    Then I see a red "Late" badge

  Scenario: ASSIGNED shipment shows vehicle and driver
    Given a shipment in ASSIGNED status
    When I view the shipment detail
    Then I see the assigned vehicle plate number
    And I see the assigned driver name

  Scenario: Shipment not found returns a friendly error
    When I navigate to /shipments/non-existent-id
    Then I see a "Shipment not found" message
    And a "Back to list" button
