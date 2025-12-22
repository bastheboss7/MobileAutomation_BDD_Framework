package pages.locators;

/**
 * Centralized locator constants for WDIO Demo App.
 * All accessibility IDs are defined here for easy maintenance.
 * 
 * @author Baskar
 * @version 1.0.0
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
    // Success/Error Messages
    // =========================================================================
    public static final String SUCCESS_MESSAGE = "You are logged in";
    public static final String ERROR_MESSAGE_CONTAINER = "error-message";
    
    // =========================================================================
    // Forms Screen
    // =========================================================================
    public static final String FORMS_INPUT = "text-input";
    public static final String FORMS_SWITCH = "switch";
    public static final String FORMS_DROPDOWN = "Dropdown";
    public static final String FORMS_ACTIVE_BUTTON = "button-Active";
    public static final String FORMS_INACTIVE_BUTTON = "button-Inactive";
}
