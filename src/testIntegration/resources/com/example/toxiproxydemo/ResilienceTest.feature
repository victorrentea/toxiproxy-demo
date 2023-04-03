Feature: Resilience test

  @ResilienceTest
  Scenario: Validate resilience of database
    Given the application is healthy
    When connection to db fails
    Then the application becomes unhealthy

    When the db connection is fixed
    Then the application is healthy

