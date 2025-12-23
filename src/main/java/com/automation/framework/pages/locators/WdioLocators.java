package com.automation.framework.pages.locators;

/**
 * Centralized locator constants for WDIO Demo App.
 * All accessibility IDs are defined here for easy maintenance.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public final class WdioLocators {
    
    private WdioLocators() {
        // Prevent instantiation
    }
    
    // =========================================================================
    // Navigation Menu
    // =========================================================================
    public static final String NAV_LOGIN = "Login";
    public static final String NAV_HOME = "Home";
    public static final String NAV_WEBVIEW = "Webview";
    public static final String NAV_FORMS = "Forms";
    public static final String NAV_SWIPE = "Swipe";
    public static final String NAV_DRAG = "Drag";
    
    // =========================================================================
    // Login Screen
    // =========================================================================
    public static final String LOGIN_EMAIL_INPUT = "input-email";
    public static final String LOGIN_PASSWORD_INPUT = "input-password";
    public static final String LOGIN_BUTTON = "button-LOGIN";
    public static final String LOGIN_SIGNUP_TAB = "button-sign-up-container";
    
    // =========================================================================
    // Success/Error Messages & Modals
    // =========================================================================
    public static final String SUCCESS_MESSAGE = "You are logged in";
    public static final String SUCCESS_MODAL_TITLE = "Success";
    public static final String SUCCESS_MODAL_OK_BUTTON = "button-OK";
    public static final String ERROR_MESSAGE_CONTAINER = "error-message";
    public static final String VALIDATION_ERROR_TEXT = "Please enter";
    
    // XPath patterns for cross-platform message detection
    public static final String XPATH_SUCCESS_ALERT = "//*[contains(@text,'logged in') or contains(@label,'logged in') or contains(@name,'logged in')]";
    public static final String XPATH_ERROR_ALERT = "//*[contains(@text,'error') or contains(@text,'invalid') or contains(@label,'error') or contains(@label,'invalid')]";
    public static final String XPATH_MODAL_CONTAINER = "//*[@resource-id='android:id/alertTitle' or @type='XCUIElementTypeAlert']";
    public static final String XPATH_ANY_ALERT_TEXT = "//android.widget.TextView | //XCUIElementTypeStaticText";
    
    // =========================================================================
    // Forms Screen
    // =========================================================================
    public static final String FORMS_INPUT = "text-input";
    public static final String FORMS_SWITCH = "switch";
    public static final String FORMS_DROPDOWN = "Dropdown";
    public static final String FORMS_ACTIVE_BUTTON = "button-Active";
    public static final String FORMS_INACTIVE_BUTTON = "button-Inactive";
}
