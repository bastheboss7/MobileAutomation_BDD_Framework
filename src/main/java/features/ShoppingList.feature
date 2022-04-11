Feature: ShoppingList

  Background:
    Given I login with email and password
    And Skip tutorials

#  @smoke
  Scenario: User is able to create/delete shopping list

    When I create a new shopping list
    And Add item into newly created shopping list
    Then Delete the shopping list
    And Logout from the application
