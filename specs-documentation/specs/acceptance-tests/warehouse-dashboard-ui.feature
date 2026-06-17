@ui
Feature: Warehouse Capacity Dashboard UI
  As a Carrier
  I want to monitor warehouse fill levels and inventory
  So that I can prevent capacity violations (BR-006) and manage stock

  Background:
    Given I am authenticated as a Carrier

  Scenario: Warehouse list shows a card per warehouse
    Given I have 2 warehouses registered
    When I navigate to /warehouse
    Then I see 2 warehouse cards
    And each card shows the warehouse name, city, and capacity gauges

  Scenario: Capacity gauge is green when fill is under 70%
    Given a warehouse with 500 / 1000 units stored (50% fill)
    When I view the warehouse list
    Then the capacity gauge is green

  Scenario: Capacity gauge turns amber at 70–89% fill
    Given a warehouse with 750 / 1000 units stored (75% fill)
    When I view the warehouse list
    Then the capacity gauge is amber

  Scenario: Capacity gauge turns red at 90%+ fill
    Given a warehouse with 950 / 1000 units stored (95% fill)
    When I view the warehouse list
    Then the capacity gauge is red

  Scenario: Warehouse detail shows inventory table
    Given a warehouse with 3 SKUs in inventory
    When I click the warehouse card and the detail page loads
    Then I see a table with 3 rows
    And each row shows: SKU, quantity, weight/unit, volume/unit, expiration date

  Scenario: SKUs expiring within 7 days are highlighted amber
    Given a warehouse with a SKU expiring in 5 days
    When I view the warehouse detail
    Then that SKU row is highlighted amber

  Scenario: Gauges refresh after inventory receipt
    Given I am on the warehouse detail page
    And a new inventory receipt increases fill from 50% to 60%
    When 30 seconds pass
    Then the capacity gauge updates to 60% without a full page reload

  Scenario: No warehouses registered shows empty state
    Given I have no warehouses registered
    When I navigate to /warehouse
    Then I see an empty state message
