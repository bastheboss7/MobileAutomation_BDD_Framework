package com.automation.framework.pages;

import com.automation.framework.core.ConfigManager;
import com.automation.framework.core.DriverManager;
import com.automation.framework.reports.ExtentReportManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all Page Objects.
 * Provides common functionality for element interaction.
 * 
 * @author Baskar
 * @version 3.1.0
 */
public abstract class BasePage {
    protected final Logger logger;
    
    protected BasePage() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }
    
    /**
     * Get the current driver instance.
     */
    protected AppiumDriver getDriver() {
        return DriverManager.getDriver();
    }
    
    /**
     * Get explicit wait timeout from config.
     * Explicit wait should always be >= implicit wait.
     * @return Duration for explicit waits
     */
    protected Duration getExplicitWaitTimeout() {
        return Duration.ofSeconds(ConfigManager.getInt("explicitWait", 30));
    }
    
    /**
     * Get a reusable WebDriverWait with default timeout from config.
     * Avoids creating new WebDriverWait objects in every method.
     * @return WebDriverWait instance
     */
    protected WebDriverWait getWait() {
        return new WebDriverWait(getDriver(), getExplicitWaitTimeout());
    }
    
    /**
     * Get a WebDriverWait with custom timeout.
     * @param timeout Custom timeout duration
     * @return WebDriverWait instance
     */
    protected WebDriverWait getWait(Duration timeout) {
        return new WebDriverWait(getDriver(), timeout);
    }
    
    // ==================== Reporting ====================
    
    /**
     * Report a test step result (logs to console and Extent Report).
     */
    protected void reportStep(String desc, String status) {
        switch (status.toUpperCase()) {
            case "PASS":
                logger.info("✅ PASS: {}", desc);
                ExtentReportManager.logPass(desc);
                break;
            case "FAIL":
                logger.error("❌ FAIL: {}", desc);
                ExtentReportManager.logFail(desc);
                throw new RuntimeException("FAILED: " + desc);
            case "INFO":
                logger.info("ℹ️ INFO: {}", desc);
                ExtentReportManager.logInfo(desc);
                break;
            case "WARN":
                logger.warn("⚠️ WARN: {}", desc);
                ExtentReportManager.logWarning(desc);
                break;
            default:
                logger.info(desc);
                ExtentReportManager.logInfo(desc);
        }
    }
    
    // ==================== Click Methods ====================
    
    /**
     * Click element by accessibility id.
     * Waits for element to be visible before clicking.
     * Note: Using visibilityOf instead of elementToBeClickable for better mobile compatibility.
     */
    protected void clickByAccessibility(String accessibilityId) {
        try {
            WebElement element = getWait().until(
                    ExpectedConditions.visibilityOfElementLocated(AppiumBy.accessibilityId(accessibilityId)));
            element.click();
            logger.info("Clicked element with accessibility: {}", accessibilityId);
        } catch (Exception e) {
            logger.error("Failed to click element with accessibility: {}", accessibilityId, e);
            throw e;
        }
    }
    
    /**
     * Click element by resource ID (Android) or name (iOS).
     * Waits for element to be visible before clicking.
     */
    protected void clickById(String id) {
        try {
            WebElement element = getWait().until(
                    ExpectedConditions.visibilityOfElementLocated(AppiumBy.id(id)));
            element.click();
            logger.info("Clicked element with id: {}", id);
        } catch (Exception e) {
            logger.error("Failed to click element with id: {}", id, e);
            throw e;
        }
    }
    
    /**
     * Click element by XPath.
     * Waits for element to be visible before clicking.
     */
    protected void clickByXpath(String xpath) {
        try {
            WebElement element = getWait().until(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
            element.click();
            logger.info("Clicked element with xpath: {}", xpath);
        } catch (Exception e) {
            logger.error("Failed to click element with xpath: {}", xpath, e);
            throw e;
        }
    }
    
    /**
     * Click WebElement.
     * Waits for element to be visible before clicking.
     */
    protected void click(WebElement element) {
        try {
            getWait().until(ExpectedConditions.visibilityOf(element));
            element.click();
            logger.info("Clicked element: {}", element);
        } catch (Exception e) {
            logger.error("Failed to click element", e);
            throw e;
        }
    }
    
    // ==================== Enter Text Methods ====================
    
    /**
     * Enter text by accessibility id.
     * Waits for element to be visible before entering text.
     */
    protected void enterByAccessibility(String accessibilityId, String text) {
        try {
            WebElement element = getWait().until(
                    ExpectedConditions.visibilityOfElementLocated(AppiumBy.accessibilityId(accessibilityId)));
            element.clear();
            element.sendKeys(text);
            logger.info("Entered text in element with accessibility: {}", accessibilityId);
        } catch (Exception e) {
            logger.error("Failed to enter text in element with accessibility: {}", accessibilityId, e);
            throw e;
        }
    }
    
    /**
     * Enter text by resource ID (Android) or name (iOS).
     * Waits for element to be visible before entering text.
     */
    protected void enterById(String id, String text) {
        try {
            WebElement element = getWait().until(
                    ExpectedConditions.visibilityOfElementLocated(AppiumBy.id(id)));
            element.clear();
            element.sendKeys(text);
            logger.info("Entered text in element with id: {}", id);
        } catch (Exception e) {
            logger.error("Failed to enter text in element with id: {}", id, e);
            throw e;
        }
    }
    
    /**
     * Enter text by XPath.
     * Waits for element to be visible before entering text.
     */
    protected void enterByXpath(String xpath, String text) {
        try {
            WebElement element = getWait().until(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
            element.clear();
            element.sendKeys(text);
            logger.info("Entered text in element with xpath: {}", xpath);
        } catch (Exception e) {
            logger.error("Failed to enter text in element with xpath: {}", xpath, e);
            throw e;
        }
    }
    
    // ==================== Verification Methods ====================
    
    /**
     * Check if element is displayed by accessibility id.
     */
    protected boolean isDisplayedByAccessibility(String accessibilityId) {
        try {
            return getDriver().findElement(AppiumBy.accessibilityId(accessibilityId)).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Check if element is displayed by resource ID (Android) or name (iOS).
     */
    protected boolean isDisplayedById(String id) {
        try {
            return getDriver().findElement(AppiumBy.id(id)).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Check if element is displayed by XPath.
     */
    protected boolean isDisplayedByXpath(String xpath) {
        try {
            return getDriver().findElement(By.xpath(xpath)).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Get text from element by resource ID (Android) or name (iOS).
     */
    protected String getTextById(String id) {
        return getDriver().findElement(AppiumBy.id(id)).getText();
    }
    
    /**
     * Get text from element by XPath.
     */
    protected String getTextByXpath(String xpath) {
        return getDriver().findElement(By.xpath(xpath)).getText();
    }
    
    // ==================== Wait Methods ====================
    
    /**
     * Wait for element to be visible.
     */
    protected WebElement waitForElement(By locator) {
        return getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Wait for element to be visible with custom timeout.
     */
    protected WebElement waitForElement(By locator, Duration timeout) {
        return getWait(timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Find all elements by XPath.
     */
    protected List<WebElement> findElementsByXpath(String xpath) {
        return getDriver().findElements(By.xpath(xpath));
    }
    
    // ==================== Gesture Methods ====================
    
    /**
     * Swipe direction enum.
     */
    public enum SwipeDirection {
        UP, DOWN, LEFT, RIGHT
    }
    
    /**
     * Swipe in a direction on the screen.
     * @param direction Swipe direction (UP, DOWN, LEFT, RIGHT)
     */
    protected void swipe(SwipeDirection direction) {
        swipe(direction, 0.75);
    }
    
    /**
     * Swipe in a direction with custom distance.
     * @param direction Swipe direction
     * @param swipeRatio How far to swipe (0.0 to 1.0, default 0.75 = 75% of screen)
     */
    protected void swipe(SwipeDirection direction, double swipeRatio) {
        Dimension size = getDriver().manage().window().getSize();
        int centerX = size.width / 2;
        int centerY = size.height / 2;
        
        int startX, startY, endX, endY;
        
        switch (direction) {
            case UP -> {
                startX = centerX;
                startY = (int) (size.height * 0.8);
                endX = centerX;
                endY = (int) (size.height * (0.8 - swipeRatio * 0.6));
            }
            case DOWN -> {
                startX = centerX;
                startY = (int) (size.height * 0.2);
                endX = centerX;
                endY = (int) (size.height * (0.2 + swipeRatio * 0.6));
            }
            case LEFT -> {
                startX = (int) (size.width * 0.8);
                startY = centerY;
                endX = (int) (size.width * (0.8 - swipeRatio * 0.6));
                endY = centerY;
            }
            case RIGHT -> {
                startX = (int) (size.width * 0.2);
                startY = centerY;
                endX = (int) (size.width * (0.2 + swipeRatio * 0.6));
                endY = centerY;
            }
            default -> throw new IllegalArgumentException("Unknown direction: " + direction);
        }
        
        performSwipe(startX, startY, endX, endY, Duration.ofMillis(300));
        logger.info("Swiped {} on screen", direction);
    }
    
    /**
     * Swipe from one point to another.
     * @param startX Start X coordinate
     * @param startY Start Y coordinate
     * @param endX End X coordinate
     * @param endY End Y coordinate
     * @param duration Swipe duration
     */
    protected void performSwipe(int startX, int startY, int endX, int endY, Duration duration) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(duration, PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        
        getDriver().perform(Collections.singletonList(swipe));
    }
    
    /**
     * Scroll down until element is visible.
     * @param locator Element locator to find
     * @param maxSwipes Maximum number of swipe attempts
     * @return The found element
     */
    protected WebElement scrollToElement(By locator, int maxSwipes) {
        for (int i = 0; i < maxSwipes; i++) {
            try {
                WebElement element = getDriver().findElement(locator);
                if (element.isDisplayed()) {
                    logger.info("Found element after {} swipes", i);
                    return element;
                }
            } catch (NoSuchElementException ignored) {
                // Element not found yet, continue scrolling
            }
            swipe(SwipeDirection.UP);
        }
        throw new NoSuchElementException("Element not found after " + maxSwipes + " swipes: " + locator);
    }
    
    /**
     * Scroll down until text is visible.
     * @param text Text to find
     * @param maxSwipes Maximum number of swipe attempts
     * @return The found element containing the text
     */
    protected WebElement scrollToText(String text, int maxSwipes) {
        By locator = By.xpath("//*[contains(@text,'" + text + "') or contains(@label,'" + text + "')]");
        return scrollToElement(locator, maxSwipes);
    }
    
    /**
     * Long press on an element.
     * @param element Element to long press
     * @param duration How long to hold (default 1 second)
     */
    protected void longPress(WebElement element, Duration duration) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        int centerX = location.getX() + size.getWidth() / 2;
        int centerY = location.getY() + size.getHeight() / 2;
        
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence longPress = new Sequence(finger, 1);
        
        longPress.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX, centerY));
        longPress.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        longPress.addAction(new Pause(finger, duration));
        longPress.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        
        getDriver().perform(Collections.singletonList(longPress));
        logger.info("Long pressed element for {} ms", duration.toMillis());
    }
    
    /**
     * Long press on element by accessibility id.
     * @param accessibilityId Accessibility ID
     */
    protected void longPressByAccessibility(String accessibilityId) {
        WebElement element = getDriver().findElement(AppiumBy.accessibilityId(accessibilityId));
        longPress(element, Duration.ofSeconds(1));
    }
    
    /**
     * Double tap on an element.
     * @param element Element to double tap
     */
    protected void doubleTap(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        int centerX = location.getX() + size.getWidth() / 2;
        int centerY = location.getY() + size.getHeight() / 2;
        
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence doubleTap = new Sequence(finger, 1);
        
        // First tap
        doubleTap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX, centerY));
        doubleTap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        doubleTap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        // Short pause between taps
        doubleTap.addAction(new Pause(finger, Duration.ofMillis(100)));
        // Second tap
        doubleTap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        doubleTap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        
        getDriver().perform(Collections.singletonList(doubleTap));
        logger.info("Double tapped element");
    }
    
    /**
     * Drag element from source to target.
     * @param source Source element
     * @param target Target element
     */
    protected void dragAndDrop(WebElement source, WebElement target) {
        Point sourceCenter = getElementCenter(source);
        Point targetCenter = getElementCenter(target);
        
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence drag = new Sequence(finger, 1);
        
        drag.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), sourceCenter.getX(), sourceCenter.getY()));
        drag.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        drag.addAction(new Pause(finger, Duration.ofMillis(500))); // Hold before dragging
        drag.addAction(finger.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(), targetCenter.getX(), targetCenter.getY()));
        drag.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        
        getDriver().perform(Collections.singletonList(drag));
        logger.info("Dragged element from ({},{}) to ({},{})", 
                sourceCenter.getX(), sourceCenter.getY(), targetCenter.getX(), targetCenter.getY());
    }
    
    /**
     * Tap at specific coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     */
    protected void tapAtCoordinates(int x, int y) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);
        
        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        
        getDriver().perform(Collections.singletonList(tap));
        logger.info("Tapped at coordinates ({},{})", x, y);
    }
    
    /**
     * Get center point of an element.
     */
    private Point getElementCenter(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        return new Point(location.getX() + size.getWidth() / 2, location.getY() + size.getHeight() / 2);
    }
}
