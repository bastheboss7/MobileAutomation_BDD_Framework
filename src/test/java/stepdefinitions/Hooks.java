package stepdefinitions;

import com.automation.framework.core.ConfigManager;
import com.automation.framework.core.DevicePool;
import com.automation.framework.core.DriverFactory;
import com.automation.framework.core.DriverManager;
import com.automation.framework.pages.PageObjectManager;
import com.automation.framework.reports.ExtentReportManager;
import com.aventstack.extentreports.Status;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cucumber hooks for test lifecycle management.
 * Integrates with Extent Reports for detailed reporting.
 * 
 * @author Baskar
 * @version 4.1.0
 */
public class Hooks {
    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);

    @Before
    public void launchApplication(Scenario scenario) {
        logger.info("Starting scenario: {}", scenario.getName());

        // Create test in Extent Report
        ExtentReportManager.createTest(scenario.getName());
        ExtentReportManager.logInfo("Scenario started: " + scenario.getName());

        // Log tags if present
        if (!scenario.getSourceTagNames().isEmpty()) {
            ExtentReportManager.logInfo("Tags: " + scenario.getSourceTagNames());
        }

        // Create driver using DriverFactory (handles platform detection)
        DriverFactory.createDriver();
        ExtentReportManager.logInfo("Application launched successfully");

        logger.info("Application launched successfully");
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        // Check if step screenshots are enabled (configurable to reduce report bloat)
        boolean captureStepScreenshots = ConfigManager.getBoolean("screenshot.on.step", false);
        logger.debug("Step screenshot config: {}, hasDriver: {}", captureStepScreenshots, DriverManager.hasDriver());

        if (captureStepScreenshots && DriverManager.hasDriver()) {
            try {
                byte[] screenshot = DriverManager.getDriver().getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Step Screenshot");
                ExtentReportManager.attachScreenshot(screenshot, "Step Screenshot");
                logger.info("📸 Step screenshot attached");
            } catch (Exception e) {
                logger.warn("Could not capture step screenshot: {}", e.getMessage());
            }
        }
    }

    @After
    public void executeAfterScenario(Scenario scenario) {
        try {
            if (scenario.isFailed() && DriverManager.hasDriver()) {
                byte[] screenshot = DriverManager.getDriver().getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", scenario.getName() + "_failure");
                ExtentReportManager.attachScreenshotOnFailure(screenshot, scenario.getName());
                ExtentReportManager.logFail("Scenario FAILED: " + scenario.getName());
            } else if (scenario.getStatus().toString().equals("PASSED")) {
                ExtentReportManager.logPass("Scenario PASSED: " + scenario.getName());
            } else {
                ExtentReportManager.logStep(Status.SKIP, "Scenario status: " + scenario.getStatus());
            }
        } catch (Exception e) {
            logger.warn("Failed to capture screenshot: {}", e.getMessage());
        } finally {
            // ALWAYS clean up resources to prevent leaks
            PageObjectManager.reset(); // Reset page objects for next scenario
            DriverManager.quitDriver(); // Quit driver
            DevicePool.releaseDevice(); // Release device from pool
            logger.info("Scenario completed: {} - {}", scenario.getName(), scenario.getStatus());
        }
    }
}
