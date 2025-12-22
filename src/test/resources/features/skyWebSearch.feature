@Baskar
Feature: Sky Web Search Functionality
  As a user of sky.com
  I want to be able to search for content
  So that I can find relevant information

  Background:
    Given I am on the home page

  @WebSearch @Scenario4
  Scenario: User performs web search
    When I search 'sky' in the search bar
    Then I should see an editorial section

  @WebNavigation
  Scenario: User navigates to Deals page
    When I navigate to 'Deals'
    Then the user should be on the below page /deals
