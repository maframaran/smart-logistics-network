@rag
Feature: Demand Forecast Context

  Background:
    Given the rag-service is running
    And 12 months of shipment history for shipper "acme-corp" on São Paulo → Rio have been indexed

  Scenario: Forecast returned for known shipper-route
    When I call GET /api/v1/rag/forecast with shipperId "acme-corp" originCity "SaoPaulo" destinationCity "RioDeJaneiro" targetMonth "2026-08"
    Then the response status is 200
    And expectedShipments is greater than 0
    And confidenceInterval.low is less than expectedShipments
    And confidenceInterval.high is greater than expectedShipments
    And at least 3 comparables are listed

  Scenario: Low confidence for new shipper
    Given no history exists for shipper "new-shipper"
    When I call GET /api/v1/rag/forecast with shipperId "new-shipper"
    Then expectedShipments is 0
    And lowConfidence is true
    And comparables list is empty

  Scenario: Forecast panel visible on Carrier dashboard
    Given the Carrier is authenticated
    When the Carrier views the dashboard
    Then a "Demand Forecast" panel is visible
    And it shows a forecast range for the next 30 days
