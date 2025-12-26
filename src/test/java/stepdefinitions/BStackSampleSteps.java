package stepdefinitions;

import com.automation.framework.pages.PageObjectManager;
import com.automation.framework.pages.screens.BStackSampleScreen;
import com.automation.framework.reports.ExtentReportManager;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BStackSampleSteps {
    private static final Logger logger = LoggerFactory.getLogger(BStackSampleSteps.class);

    private BStackSampleScreen sampleScreen() {
        return PageObjectManager.getInstance().getBStackSampleScreen();
    }

    private void reportStep(String stepName, String status) {
        if (status.equalsIgnoreCase("PASS")) {
            ExtentReportManager.getTest().pass(stepName);
        } else {
            ExtentReportManager.getTest().fail(stepName);
        }
    }

    @Given("I open the BStack Sample application")
    public void iOpenTheBStackSampleApplication() {
        // App is launched by the @Before hook
        // Alert button is on the home screen, no navigation needed
        reportStep("Opened BStack Sample application", "PASS");
    }

    @When("I click on the Alert button")
    public void iClickOnTheAlertButton() {
        sampleScreen().clickAlertButton();
        reportStep("Clicked Alert button", "PASS");
    }

    @Then("I should see an alert message")
    public void iShouldSeeAnAlertMessage() {
        String alertText = sampleScreen().getAlertText();
        Assert.assertNotNull(alertText, "Alert was not displayed");
        logger.info("Alert displayed: {}", alertText);
        reportStep("Verified alert is displayed: " + alertText, "PASS");
    }

    @Then("I click {string} on the alert")
    public void iClickOnTheAlert(String buttonLabel) {
        sampleScreen().acceptAlert();
        reportStep("Clicked OK on the alert", "PASS");
    }
}
