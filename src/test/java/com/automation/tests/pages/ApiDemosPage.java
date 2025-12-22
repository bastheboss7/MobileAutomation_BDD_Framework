package com.automation.tests.pages;

import com.automation.framework.pages.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Page Object for the API Demos app (native app testing).
 * Locators are externalized in object.properties with prefix "ApiDemos."
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class ApiDemosPage extends BasePage {
    
    // Locator keys from object.properties
    private static final String VIEWS_KEY = "ApiDemos.Views.Accessibility";
    private static final String ANIMATION_KEY = "ApiDemos.Animation.Accessibility";
    private static final String APP_KEY = "ApiDemos.App.Accessibility";
    
    /**
     * Click on Views menu item.
     */
    public void clickViews() {
        String locator = getLocator(VIEWS_KEY);
        clickByAccessibility(locator != null ? locator : "Views");
        logger.info("Clicked on Views");
    }
    
    /**
     * Click on Animation menu item.
     */
    public void clickAnimation() {
        String locator = getLocator(ANIMATION_KEY);
        clickByAccessibility(locator != null ? locator : "Animation");
        logger.info("Clicked on Animation");
    }
    
    /**
     * Click on App menu item.
     */
    public void clickApp() {
        String locator = getLocator(APP_KEY);
        clickByAccessibility(locator != null ? locator : "App");
        logger.info("Clicked on App");
    }
    
    /**
     * Click on any menu item by name.
     * Tries to get locator from object.properties first.
     * @param itemName The menu item to click
     */
    public void clickMenuItem(String itemName) {
        // Try to get from object.properties first
        String key = "ApiDemos." + itemName + ".Accessibility";
        String locator = getLocator(key);
        clickByAccessibility(locator != null ? locator : itemName);
        logger.info("Clicked on menu item: {}", itemName);
    }
    
    /**
     * Navigate to a nested menu.
     * @param menuItems List of menu items to navigate through
     */
    public void navigateTo(String... menuItems) {
        for (String item : menuItems) {
            clickMenuItem(item);
            logger.debug("Navigated to: {}", item);
        }
    }
    
    /**
     * Check if a menu item is displayed.
     * @param itemName The menu item to check
     * @return true if displayed
     */
    public boolean isMenuItemDisplayed(String itemName) {
        String key = "ApiDemos." + itemName + ".Accessibility";
        String locator = getLocator(key);
        return isDisplayedByAccessibility(locator != null ? locator : itemName);
    }
    
    /**
     * Get the page title.
     */
    public String getTitle() {
        try {
            return getDriver().getTitle();
        } catch (Exception e) {
            return "";
        }
    }
}
