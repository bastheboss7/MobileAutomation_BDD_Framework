Feature: Logout

  Background:
#    Given I open Scan&Go

  @regression @singleProfile
  Scenario: User is able to log out
    When I login with email and password
#    And I skip tutorial
#    And I pick store "5542"
#    And I open menu
#    And I click logout
#    Then "Are you sure you want to Log out?" is displayed
#    When I click yes
#    Then sign in button is displayed
