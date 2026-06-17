Feature: SLA Penalty Calculation
  As the billing system
  I want to calculate financial penalties for late deliveries
  So that shippers are compensated correctly per their SLA agreement

  Scenario: On-time delivery — no penalty
    Given a shipment with SLA type STANDARD and promised delivery in 72 hours
    And the shipment is delivered exactly on time
    When the SLA penalty is calculated
    Then the penalty amount is €0.00

  Scenario: Early delivery — no penalty
    Given a shipment with SLA type PRIORITY and promised delivery in 24 hours
    And the shipment is delivered 2 hours early
    When the SLA penalty is calculated
    Then the penalty amount is €0.00

  Scenario: STANDARD SLA late delivery — 5% per hour
    Given a shipment with SLA type STANDARD
    And a base transportation cost of €200.00
    And the shipment is delivered 3 hours late
    When the SLA penalty is calculated
    Then the penalty amount is €30.00
    # 200 × 0.05 × 3 = 30

  Scenario: PRIORITY SLA late delivery — 15% per hour
    Given a shipment with SLA type PRIORITY
    And a base transportation cost of €200.00
    And the shipment is delivered 2 hours late
    When the SLA penalty is calculated
    Then the penalty amount is €60.00
    # 200 × 0.15 × 2 = 60

  Scenario: EXPRESS SLA late delivery — 25% per hour
    Given a shipment with SLA type EXPRESS
    And a base transportation cost of €200.00
    And the shipment is delivered 1 hour late
    When the SLA penalty is calculated
    Then the penalty amount is €50.00
    # 200 × 0.25 × 1 = 50

  Scenario: Penalty capped at 100% of base cost
    Given a shipment with SLA type EXPRESS
    And a base transportation cost of €100.00
    And the shipment is delivered 10 hours late
    When the SLA penalty is calculated
    Then the penalty amount is €100.00
    # Uncapped: 100 × 0.25 × 10 = 250 → capped at 100

  Scenario: Penalty triggers SlaPenaltyApplied event
    Given a shipment delivered 1 hour late
    And the calculated penalty is €50.00
    When the invoice is generated
    Then a SlaPenaltyApplied event is published
