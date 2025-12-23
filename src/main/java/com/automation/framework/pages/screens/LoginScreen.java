package com.automation.framework.pages.screens;

import com.automation.framework.pages.BasePage;
import com.automation.framework.pages.locators.WdioLocators;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for WDIO Demo App Login Screen.
 * Contains all actions and verifications for the login functionality.
 * Follows Page Object Model pattern with locators externalized.
 * 
 * @author Baskar
 * @version 4.0.0
 */
public class LoginScreen extends BasePage {
    
    private static final int WAIT_TIMEOUT_SECONDS = 10;
    
    /**
     * Navigate to the Login screen via bottom navigation.
     */
    public void navigateToLoginScreen() {
        clickByAccessibility(WdioLocators.NAV_LOGIN);
    }
    
    /**
     * Enter email/username in the login form.
     * @param email The email address to enter
     */
    public void enterEmail(String email) {
        enterByAccessibility(WdioLocators.LOGIN_EMAIL_INPUT, email);
    }
    
    /**
     * Enter password in the login form.
     * @param password The password to enter
     */
    public void enterPassword(String password) {
        enterByAccessibility(WdioLocators.LOGIN_PASSWORD_INPUT, password);
    }
    
    /**
     * Tap the Login button.
     */
    public void tapLoginButton() {
        clickByAccessibility(WdioLocators.LOGIN_BUTTON);
    }
    
    /**
     * Perform complete login action.
     * @param email The email address
     * @param password The password
     */
    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        tapLoginButton();
    }
    
    /**
     * Verify success message is displayed after login.
     * Uses element-based waits instead of fragile pageSource checks.
     * @return true if success message is displayed
     */
    public boolean isSuccessMessageDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
            
            // Strategy 1: Try to find success alert by XPath
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.xpath(WdioLocators.XPATH_SUCCESS_ALERT)));
                logger.info("Success message found via XPath");
                return true;
            } catch (Exception e1) {
                // Strategy 2: Look for OK button on success modal
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                            AppiumBy.accessibilityId(WdioLocators.SUCCESS_MODAL_OK_BUTTON)));
                    logger.info("Success modal OK button found");
                    return true;
                } catch (Exception e2) {
                    // Strategy 3: Fallback to text content check (last resort)
                    return waitForTextInElements(WdioLocators.SUCCESS_MESSAGE, wait);
                }
            }
        } catch (Exception e) {
            logger.debug("Success message not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Verify error message is displayed.
     * Uses element-based waits for reliability.
     * @return true if any error indication is present
     */
    public boolean isErrorMessageDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
            
            // Strategy 1: Try to find error element by accessibility ID
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.accessibilityId(WdioLocators.ERROR_MESSAGE_CONTAINER)));
                logger.info("Error message container found");
                return true;
            } catch (Exception e1) {
                // Strategy 2: Look for error text via XPath
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                            AppiumBy.xpath(WdioLocators.XPATH_ERROR_ALERT)));
                    logger.info("Error alert found via XPath");
                    return true;
                } catch (Exception e2) {
                    // Strategy 3: Check text elements for error keywords
                    return waitForTextInElements("error", wait) || 
                           waitForTextInElements("invalid", wait) ||
                           waitForTextInElements("incorrect", wait);
                }
            }
        } catch (Exception e) {
            logger.debug("Error message not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Verify validation error is displayed for empty fields.
     * @return true if validation error is present
     */
    public boolean isValidationErrorDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
            
            // Look for validation error text
            return waitForTextInElements(WdioLocators.VALIDATION_ERROR_TEXT, wait) ||
                   waitForTextInElements("required", wait) ||
                   waitForTextInElements("cannot be empty", wait);
        } catch (Exception e) {
            logger.debug("Validation error not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper method to wait for specific text in any visible text elements.
     * More reliable than pageSource.contains() as it checks actual rendered elements.
     */
    private boolean waitForTextInElements(String text, WebDriverWait wait) {
        try {
            String xpath = String.format(
                "//*[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s') or " +
                "contains(translate(@label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s') or " +
                "contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s') or " +
                "contains(translate(@value,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]",
                text.toLowerCase(), text.toLowerCase(), text.toLowerCase(), text.toLowerCase()
            );
            wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath(xpath)));
            logger.debug("Found text '{}' in element", text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if Login screen is displayed.
     * @return true if on login screen
     */
    public boolean isLoginScreenDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.accessibilityId(WdioLocators.LOGIN_EMAIL_INPUT)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
