@ui
Feature: Dashboard Overview
  As an authenticated Shipper or Carrier
  I want to see a summary of platform operations on the dashboard
  So that I have situational awareness without navigating multiple pages

  Background:
    Given I am authenticated as a Shipper

  Scenario: Dashboard loads with all four stat cards
    Given the platform has shipments, vehicles, warehouses, and invoices
    When I navigate to the dashboard
    Then I see an "Active Shipments" card
    And I see an "Available Vehicles" card
    And I see a "Warehouse Fill" card
    And I see an "Outstanding Invoices" card
    And all cards load within 2 seconds

  Scenario: Dashboard shows zero counts for a new account
    Given I am a new Shipper with no data
    When I navigate to the dashboard
    Then all stat cards show "0"
    And a "Get started" call-to-action is visible

  Scenario: Data is scoped to the authenticated actor
    Given Shipper A has 5 active shipments
    And Shipper B has 3 active shipments
    When Shipper A views the dashboard
    Then the "Active Shipments" card shows 5
    And does not include Shipper B's shipments

  Scenario: Unreachable service shows error card without breaking others
    Given the billing-service is unreachable
    When I navigate to the dashboard
    Then the "Outstanding Invoices" card shows an error state with a retry button
    And the other three cards render normally with their data

  Scenario: Each stat card links to its detail page
    When I click the "Active Shipments" card
    Then I am navigated to the /shipments page
