package pages.screens;

import com.automation.framework.pages.BasePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.locators.WdioLocators;

import java.time.Duration;

/**
 * Page Object for WDIO Demo App Login Screen.
 * Contains all actions and verifications for the login functionality.
 * Follows Page Object Model pattern with locators externalized.
 * 
 * @author Baskar
 * @version 3.0.0
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
     * Uses explicit wait instead of Thread.sleep.
     * @return true if success message is displayed
     */
    public boolean isSuccessMessageDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
            // Wait for modal/alert to appear with success text
            wait.until(driver -> {
                String pageSource = driver.getPageSource();
                return pageSource.contains(WdioLocators.SUCCESS_MESSAGE) || 
                       pageSource.contains("logged in");
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verify error message is displayed.
     * @return true if any error indication is present
     */
    public boolean isErrorMessageDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
            wait.until(driver -> {
                String pageSource = driver.getPageSource();
                return pageSource.contains("error") || 
                       pageSource.contains("invalid") || 
                       pageSource.contains("incorrect");
            });
            return true;
        } catch (Exception e) {
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
            wait.until(driver -> {
                String pageSource = driver.getPageSource();
                return pageSource.contains("Please enter") || 
                       pageSource.contains("required");
            });
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
