@Login
Feature: WDIO Demo App Login
  Cross-platform login test for WDIO Demo App (Android & iOS)

  Background:
    Given I navigate to the Login screen

  @Smoke
  Scenario: Successful login with valid credentials
    When I enter username "test@example.com"
    And I enter password "Password123"
    And I tap the Login button
    Then I should see the success message "You are logged in!"

  Scenario: Login with empty credentials
    When I tap the Login button
    Then I should see validation error for empty fields

  Scenario: Login with invalid credentials
    When I enter username "invalid@test.com"
    And I enter password "wrongpass"
    And I tap the Login button
    Then I should see an error message
