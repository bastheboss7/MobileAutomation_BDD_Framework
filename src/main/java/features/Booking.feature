Feature: Booking creation

  Background:
    Given I signin with existing mail

#  @smoke
  Scenario: Perform search for specific dates

    When Enter my destination
    And Set the search dates
    And Click search
    Then I see results for my search
