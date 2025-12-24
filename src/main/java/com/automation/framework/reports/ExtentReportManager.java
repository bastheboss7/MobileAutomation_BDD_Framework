package com.automation.framework.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.automation.framework.core.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * Manages Extent Reports for test execution.
 * Thread-safe implementation for parallel execution.
 * 
 * @author Baskar
 * @version 1.0.0
 */
public class ExtentReportManager {
    private static final Logger logger = LoggerFactory.getLogger(ExtentReportManager.class);
    
    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private static final String REPORT_DIR = "target/extent-reports/";
    private static String reportPath;
    
    private ExtentReportManager() {
        // Private constructor
    }
    
    /**
     * Initialize the Extent Reports instance.
     * Should be called once at the start of test execution.
     */
    public static synchronized void initReports() {
        if (extentReports == null) {
            // Create report directory
            new File(REPORT_DIR).mkdirs();
            
            // Generate unique report name with timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            reportPath = REPORT_DIR + "TestReport_" + timestamp + ".html";
            
            // Configure Spark Reporter
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setDocumentTitle("Mobile Automation Test Report");
            sparkReporter.config().setReportName("WDIO Demo App - Test Results");
            sparkReporter.config().setTimeStampFormat("dd-MM-yyyy HH:mm:ss");
            sparkReporter.config().setEncoding("UTF-8");
            
            // Initialize ExtentReports
            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);
            
            // Add system info
            extentReports.setSystemInfo("Platform", ConfigManager.getPlatform().toUpperCase());
            extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
            extentReports.setSystemInfo("OS", System.getProperty("os.name"));
            extentReports.setSystemInfo("User", System.getProperty("user.name"));
            
            logger.info("Extent Reports initialized: {}", reportPath);
        }
    }
    
    /**
     * Create a new test in the report.
     * @param testName Name of the test/scenario
     * @return ExtentTest instance
     */
    public static ExtentTest createTest(String testName) {
        initReports();
        ExtentTest test = extentReports.createTest(testName);
        extentTest.set(test);
        logger.debug("Created test in report: {}", testName);
        return test;
    }
    
    /**
     * Get the current test instance.
     */
    public static ExtentTest getTest() {
        return extentTest.get();
    }
    
    /**
     * Log a step with status.
     */
    public static void logStep(Status status, String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(status, message);
        }
    }
    
    /**
     * Log a passing step.
     */
    public static void logPass(String message) {
        logStep(Status.PASS, "✅ " + message);
    }
    
    /**
     * Log a failing step.
     */
    public static void logFail(String message) {
        logStep(Status.FAIL, "❌ " + message);
    }
    
    /**
     * Log an info step.
     */
    public static void logInfo(String message) {
        logStep(Status.INFO, "ℹ️ " + message);
    }
    
    /**
     * Log a warning step.
     */
    public static void logWarning(String message) {
        logStep(Status.WARNING, "⚠️ " + message);
    }
    
    /**
     * Log a skip step.
     */
    public static void logSkip(String message) {
        logStep(Status.SKIP, "⏭️ " + message);
    }
    
    /**
     * Attach screenshot to the report.
     * @param screenshotBytes Screenshot as byte array
     * @param title Screenshot title
     */
    public static void attachScreenshot(byte[] screenshotBytes, String title) {
        ExtentTest test = getTest();
        if (test != null && screenshotBytes != null) {
            try {
                String base64Screenshot = Base64.getEncoder().encodeToString(screenshotBytes);
                test.info(title, MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
                logger.info("📸 Screenshot attached to Extent Report: {}", title);
            } catch (Exception e) {
                logger.warn("Failed to attach screenshot: {}", e.getMessage());
            }
        } else {
            logger.warn("Cannot attach screenshot - test: {}, screenshotBytes: {}", 
                    test != null ? "exists" : "NULL", 
                    screenshotBytes != null ? "exists" : "NULL");
        }
    }
    
    /**
     * Attach screenshot on failure.
     */
    public static void attachScreenshotOnFailure(byte[] screenshotBytes, String scenarioName) {
        ExtentTest test = getTest();
        if (test != null && screenshotBytes != null) {
            try {
                String base64Screenshot = Base64.getEncoder().encodeToString(screenshotBytes);
                test.fail("Screenshot on Failure", 
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
                logger.debug("Failure screenshot attached for: {}", scenarioName);
            } catch (Exception e) {
                logger.warn("Failed to attach failure screenshot: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Flush the reports (write to file).
     * Should be called after all tests complete.
     */
    public static synchronized void flushReports() {
        if (extentReports != null) {
            extentReports.flush();
            logger.info("Extent Reports saved to: {}", reportPath);
        }
        extentTest.remove();
    }
    
    /**
     * Get the report file path.
     */
    public static String getReportPath() {
        return reportPath;
    }
}
