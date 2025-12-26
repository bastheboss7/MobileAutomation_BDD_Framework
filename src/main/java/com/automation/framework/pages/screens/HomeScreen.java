package com.automation.framework.pages.screens;

import com.automation.framework.pages.BasePage;
import com.automation.framework.pages.locators.WdioLocators;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object for WDIO Demo App Home Screen.
 * Contains navigation actions and home screen verifications.
 * This is the main landing screen with bottom navigation tabs.
 * 
 * @author Baskar
 * @version 1.0.0
 */
public class HomeScreen extends BasePage {

    // ==================== Navigation Methods ====================

    /**
     * Navigate to the Login screen via bottom navigation.
     * Uses robust click with fallback to ensure navigation on different
     * devices/Appium versions.
     */
    public void navigateToLogin() {
        try {
            clickByAccessibility(WdioLocators.NAV_LOGIN);
        } catch (Exception e) {
            logger.warn("Click by accessibility failed for Login tab, trying XPath fallback...");
            clickByXpath(WdioLocators.XPATH_NAV_LOGIN);
        }
        logger.info("Navigated to Login screen");
    }

    /**
     * Navigate to the Home screen via bottom navigation.
     */
    public void navigateToHome() {
        clickByAccessibility(WdioLocators.NAV_HOME);
        logger.info("Navigated to Home screen");
    }

    /**
     * Navigate to the Webview screen via bottom navigation.
     */
    public void navigateToWebview() {
        clickByAccessibility(WdioLocators.NAV_WEBVIEW);
        logger.info("Navigated to Webview screen");
    }

    /**
     * Navigate to the Forms screen via bottom navigation.
     */
    public void navigateToForms() {
        clickByAccessibility(WdioLocators.NAV_FORMS);
        logger.info("Navigated to Forms screen");
    }

    /**
     * Navigate to the Swipe screen via bottom navigation.
     */
    public void navigateToSwipe() {
        clickByAccessibility(WdioLocators.NAV_SWIPE);
        logger.info("Navigated to Swipe screen");
    }

    /**
     * Navigate to the Drag screen via bottom navigation.
     */
    public void navigateToDrag() {
        clickByAccessibility(WdioLocators.NAV_DRAG);
        logger.info("Navigated to Drag screen");
    }

    // ==================== Verification Methods ====================

    /**
     * Check if Home screen is displayed.
     * 
     * @return true if on home screen
     */
    public boolean isHomeScreenDisplayed() {
        try {
            getWait().until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.accessibilityId(WdioLocators.NAV_HOME)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if bottom navigation is visible.
     * 
     * @return true if navigation tabs are visible
     */
    public boolean isNavigationVisible() {
        return isDisplayedByAccessibility(WdioLocators.NAV_HOME) &&
                isDisplayedByAccessibility(WdioLocators.NAV_LOGIN);
    }
}
