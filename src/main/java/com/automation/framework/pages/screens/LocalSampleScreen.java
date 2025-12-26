package com.automation.framework.pages.screens;

import com.automation.framework.pages.BasePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;
import java.util.List;

/**
 * Page Object for Local Sample App (Basic Networking).
 *
 * @author Baskar
 * @version 1.0.0
 */
public class LocalSampleScreen extends BasePage {

    // Android Locators
    private static final String ANDROID_TEST_ACTION_ID = "com.example.android.basicnetworking:id/test_action";
    private static final String ANDROID_TEXT_VIEW_CLASS = "android.widget.TextView";

    // iOS Locators
    private static final String IOS_TEST_ACTION_ACC_ID = "Test Action";
    private static final String IOS_TEXT_VIEW_CLASS = "XCUIElementTypeStaticText";

    /**
     * Tap on the Test Action button to trigger network check.
     */
    public void tapTestAction() {
        if (isAndroid()) {
            clickById(ANDROID_TEST_ACTION_ID);
        } else {
            // Workaround for ClassCastException with ExpectedConditions on iOS
            // Directly using custom wait to find element
            org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(
                    getDriver(), java.time.Duration.ofSeconds(10));
            WebElement element = wait.until(d -> d.findElement(AppiumBy.accessibilityId(IOS_TEST_ACTION_ACC_ID)));
            element.click();
        }
        logger.info("Tapped on Test Action button");
    }

    /**
     * Get the connection status text.
     * Iterates through all text views to find the status message.
     * 
     * @return Connection status string or null if not found
     */
    public String getConnectionStatus() {
        // Wait for potential network delay/UI update
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String locatorClass = isAndroid() ? ANDROID_TEXT_VIEW_CLASS : IOS_TEXT_VIEW_CLASS;
        List<WebElement> allTextViewElements = getDriver().findElements(AppiumBy.className(locatorClass));

        for (WebElement textElement : allTextViewElements) {
            String text = textElement.getText();
            if (text.contains("The active connection is")) {
                logger.info("Found connection status: {}", text);
                return text;
            }
        }

        logger.warn("Connection status text not found");
        return null;
    }

    private boolean isAndroid() {
        return com.automation.framework.core.ConfigManager.getPlatform().equalsIgnoreCase("android");
    }
}
