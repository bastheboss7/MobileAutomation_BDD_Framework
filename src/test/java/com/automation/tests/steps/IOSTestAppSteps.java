package com.automation.tests.steps;

import com.automation.tests.pages.IOSTestAppPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * Step Definitions for iOS TestApp scenarios.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class IOSTestAppSteps {
    private static final Logger logger = LoggerFactory.getLogger(IOSTestAppSteps.class);
    private IOSTestAppPage testAppPage;
    
    @Given("I launch the iOS TestApp")
    public void iLaunchTheIOSTestApp() {
        testAppPage = new IOSTestAppPage();
        logger.info("iOS TestApp launched successfully");
    }
    
    @Then("I should see the main screen")
    public void iShouldSeeTheMainScreen() {
        Assert.assertTrue(testAppPage.isMainScreenDisplayed(), 
                "Main screen should be displayed");
        logger.info("Main screen is displayed");
        
        // Log page source for debugging
        String pageSource = testAppPage.getPageSource();
        logger.debug("Page source length: {} characters", pageSource.length());
    }
}
