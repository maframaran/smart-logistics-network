Feature: Generate Route
  As the routing service
  I want to calculate a route for a shipment
  So that the assignment use case has accurate ETA and cost data

  Background:
    Given the Maps/Routing API is available

  Scenario: Successful route generation
    Given a ShipmentCreated event for a shipment from Berlin to Munich
    And the vehicle type is TRUCK
    When the routing service processes the event
    Then a Route is calculated with total distance, ETA, fuel estimate, and toll cost
    And a RouteCalculated event is published within 5 seconds
    And the RouteCalculated event contains the shipmentId

  Scenario: Route respects vehicle weight restrictions
    Given a ShipmentCreated event for a shipment through a city center zone
    And the vehicle type is TRUCK with weight 20,000kg
    When the routing service calculates the route
    Then the route avoids roads with weight restrictions below 20,000kg

  Scenario: Route calculation fails — Maps API unreachable
    Given the Maps/Routing API is unreachable
    And a ShipmentCreated event is received
    When the routing service attempts to calculate a route
    Then the service retries 3 times with exponential backoff
    And after exhaustion a RouteCalculationFailed event is published

  Scenario: No viable route exists
    Given origin and destination that have no viable road connection
    When the routing service attempts to calculate a route
    Then a RouteCalculationFailed event is published with reason NO_ROUTE_FOUND
