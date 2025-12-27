package com.automation.framework.core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;

import static com.automation.framework.core.FrameworkConstants.*;

/**
 * Factory class for creating platform-specific Appium drivers for BrowserStack.
 * Supports Android and iOS automation exclusively on BrowserStack cloud.
 * 
 * <p>Architecture:
 * - Uses BrowserStack SDK-managed capabilities from YAML files
 * - Automatic device allocation via BrowserStack platform list
 * - Thread-safe driver management via DriverManager
 * 
 * @author Baskar
 * @version 5.0.0 - BrowserStack-only, removed local execution support
 */
public class DriverFactory {
    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);

    public enum Platform {
        ANDROID,
        IOS
    }

    private DriverFactory() {
        // Private constructor
    }

    /**
     * Create a driver based on the current platform configuration.
     * 
     * @return AppiumDriver instance
     * @throws RuntimeException if driver creation fails
     */
    public static AppiumDriver createDriver() {
        String platformStr = ConfigManager.getPlatform().toUpperCase();
        Platform platform = platformStr.equals("IOS") ? Platform.IOS : Platform.ANDROID;
        logger.info("Creating driver for platform: {}", platform);

        try {
            AppiumDriver driver = (platform == Platform.IOS)
                ? createIOSDriver()
                : createAndroidDriver();
            DriverManager.setDriver(driver);
            return driver;
        } catch (Exception e) {
            logger.error("Failed to create driver for platform: {}", platform, e);
            throw new RuntimeException("Driver creation failed", e);
        }
    }
    
    /**
     * Get BrowserStack hub URL with embedded credentials.
     * 
     * @return BrowserStack hub URL
     * @throws IllegalStateException if credentials are not set
     */
    private static String getBrowserStackHubUrl() {
        // Prefer env vars; fall back to YAML (userName/accessKey) via ConfigManager
        String username = System.getenv(BROWSERSTACK_USERNAME_ENV);
        String accessKey = System.getenv(BROWSERSTACK_ACCESS_KEY_ENV);

        if (username == null || username.isEmpty()) {
            username = ConfigManager.get("userName");
        }
        if (accessKey == null || accessKey.isEmpty()) {
            accessKey = ConfigManager.get("accessKey");
        }

        if (username == null || accessKey == null || username.isEmpty() || accessKey.isEmpty()) {
            throw new IllegalStateException(
                String.format("BrowserStack credentials not found. Provide %s/%s env vars or userName/accessKey in YAML.",
                    BROWSERSTACK_USERNAME_ENV, BROWSERSTACK_ACCESS_KEY_ENV));
        }

        String hubUrl = "https://" + username + ":" + accessKey + "@" + BROWSERSTACK_HUB_URL.replace("https://", "");
        logger.info("Connecting to BrowserStack hub: {}", hubUrl.replaceAll(":.*@", ":***@"));
        return hubUrl;
    }
    
    /**
     * Configure implicit wait timeout on driver.
     * 
     * @param driver AppiumDriver instance
     */
    private static void configureImplicitWait(AppiumDriver driver) {
        int implicitWait = ConfigManager.getInt(CONFIG_KEY_IMPLICIT_WAIT, DEFAULT_IMPLICIT_WAIT);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
    }

    /**
     * Create Android driver for BrowserStack.
     * 
     * @return AndroidDriver instance
     * @throws MalformedURLException if hub URL is invalid
     */
    private static AppiumDriver createAndroidDriver() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();

        logger.info("Creating Android driver for BrowserStack cloud");
        
        AppiumDriver driver = new AndroidDriver(URI.create(getBrowserStackHubUrl()).toURL(), options);
        configureImplicitWait(driver);
        return driver;
    }
    
    /**
     * Create iOS driver for BrowserStack.
     * 
     * @return IOSDriver instance
     * @throws MalformedURLException if hub URL is invalid
     */
    private static AppiumDriver createIOSDriver() throws MalformedURLException {
        XCUITestOptions options = new XCUITestOptions();

        logger.info("Creating iOS driver for BrowserStack cloud");
        
        AppiumDriver driver = new IOSDriver(URI.create(getBrowserStackHubUrl()).toURL(), options);
        configureImplicitWait(driver);
        return driver;
    }

    // Capabilities are injected by the BrowserStack SDK from YAML.
    // Options remain minimal here; SDK applies app, logging, and other settings.
}