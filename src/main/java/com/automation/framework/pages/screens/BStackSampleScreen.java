package com.automation.framework.pages.screens;

import com.automation.framework.pages.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.NoSuchElementException;
import com.google.common.collect.ImmutableMap;

/**
 * Page Object for BrowserStack Sample App (iOS).
 *
 * @author Baskar
 * @version 1.0.0
 */
public class BStackSampleScreen extends BasePage {

    // iOS Accessibility IDs
    private static final String TEXT_BUTTON_ACC_ID = "Text Button";
    private static final String ALERT_BUTTON_ACC_ID = "Alert";
    private static final String TEXT_INPUT_ACC_ID = "Text Input";
    private static final String TEXT_OUTPUT_ACC_ID = "Text Output";

    /**
     * Click the Text Button to navigate to the text input screen.
     */
    public void clickTextButton() {
        System.out.println("Attempting to click navigation button using mobile: tap workaround...");

        for (int i = 0; i < 3; i++) {
            try {
                WebElement target;
                try {
                    target = safeFindElement("Text");
                } catch (Exception e) {
                    target = safeFindElement(TEXT_BUTTON_ACC_ID);
                }

                // Use mobile: tap for better reliability on iOS
                java.util.Map<String, Object> params = new java.util.HashMap<>();
                params.put("elementId", ((RemoteWebElement) target).getId());
                params.put("x", 0);
                params.put("y", 0);
                getDriver().executeScript("mobile: tap", params);

                System.out.println("Performed mobile: tap on navigation element. Waiting for transition...");
                Thread.sleep(3000);

                try {
                    safeFindElement(TEXT_INPUT_ACC_ID);
                    logger.info("Navigation successful to Text screen");
                    return;
                } catch (Exception e) {
                    System.out.println("Still on home screen, retrying with forceful click... (" + (i + 1) + "/3)");
                }
            } catch (Exception e) {
                System.err.println("Tap attempt failed: " + e.getMessage());
            }
        }

        throw new RuntimeException("Failed to navigate to Text screen after multiple mobile: tap attempts");
    }

    /**
     * Click the Alert Button.
     */
    public void clickAlertButton() {
        System.out.println("Attempting to click Alert button using multiple methods...");

        for (int i = 0; i < 3; i++) {
            try {
                WebElement target = safeFindElement(ALERT_BUTTON_ACC_ID);

                // Method 1: Standard Click
                try {
                    target.click();
                    System.out.println("Performed standard click on Alert button. Waiting...");
                    Thread.sleep(2000);
                    getDriver().switchTo().alert();
                    logger.info("Alert displayed successfully via standard click");
                    return;
                } catch (Exception clickEx) {
                    System.out.println("Standard click didn't trigger alert, trying mobile: tap...");
                }

                // Method 2: mobile: tap with center offset
                java.util.Map<String, Object> params = new java.util.HashMap<>();
                params.put("elementId", ((RemoteWebElement) target).getId());
                params.put("x", 10); // Minimal offset from top-left
                params.put("y", 10);
                getDriver().executeScript("mobile: tap", params);

                System.out.println("Performed mobile: tap on Alert button. Waiting...");
                Thread.sleep(2000);

                try {
                    getDriver().switchTo().alert();
                    logger.info("Alert displayed successfully via mobile: tap");
                    return;
                } catch (Exception e) {
                    System.out.println("Alert not yet displayed, retrying... (" + (i + 1) + "/3)");
                }
            } catch (Exception e) {
                System.err.println("Alert interaction attempt failed: " + e.getMessage());
            }
        }

        throw new RuntimeException("Failed to trigger alert after multiple interaction attempts");
    }

    /**
     * Get the text message from the native alert.
     * 
     * @return Alert message or null
     */
    public String getAlertText() {
        try {
            return getDriver().switchTo().alert().getText();
        } catch (Exception e) {
            logger.warn("Could not get alert text: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Accept (click OK) on the native alert.
     */
    public void acceptAlert() {
        try {
            getDriver().switchTo().alert().accept();
            logger.info("Accepted alert");
        } catch (Exception e) {
            logger.error("Failed to accept alert", e);
        }
    }

    /**
     * Get the text displayed in the output field.
     * 
     * @return Displayed text
     */
    public String getDisplayedText() {
        WebElement element = safeFindElement(TEXT_OUTPUT_ACC_ID);
        String text = element.getText();
        logger.info("Retrieved displayed text: {}", text);
        return text;
    }

    // WORKAROUND: Bypass Selenium's internal casting by invoking execute directly
    // and handling the Map response
    private WebElement safeFindElement(String accessibilityId) {
        try {
            // Reflection to call 'execute' method on RemoteWebDriver
            java.lang.reflect.Method executeMethod = RemoteWebDriver.class
                    .getDeclaredMethod("execute", String.class, java.util.Map.class);
            executeMethod.setAccessible(true);

            java.util.Map<String, Object> params = ImmutableMap.of(
                    "using", "accessibility id",
                    "value", accessibilityId);

            org.openqa.selenium.remote.Response response = (org.openqa.selenium.remote.Response) executeMethod
                    .invoke(getDriver(), org.openqa.selenium.remote.DriverCommand.FIND_ELEMENT, params);

            Object value = response.getValue();

            // Check if it's already a WebElement (reflection-based execute might
            // successfully deserialize it)
            if (value instanceof WebElement) {
                return (WebElement) value;
            }

            if (value instanceof java.util.Map) {
                java.util.Map<?, ?> map = (java.util.Map<?, ?>) value;

                // CHECK FOR ERROR RESPONSE
                if (map.containsKey("error") || map.containsKey("message")) {
                    String error = map.get("error") != null ? map.get("error").toString() : "unknown error";
                    String message = map.get("message") != null ? map.get("message").toString() : "";

                    if (error.equals("no such element") || error.equals("invalid selector")) {
                        // Log page source to help debug
                        System.out.println("Element not found: " + accessibilityId + ". Capturing page source...");
                        try {
                            System.out.println("CURRENT PAGE SOURCE:\n" + getDriver().getPageSource());
                        } catch (Exception sourceEx) {
                        }
                    }

                    throw new NoSuchElementException(
                            "Element not found: " + accessibilityId + ". Server returned: " + error + " - " + message);
                }

                // Standard W3C Element Key
                String w3cKey = "element-6066-11e4-a52e-4f735466cecf";
                // Legacy JSONWire Element Key
                String legacyKey = "ELEMENT";

                String elementId = null;
                if (map.containsKey(w3cKey)) {
                    elementId = (String) map.get(w3cKey);
                } else if (map.containsKey(legacyKey)) {
                    elementId = (String) map.get(legacyKey);
                }

                if (elementId != null && !elementId.isEmpty()) {
                    RemoteWebElement manualElement = new RemoteWebElement();
                    manualElement.setParent((RemoteWebDriver) getDriver());
                    manualElement.setId(elementId);
                    return manualElement;
                }
            }

            throw new NoSuchElementException("Could not find element: " + accessibilityId + ". Response: " + value);

        } catch (NoSuchElementException nse) {
            throw nse;
        } catch (Exception ex) {
            throw new RuntimeException("Failed manual workaround for finding element: " + accessibilityId, ex);
        }
    }
}
