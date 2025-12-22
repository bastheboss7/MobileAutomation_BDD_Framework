package com.automation.framework.utils;

import com.automation.framework.core.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Utility class for scrolling operations.
 * Supports both Android (UiScrollable) and iOS scrolling.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class ScrollUtils {
    private static final Logger logger = LoggerFactory.getLogger(ScrollUtils.class);
    private static final Duration SCROLL_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    
    private ScrollUtils() {
        // Private constructor
    }
    
    /**
     * Scroll to element by accessibility id (Android).
     * Uses UiScrollable for efficient scrolling.
     */
    public static WebElement scrollToElementByAccessibility(String accessibilityId) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return null;
        }
        try {
            // Reduce timeout for scroll attempts
            driver.manage().timeouts().implicitlyWait(SCROLL_TIMEOUT);
            
            String uiSelector = String.format(
                "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(\"%s\"))",
                accessibilityId
            );
            WebElement element = driver.findElement(AppiumBy.androidUIAutomator(uiSelector));
            
            // Restore timeout
            driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT);
            return element;
        } catch (Exception e) {
            driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT);
            logger.debug("Could not scroll to element with accessibility: {}", accessibilityId);
            return null;
        }
    }
    
    /**
     * Scroll to element by text (Android).
     */
    public static WebElement scrollToElementByText(String text) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return null;
        }
        try {
            driver.manage().timeouts().implicitlyWait(SCROLL_TIMEOUT);
            String uiSelector = String.format(
                "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(\"%s\"))",
                text
            );
            WebElement element = driver.findElement(AppiumBy.androidUIAutomator(uiSelector));
            driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT);
            return element;
        } catch (Exception e) {
            driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT);
            logger.debug("Could not scroll to element with text: {}", text);
            return null;
        }
    }
    
    /**
     * Scroll to element by resource id (Android).
     */
    public static WebElement scrollToElementById(String resourceId) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return null;
        }
        try {
            driver.manage().timeouts().implicitlyWait(SCROLL_TIMEOUT);
            String uiSelector = String.format(
                "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().resourceId(\"%s\"))",
                resourceId
            );
            WebElement element = driver.findElement(AppiumBy.androidUIAutomator(uiSelector));
            driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT);
            return element;
        } catch (Exception e) {
            driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT);
            logger.debug("Could not scroll to element with id: {}", resourceId);
            return null;
        }
    }
    
    /**
     * Scroll to element by partial text (Android).
     */
    public static WebElement scrollToElementByPartialText(String partialText) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return null;
        }
        try {
            driver.manage().timeouts().implicitlyWait(SCROLL_TIMEOUT);
            String uiSelector = String.format(
                "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains(\"%s\"))",
                partialText
            );
            WebElement element = driver.findElement(AppiumBy.androidUIAutomator(uiSelector));
            driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT);
            return element;
        } catch (Exception e) {
            driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT);
            logger.debug("Could not scroll to element with partial text: {}", partialText);
            return null;
        }
    }
    
    /**
     * Scroll down by percentage of screen.
     */
    public static void scrollDown(double percentage) {
        AppiumDriver driver = DriverManager.getDriver();
        var size = driver.manage().window().getSize();
        int startX = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.8);
        int endY = (int) (size.getHeight() * (0.8 - percentage));
        
        SwipeUtils.swipe(startX, startY, startX, endY);
    }
    
    /**
     * Scroll up by percentage of screen.
     */
    public static void scrollUp(double percentage) {
        AppiumDriver driver = DriverManager.getDriver();
        var size = driver.manage().window().getSize();
        int startX = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.2);
        int endY = (int) (size.getHeight() * (0.2 + percentage));
        
        SwipeUtils.swipe(startX, startY, startX, endY);
    }
}
