@rag
Feature: Inventory Rebalancing Advisor

  Background:
    Given the rag-service is running
    And warehouse "SP-Central" is at 88% capacity with 5 SKUs indexed
    And warehouse "SP-South" is at 40% capacity with space available

  Scenario: Recommendations returned for overcrowded warehouse
    When I call GET /api/v1/rag/warehouses/{spCentralId}/rebalance
    Then the response status is 200
    And at least 1 recommendation is returned
    And each recommendation includes sku, suggestedQtyToMove, targetWarehouseId, and reasoning
    And targetWarehouse fillPctAfter is below 70

  Scenario: No recommendations when warehouse not overcrowded
    Given warehouse "SP-Central" is at 50% capacity
    When I call GET /api/v1/rag/warehouses/{spCentralId}/rebalance
    Then recommendations list is empty
    And reason is "rebalancing not needed"

  Scenario: Escalation when all warehouses near capacity
    Given all warehouses are above 85% capacity
    When I call GET /api/v1/rag/warehouses/{spCentralId}/rebalance
    Then the response contains recommendation to escalate to operations

  Scenario: Rebalancing page accessible to Carrier
    Given the Carrier is authenticated
    When they navigate to /warehouse/{id}/rebalance
    Then a list of dispatch recommendations is displayed
