@Baskar
Feature: API Demos Native App Testing
  As a mobile tester
  I want to verify the API Demos app navigation
  So that I can ensure the app works correctly

  @Smoke
  Scenario: Navigate to Views section
    Given I launch the API Demos app
    When I click on "Views"
    Then I should see "Animation" displayed

  @Regression
  Scenario: Navigate to App section
    Given I launch the API Demos app
    When I click on "App"
    Then I should see "Activity" displayed
