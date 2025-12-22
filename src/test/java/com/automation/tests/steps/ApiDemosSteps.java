package com.automation.tests.steps;

import com.automation.tests.pages.ApiDemosPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * Step Definitions for API Demos app scenarios.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class ApiDemosSteps {
    private static final Logger logger = LoggerFactory.getLogger(ApiDemosSteps.class);
    private ApiDemosPage apiDemosPage;
    
    @Given("I launch the API Demos app")
    public void iLaunchApiDemosApp() {
        apiDemosPage = new ApiDemosPage();
        logger.info("API Demos app launched");
    }
    
    @When("I click on {string}")
    public void iClickOn(String menuItem) {
        apiDemosPage.clickMenuItem(menuItem);
        logger.info("Clicked on: {}", menuItem);
    }
    
    @Then("I should see {string} displayed")
    public void iShouldSeeDisplayed(String menuItem) {
        Assert.assertTrue(apiDemosPage.isMenuItemDisplayed(menuItem),
                menuItem + " should be displayed");
        logger.info("Verified {} is displayed", menuItem);
    }
}
