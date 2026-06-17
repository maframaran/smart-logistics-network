@rag
Feature: Dynamic Pricing Advisor

  Background:
    Given the rag-service is running
    And 10 PAID STANDARD invoices for São Paulo → Rio de Janeiro have been indexed

  Scenario: Pricing recommendation returned with high confidence
    When I POST /api/v1/rag/pricing/recommend with originCity "SaoPaulo" destinationCity "RioDeJaneiro" weightKg 500 slaType "STANDARD" warehouseUtilizationPct 75
    Then the response status is 200
    And suggestedPriceBrl is greater than 0
    And confidencePct is at least 60
    And lowerBound is less than suggestedPriceBrl
    And upperBound is greater than suggestedPriceBrl

  Scenario: Fallback triggered on low confidence
    Given fewer than 3 paid comparables exist for the route
    When I POST /api/v1/rag/pricing/recommend
    Then confidencePct is at most 50

  Scenario: Suggested price does not exceed 1.5x static rate
    When I POST /api/v1/rag/pricing/recommend for an EXPRESS shipment
    Then suggestedPriceBrl is at most 450.00
