package com.automation.tests.steps;

import com.automation.tests.pages.HomePage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * Step Definitions for Home Page scenarios.
 * Separated from Page Object for clean architecture.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class HomeSteps {
    private static final Logger logger = LoggerFactory.getLogger(HomeSteps.class);
    private HomePage homePage;
    
    @Given("I am on the home page")
    public void iAmOnTheHomePage() {
        homePage = new HomePage();
        homePage.handleCookieConsent();
        logger.info("User is on the home page");
    }
    
    @When("I navigate to 'Deals'")
    public void iNavigateToDeals() {
        homePage.navigateToDeals();
        logger.info("Navigated to Deals page");
    }
    
    @Then("the user should be on the below page(.*)")
    public void userShouldBeOnPage(String expectedUrl) {
        String currentUrl = homePage.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains(expectedUrl.trim()),
                "Expected URL to contain: " + expectedUrl + " but was: " + currentUrl);
        logger.info("Verified user is on page: {}", expectedUrl);
    }
    
    @When("I search {string} in the search bar")
    public void iSearchInSearchBar(String searchTerm) {
        homePage.search(searchTerm);
        logger.info("Searched for: {}", searchTerm);
    }
    
    @When("I search 'sky' in the search bar")
    public void iSearchSkyInSearchBar() {
        homePage.search("sky");
        logger.info("Searched for: sky");
    }
    
    @Then("I should see an editorial section")
    public void iShouldSeeEditorialSection() {
        Assert.assertTrue(homePage.isSearchResultsDisplayed(),
                "Search results/editorial section should be displayed");
        logger.info("Verified editorial section is displayed");
    }
}
