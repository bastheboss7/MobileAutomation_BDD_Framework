package com.automation.framework.listeners;

import com.automation.framework.core.DriverManager;
import com.automation.framework.utils.ScreenshotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TestNG Test Listener for enhanced reporting.
 * Captures screenshots on failure and logs test lifecycle events.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class TestListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger(TestListener.class);
    private static final String SCREENSHOT_DIR = "target/screenshots";
    
    @Override
    public void onStart(ITestContext context) {
        logger.info("========================================");
        logger.info("Test Suite Started: {}", context.getName());
        logger.info("========================================");
        
        // Create screenshot directory
        try {
            Files.createDirectories(Paths.get(SCREENSHOT_DIR));
        } catch (Exception e) {
            logger.warn("Could not create screenshot directory", e);
        }
    }
    
    @Override
    public void onFinish(ITestContext context) {
        logger.info("========================================");
        logger.info("Test Suite Finished: {}", context.getName());
        logger.info("Passed: {} | Failed: {} | Skipped: {}", 
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
        logger.info("========================================");
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        logger.info(">>> Starting test: {}", result.getName());
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("<<< Test PASSED: {} ({}ms)", 
                result.getName(), result.getEndMillis() - result.getStartMillis());
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("<<< Test FAILED: {}", result.getName());
        logger.error("Failure reason: {}", result.getThrowable().getMessage());
        
        // Capture screenshot on failure
        captureFailureScreenshot(result);
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("<<< Test SKIPPED: {}", result.getName());
    }
    
    /**
     * Capture screenshot on test failure.
     */
    private void captureFailureScreenshot(ITestResult result) {
        try {
            if (DriverManager.getDriver() != null) {
                String fileName = ScreenshotUtils.captureOnFailure(result.getName());
                if (fileName != null) {
                    logger.info("Failure screenshot saved: {}", fileName);
                }
            }
        } catch (Exception e) {
            logger.warn("Could not capture failure screenshot", e);
        }
    }
}
