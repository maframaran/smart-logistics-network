Feature: Receive Inventory
  As a Warehouse Operator
  I want to record inbound inventory
  So that the platform tracks stock levels and enforces capacity limits

  Background:
    Given the Warehouse Operator is authenticated

  Scenario: Successful inventory receipt — within capacity
    Given a warehouse with 1000 unit capacity and 600 units currently stored
    And an inbound delivery of 300 units of SKU "SKU-001"
    When the Warehouse Operator records the inbound delivery
    Then the inventory is accepted
    And warehouse current units becomes 900
    And an InventoryReceived event is published
    And a WarehouseCapacityUpdated event is published

  Scenario: Rejection — inbound exceeds warehouse capacity
    Given a warehouse with 1000 unit capacity and 900 units currently stored
    And an inbound delivery of 200 units
    When the Warehouse Operator records the inbound delivery
    Then the delivery is rejected with error code WAREHOUSE_CAPACITY_EXCEEDED
    And a list of alternative warehouses with available capacity is returned

  Scenario: Rejection — exactly at capacity
    Given a warehouse with 1000 unit capacity and 1000 units currently stored
    And an inbound delivery of 1 unit
    When the Warehouse Operator records the inbound delivery
    Then the delivery is rejected with error code WAREHOUSE_CAPACITY_EXCEEDED

  Scenario: Rejection — expired inventory
    Given an inbound delivery with expiration date 3 days ago
    When the Warehouse Operator records the inbound delivery
    Then the delivery is rejected with error code INVALID_EXPIRATION_DATE

  Scenario: Acceptance of new unrecognized SKU
    Given an inbound delivery with a new SKU "SKU-NEW-999" not previously registered
    And the warehouse has sufficient capacity
    When the Warehouse Operator records the inbound delivery
    Then the inventory is accepted
    And a new SKU record is created automatically
