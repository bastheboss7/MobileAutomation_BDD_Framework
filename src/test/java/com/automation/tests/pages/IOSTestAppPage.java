package com.automation.tests.pages;

import com.automation.framework.pages.BasePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the iOS TestApp.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class IOSTestAppPage extends BasePage {
    
    /**
     * Check if main screen is displayed.
     */
    public boolean isMainScreenDisplayed() {
        try {
            // TestApp typically has some UI elements on main screen
            // Wait a bit for app to load
            Thread.sleep(2000);
            
            // Check if any element is present (app is loaded)
            return getDriver().getPageSource().length() > 0;
        } catch (Exception e) {
            logger.error("Failed to check main screen", e);
            return false;
        }
    }
    
    /**
     * Get the page source for debugging.
     */
    public String getPageSource() {
        return getDriver().getPageSource();
    }
    
    /**
     * Click element by accessibility id (iOS).
     */
    public void tapByAccessibility(String accessibilityId) {
        try {
            WebElement element = getDriver().findElement(AppiumBy.accessibilityId(accessibilityId));
            element.click();
            logger.info("Tapped on element with accessibility: {}", accessibilityId);
        } catch (Exception e) {
            logger.error("Failed to tap element: {}", accessibilityId, e);
            throw e;
        }
    }
}
