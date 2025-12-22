package com.automation.framework.pages;

import com.automation.framework.core.ConfigManager;
import com.automation.framework.core.DriverManager;
import com.automation.framework.utils.ScreenshotUtils;
import com.automation.framework.utils.ScrollUtils;
import com.automation.framework.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Base class for all Page Objects.
 * Provides common functionality for element interaction.
 * Uses composition over inheritance for utilities.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public abstract class BasePage {
    protected final Logger logger;
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    
    protected BasePage() {
        this.logger = LoggerFactory.getLogger(this.getClass());
        // Note: PageFactory.initElements removed due to Appium 9.x compatibility issues
        // Use direct element finding methods instead of @FindBy annotations
    }
    
    /**
     * Get the current driver instance.
     */
    protected AppiumDriver getDriver() {
        return DriverManager.getDriver();
    }
    
    /**
     * Get a locator from object.properties.
     */
    protected String getLocator(String key) {
        return ConfigManager.getLocator(key);
    }
    
    // ==================== Click Methods ====================
    
    /**
     * Click element by accessibility id.
     * Scrolls to element first if not visible.
     */
    protected void clickByAccessibility(String accessibilityId) {
        try {
            WebElement element = getDriver().findElement(AppiumBy.accessibilityId(accessibilityId));
            element.click();
            logger.info("Clicked element with accessibility: {}", accessibilityId);
        } catch (Exception e) {
            // Try scrolling to element if not found
            WebElement scrolledElement = ScrollUtils.scrollToElementByAccessibility(accessibilityId);
            if (scrolledElement != null) {
                scrolledElement.click();
                logger.info("Clicked element with accessibility after scroll: {}", accessibilityId);
            } else {
                logger.error("Failed to click element with accessibility: {}", accessibilityId, e);
                throw e;
            }
        }
    }
    
    /**
     * Click element by ID.
     * Scrolls to element first if not visible.
     */
    protected void clickById(String id) {
        try {
            WebElement element = getDriver().findElement(By.id(id));
            element.click();
            logger.info("Clicked element with id: {}", id);
        } catch (Exception e) {
            // Try scrolling to element if not found
            WebElement scrolledElement = ScrollUtils.scrollToElementById(id);
            if (scrolledElement != null) {
                scrolledElement.click();
                logger.info("Clicked element with id after scroll: {}", id);
            } else {
                logger.error("Failed to click element with id: {}", id, e);
                throw e;
            }
        }
    }
    
    /**
     * Click element by XPath.
     */
    protected void clickByXpath(String xpath) {
        try {
            WebElement element = WaitUtils.waitForClickable(By.xpath(xpath));
            element.click();
            logger.info("Clicked element with xpath: {}", xpath);
        } catch (Exception e) {
            logger.error("Failed to click element with xpath: {}", xpath, e);
            throw e;
        }
    }
    
    /**
     * Click element by text with scroll support.
     */
    protected void clickByText(String text) {
        try {
            WebElement element = ScrollUtils.scrollToElementByText(text);
            if (element == null) {
                element = getDriver().findElement(By.xpath("//*[@text='" + text + "']"));
            }
            element.click();
            logger.info("Clicked element with text: {}", text);
        } catch (Exception e) {
            logger.error("Failed to click element with text: {}", text, e);
            throw e;
        }
    }
    
    /**
     * Click WebElement.
     */
    protected void click(WebElement element) {
        try {
            WaitUtils.waitForClickable(element);
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
     */
    protected void enterByAccessibility(String accessibilityId, String text) {
        try {
            WebElement element = ScrollUtils.scrollToElementByAccessibility(accessibilityId);
            if (element == null) {
                element = getDriver().findElement(AppiumBy.accessibilityId(accessibilityId));
            }
            element.clear();
            element.sendKeys(text);
            logger.info("Entered '{}' in element with accessibility: {}", text, accessibilityId);
        } catch (Exception e) {
            logger.error("Failed to enter text in element with accessibility: {}", accessibilityId, e);
            throw e;
        }
    }
    
    /**
     * Enter text by ID.
     */
    protected void enterById(String id, String text) {
        try {
            WebElement element = ScrollUtils.scrollToElementById(id);
            if (element == null) {
                element = getDriver().findElement(By.id(id));
            }
            element.clear();
            element.sendKeys(text);
            logger.info("Entered '{}' in element with id: {}", text, id);
        } catch (Exception e) {
            logger.error("Failed to enter text in element with id: {}", id, e);
            throw e;
        }
    }
    
    /**
     * Enter text by XPath.
     */
    protected void enterByXpath(String xpath, String text) {
        try {
            WebElement element = WaitUtils.waitForVisible(By.xpath(xpath));
            element.clear();
            element.sendKeys(text);
            logger.info("Entered '{}' in element with xpath: {}", text, xpath);
        } catch (Exception e) {
            logger.error("Failed to enter text in element with xpath: {}", xpath, e);
            throw e;
        }
    }
    
    /**
     * Enter text in WebElement.
     */
    protected void enter(WebElement element, String text) {
        try {
            WaitUtils.waitForVisible(element);
            element.clear();
            element.sendKeys(text);
            logger.info("Entered '{}' in element", text);
        } catch (Exception e) {
            logger.error("Failed to enter text in element", e);
            throw e;
        }
    }
    
    // ==================== Verification Methods ====================
    
    /**
     * Check if element is displayed by ID.
     */
    protected boolean isDisplayedById(String id) {
        try {
            return getDriver().findElement(By.id(id)).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
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
     * Get text from element by ID.
     */
    protected String getTextById(String id) {
        return getDriver().findElement(By.id(id)).getText();
    }
    
    /**
     * Get text from element by XPath.
     */
    protected String getTextByXpath(String xpath) {
        return getDriver().findElement(By.xpath(xpath)).getText();
    }
    
    // ==================== Frame Methods ====================
    
    /**
     * Switch to iframe by XPath.
     */
    protected void switchToFrameByXpath(String xpath) {
        WebElement frame = getDriver().findElement(By.xpath(xpath));
        getDriver().switchTo().frame(frame);
        logger.debug("Switched to frame: {}", xpath);
    }
    
    /**
     * Switch to parent frame.
     */
    protected void switchToParentFrame() {
        getDriver().switchTo().parentFrame();
        logger.debug("Switched to parent frame");
    }
    
    /**
     * Switch to default content.
     */
    protected void switchToDefaultContent() {
        getDriver().switchTo().defaultContent();
        logger.debug("Switched to default content");
    }
    
    // ==================== Wait Methods ====================
    
    /**
     * Wait for element to be visible.
     */
    protected WebElement waitForElement(By locator) {
        return WaitUtils.waitForVisible(locator);
    }
    
    /**
     * Wait for element to be visible with custom timeout.
     */
    protected WebElement waitForElement(By locator, Duration timeout) {
        return WaitUtils.waitForVisible(locator, timeout);
    }
    
    /**
     * Find all elements by XPath.
     */
    protected List<WebElement> findElementsByXpath(String xpath) {
        return getDriver().findElements(By.xpath(xpath));
    }
    
    /**
     * Take a screenshot.
     */
    protected String takeScreenshot(String name) {
        return ScreenshotUtils.captureToFile(name);
    }
}
