package com.automation.framework.pages;

import com.automation.framework.core.DriverManager;
import com.automation.framework.reports.ExtentReportManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
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
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    
    protected BasePage() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }
    
    /**
     * Get the current driver instance.
     */
    protected AppiumDriver getDriver() {
        return DriverManager.getDriver();
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
     */
    protected void clickByAccessibility(String accessibilityId) {
        try {
            WebElement element = getDriver().findElement(AppiumBy.accessibilityId(accessibilityId));
            element.click();
            logger.info("Clicked element with accessibility: {}", accessibilityId);
        } catch (Exception e) {
            logger.error("Failed to click element with accessibility: {}", accessibilityId, e);
            throw e;
        }
    }
    
    /**
     * Click element by resource ID (Android) or name (iOS).
     */
    protected void clickById(String id) {
        try {
            WebElement element = getDriver().findElement(AppiumBy.id(id));
            element.click();
            logger.info("Clicked element with id: {}", id);
        } catch (Exception e) {
            logger.error("Failed to click element with id: {}", id, e);
            throw e;
        }
    }
    
    /**
     * Click element by XPath.
     */
    protected void clickByXpath(String xpath) {
        try {
            WebElement element = getDriver().findElement(By.xpath(xpath));
            element.click();
            logger.info("Clicked element with xpath: {}", xpath);
        } catch (Exception e) {
            logger.error("Failed to click element with xpath: {}", xpath, e);
            throw e;
        }
    }
    
    /**
     * Click WebElement.
     */
    protected void click(WebElement element) {
        try {
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
            WebElement element = getDriver().findElement(AppiumBy.accessibilityId(accessibilityId));
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
     */
    protected void enterById(String id, String text) {
        try {
            WebElement element = getDriver().findElement(AppiumBy.id(id));
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
     */
    protected void enterByXpath(String xpath, String text) {
        try {
            WebElement element = getDriver().findElement(By.xpath(xpath));
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
        WebDriverWait wait = new WebDriverWait(getDriver(), DEFAULT_TIMEOUT);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Wait for element to be visible with custom timeout.
     */
    protected WebElement waitForElement(By locator, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(getDriver(), timeout);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Find all elements by XPath.
     */
    protected List<WebElement> findElementsByXpath(String xpath) {
        return getDriver().findElements(By.xpath(xpath));
    }
}
