@Login @androidOnly
Feature: WDIO Demo App Login
  Cross-platform login test for WDIO Demo App (Android & iOS)

  Background:
    Given I navigate to the Login screen

  @Smoke
  Scenario Outline: Login with <scenario_type> credentials
    When I enter username "<username>"
    And I enter password "<password>"
    And I tap the Login button
    Then I should see <expected_result>

    Examples: Valid Credentials
      | scenario_type | username           | password    | expected_result                         |
      | valid         | test@example.com   | Password123 | the success message "You are logged in!" |

  