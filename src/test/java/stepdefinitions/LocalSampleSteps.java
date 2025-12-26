package stepdefinitions;

import com.automation.framework.pages.PageObjectManager;
import com.automation.framework.pages.screens.LocalSampleScreen;
import com.automation.framework.reports.ExtentReportManager;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Step definitions for Local Sample App (Basic Networking) feature.
 *
 * @author Baskar
 * @version 1.0.0
 */
public class LocalSampleSteps {

    private LocalSampleScreen localSampleScreen() {
        return PageObjectManager.getInstance().getLocalSampleScreen();
    }

    @Given("I open the Basic Networking application")
    public void iOpenTheBasicNetworkingApplication() {
        // App is launched by Hooks. @Given is primarily for logging/report clarity in
        // this context
        // unless we need to navigate specifically within the app.
        reportStep("Opened Basic Networking application", "PASS");
    }

    @When("I tap on the \"Test Action\" button")
    public void iTapOnTheTestActionButton() {
        localSampleScreen().tapTestAction();
        reportStep("Tapped on Test Action button", "PASS");
    }

    @Then("I should see the connection status {string}")
    public void iShouldSeeTheConnectionStatus(String expectedStatus) {
        String actualStatus = localSampleScreen().getConnectionStatus();

        if (actualStatus != null) {
            if (actualStatus.contains(expectedStatus)) {
                reportStep("Connection status verified: " + actualStatus, "PASS");
            } else {
                reportStep("Expected status '" + expectedStatus + "' but found '" + actualStatus + "'", "FAIL");
            }
        } else {
            reportStep("Connection status message not found", "FAIL");
        }
    }

    /**
     * Report a test step result (logs to Extent Report).
     */
    private void reportStep(String desc, String status) {
        switch (status.toUpperCase()) {
            case "PASS" -> ExtentReportManager.logPass(desc);
            case "FAIL" -> {
                ExtentReportManager.logFail(desc);
                // Use TestNG Assert to fail the cucumber step physically
                Assert.fail("FAILED: " + desc);
            }
            case "INFO" -> ExtentReportManager.logInfo(desc);
            default -> ExtentReportManager.logInfo(desc);
        }
    }
}
