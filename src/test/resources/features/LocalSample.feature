Feature: Basic Networking Validation

  @LocalSample @Skip
  Scenario: Verify active internet connection
    Given I open the Basic Networking application
    When I tap on the "Test Action" button
    Then I should see the connection status "The active connection is wifi"
