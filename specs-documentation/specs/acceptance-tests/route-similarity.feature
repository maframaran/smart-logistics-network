@rag
Feature: Route Similarity Search

  Background:
    Given the rag-service is running
    And the following routes have been indexed:
      | originCity | destinationCity | vehicleType | slaType  | distanceKm | fuelCostBrl | tollsCostBrl |
      | SaoPaulo   | Curitiba        | TRUCK       | STANDARD | 408        | 190.50      | 35.00        |
      | SaoPaulo   | Curitiba        | TRUCK       | PRIORITY | 408        | 190.50      | 35.00        |
      | SaoPaulo   | Curitiba        | VAN         | STANDARD | 408        | 145.00      | 35.00        |

  Scenario: Similar routes returned for known corridor
    When I call GET /api/v1/rag/routes/similar with originCity "SaoPaulo" destinationCity "Curitiba" vehicleType "TRUCK" slaType "STANDARD"
    Then the response status is 200
    And the response contains at least 1 comparable
    And estimatedCostBrl is greater than 0
    And estimatedDurationMinutes is greater than 0

  Scenario: No routes indexed
    Given no routes have been indexed
    When I call GET /api/v1/rag/routes/similar with originCity "Manaus" destinationCity "Belem" vehicleType "TRUCK" slaType "EXPRESS"
    Then the response status is 200
    And comparables list is empty
    And lowConfidence is true

  Scenario: Route estimate card renders in UI
    Given the Shipper is authenticated
    And a shipment with an indexed route exists
    When the Shipper opens the shipment detail page
    Then a "Route Estimate" card is visible with cost and duration
