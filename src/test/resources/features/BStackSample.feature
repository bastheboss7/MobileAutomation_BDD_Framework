Feature: BrowserStack Sample App Validation

  @BStackSample @iosOnly
Scenario: Verify Alert Functionality
  Given I open the BStack Sample application
  When I click on the Alert button
  Then I should see an alert message
  And I click "OK" on the alert
