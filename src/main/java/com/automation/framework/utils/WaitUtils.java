package com.automation.framework.utils;

import com.automation.framework.core.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Utility class for explicit waits.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class WaitUtils {
    private static final Logger logger = LoggerFactory.getLogger(WaitUtils.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    
    private WaitUtils() {
        // Private constructor
    }
    
    /**
     * Wait for element to be visible.
     */
    public static WebElement waitForVisible(WebElement element) {
        return waitForVisible(element, DEFAULT_TIMEOUT);
    }
    
    /**
     * Wait for element to be visible with custom timeout.
     */
    public static WebElement waitForVisible(WebElement element, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), timeout);
        return wait.until(ExpectedConditions.visibilityOf(element));
    }
    
    /**
     * Wait for element to be visible by locator.
     */
    public static WebElement waitForVisible(By locator) {
        return waitForVisible(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Wait for element to be visible by locator with custom timeout.
     */
    public static WebElement waitForVisible(By locator, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), timeout);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Wait for element to be clickable.
     */
    public static WebElement waitForClickable(WebElement element) {
        return waitForClickable(element, DEFAULT_TIMEOUT);
    }
    
    /**
     * Wait for element to be clickable with custom timeout.
     */
    public static WebElement waitForClickable(WebElement element, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), timeout);
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }
    
    /**
     * Wait for element to be clickable by locator.
     */
    public static WebElement waitForClickable(By locator) {
        return waitForClickable(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Wait for element to be clickable by locator with custom timeout.
     */
    public static WebElement waitForClickable(By locator, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), timeout);
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    /**
     * Wait for element to disappear.
     */
    public static boolean waitForInvisible(By locator) {
        return waitForInvisible(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Wait for element to disappear with custom timeout.
     */
    public static boolean waitForInvisible(By locator, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), timeout);
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
    
    /**
     * Wait for presence of all elements.
     */
    public static List<WebElement> waitForAllPresent(By locator) {
        return waitForAllPresent(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Wait for presence of all elements with custom timeout.
     */
    public static List<WebElement> waitForAllPresent(By locator, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), timeout);
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }
    
    /**
     * Wait for text to be present in element.
     */
    public static boolean waitForText(WebElement element, String text) {
        return waitForText(element, text, DEFAULT_TIMEOUT);
    }
    
    /**
     * Wait for text to be present in element with custom timeout.
     */
    public static boolean waitForText(WebElement element, String text, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), timeout);
        return wait.until(ExpectedConditions.textToBePresentInElement(element, text));
    }
    
    /**
     * Wait for URL to contain specific text.
     */
    public static boolean waitForUrlContains(String urlPart) {
        return waitForUrlContains(urlPart, DEFAULT_TIMEOUT);
    }
    
    /**
     * Wait for URL to contain specific text with custom timeout.
     */
    public static boolean waitForUrlContains(String urlPart, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), timeout);
        return wait.until(ExpectedConditions.urlContains(urlPart));
    }
    
    /**
     * Simple thread sleep (use sparingly).
     */
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Sleep interrupted", e);
        }
    }
}
