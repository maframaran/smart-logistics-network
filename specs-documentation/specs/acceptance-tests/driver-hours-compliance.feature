Feature: Driver Hours Compliance
  As the platform
  I want to enforce legal driving hour limits
  So that drivers do not exceed 9 hours of driving per day

  Scenario: Assignment allowed — within daily limit
    Given a driver who has driven 6 hours today
    And an estimated trip duration of 2 hours
    When the assignment use case checks driver eligibility
    Then the driver is eligible for assignment

  Scenario: Assignment rejected — would exceed daily limit
    Given a driver who has driven 8 hours today
    And an estimated trip duration of 2 hours
    When the assignment use case checks driver eligibility
    Then the driver is ineligible
    And the reason DRIVER_HOURS_EXCEEDED is returned

  Scenario: Assignment rejected — already at daily limit
    Given a driver who has driven exactly 9 hours today
    And any trip duration
    When the assignment use case checks driver eligibility
    Then the driver is ineligible
    And the reason DRIVER_HOURS_EXCEEDED is returned

  Scenario: Driver transitions to RESTING automatically on limit reached
    Given a driver currently DRIVING
    When the driver logs a session that brings their daily total to 9 hours
    Then the driver status transitions to RESTING
    And a DriverHoursExceeded event is published

  Scenario: Daily reset restores eligibility at midnight
    Given a driver who reached the 9-hour limit today
    And it is now midnight (next calendar day)
    When the daily reset job runs
    Then the driver's dailyHoursToday is reset to 0
    And the driver is eligible for assignment again
