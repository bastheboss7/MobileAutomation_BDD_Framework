package com.automation.framework.utils;

import com.automation.framework.core.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for capturing screenshots.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class ScreenshotUtils {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = "target/screenshots";
    
    private ScreenshotUtils() {
        // Private constructor
    }
    
    /**
     * Capture screenshot as byte array.
     * @return Screenshot bytes or empty array on failure
     */
    public static byte[] captureAsBytes() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (driver != null) {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            }
        } catch (Exception e) {
            logger.error("Failed to capture screenshot", e);
        }
        return new byte[0];
    }
    
    /**
     * Capture screenshot and save to file.
     * @param fileName Name of the file (without extension)
     * @return Path to saved file or null on failure
     */
    public static String captureToFile(String fileName) {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            if (driver == null) {
                logger.warn("Cannot capture screenshot - driver is null");
                return null;
            }
            
            // Ensure directory exists
            Path dir = Paths.get(SCREENSHOT_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            
            // Generate unique filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fullFileName = String.format("%s_%s.png", fileName, timestamp);
            Path filePath = dir.resolve(fullFileName);
            
            // Capture and save
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot.toPath(), filePath);
            
            logger.info("Screenshot saved: {}", filePath);
            return filePath.toString();
            
        } catch (IOException e) {
            logger.error("Failed to save screenshot", e);
            return null;
        }
    }
    
    /**
     * Capture screenshot with auto-generated name.
     * @return Path to saved file
     */
    public static String capture() {
        return captureToFile("screenshot");
    }
    
    /**
     * Capture screenshot on test failure.
     * @param testName Name of the failed test
     * @return Path to saved file
     */
    public static String captureOnFailure(String testName) {
        String safeName = testName.replaceAll("[^a-zA-Z0-9]", "_");
        return captureToFile("FAILED_" + safeName);
    }
}
