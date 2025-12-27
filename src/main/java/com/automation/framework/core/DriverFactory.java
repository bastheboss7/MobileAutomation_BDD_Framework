package com.automation.framework.core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.remote.options.BaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Factory class for creating platform-specific Appium drivers.
 * Supports Android, iOS, and Web automation.
 * 
 * @author Baskar
 * @version 3.0.0
 */
public class DriverFactory {
    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);

    // Atomic counters for thread-safe port allocation
    private static final AtomicInteger androidPortCounter = new AtomicInteger(8200);
    private static final AtomicInteger wdaPortCounter = new AtomicInteger(8100);

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
     */
    public static AppiumDriver createDriver() {
        String platformStr = ConfigManager.getPlatform().toUpperCase();
        Platform platform = platformStr.equals("IOS") ? Platform.IOS : Platform.ANDROID;

        return createDriver(platform);
    }

    /**
     * Create a driver for a specific platform.
     * 
     * @param platform Target platform
     * @return AppiumDriver instance
     */
    public static AppiumDriver createDriver(Platform platform) {
        logger.info("Creating driver for platform: {}", platform);

        try {
            AppiumDriver driver = switch (platform) {
                case ANDROID -> createAndroidDriver();
                case IOS -> createIOSDriver();
            };

            DriverManager.setDriver(driver);
            return driver;

        } catch (Exception e) {
            logger.error("Failed to create driver for platform: {}", platform, e);
            throw new RuntimeException("Driver creation failed", e);
        }
    }

    /**
     * Create Android driver with UiAutomator2.
     */
    private static AppiumDriver createAndroidDriver() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();

        String hubUrl = ConfigManager.get("hubUrl", ConfigManager.get("HUB", "http://127.0.0.1:4723"));
        boolean isBrowserStack = BrowserStackCapabilityBuilder.isBrowserStackHub(hubUrl);
        boolean isBSDK = System.getProperty("browserstack.sdk") != null || System.getenv("BROWSERSTACK_SDK") != null
                || new java.io.File("browserstack.yml").exists();

        // SDK-only: Delegate BrowserStack execution to SDK agent
        if (isBrowserStack) {
            if (!isBSDK) {
                throw new IllegalStateException("BrowserStack SDK agent is not active. Enable Maven profile 'browserstack' or set browserstack.sdk=true.");
            }
            logger.info("SDK Mode (Android) - Delegating to SDK with BaseOptions");
            AppiumDriver driver = new AndroidDriver(URI.create(hubUrl).toURL(), new BaseOptions<>());
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getInt("implicitWait", 30)));
            return driver;
        }

        // Local Mode: Full framework control (Local execution)
        String deviceName = DevicePool.getDeviceName();
        String platformVersion = DevicePool.getPlatformVersion();
        options.setDeviceName(deviceName);
        options.setPlatformVersion(platformVersion);
        options.setAutomationName("UiAutomator2");

        options.setNoReset(ConfigManager.getBoolean("noReset", false));
        options.setNewCommandTimeout(Duration.ofSeconds(ConfigManager.getInt("newCommandTimeout", 6000)));

        options.setSystemPort(androidPortCounter.getAndIncrement());
        String appPath = ConfigManager.get("androidAppPath");
        if (appPath == null)
            appPath = ConfigManager.get("appPath");
        options.setApp(System.getProperty("user.dir") + "/" + appPath);

        logger.info("Creating AndroidDriver (Local) with options: {}", options.asMap());
        AppiumDriver driver = new AndroidDriver(URI.create(hubUrl).toURL(), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getInt("implicitWait", 30)));
        return driver;
    }

    /**
     * Create iOS driver with XCUITest.
     */
    private static AppiumDriver createIOSDriver() throws MalformedURLException {
        XCUITestOptions options = new XCUITestOptions();

        String hubUrl = ConfigManager.get("hubUrl", ConfigManager.get("HUB", "http://127.0.0.1:4723"));
        boolean isBrowserStack = BrowserStackCapabilityBuilder.isBrowserStackHub(hubUrl);
        boolean isBSDK = System.getProperty("browserstack.sdk") != null || System.getenv("BROWSERSTACK_SDK") != null
                || new java.io.File("browserstack.yml").exists();

        // SDK-only for BrowserStack
        if (isBrowserStack) {
            if (!isBSDK) {
                throw new IllegalStateException("BrowserStack SDK agent is not active. Enable Maven profile 'browserstack' or set browserstack.sdk=true.");
            }
            logger.info("SDK Mode (iOS) - Delegating to SDK with BaseOptions");
            AppiumDriver driver = new io.appium.java_client.ios.IOSDriver(URI.create(hubUrl).toURL(), new BaseOptions<>());
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getInt("implicitWait", 30)));
            return driver;
        }

        // Local Mode (non-BrowserStack)
        String deviceName = DevicePool.getIOSDeviceName();
        String platformVersion = DevicePool.getIOSPlatformVersion();
        options.setDeviceName(deviceName);
        options.setPlatformVersion(platformVersion);
        options.setAutomationName("XCUITest");

        options.setNoReset(ConfigManager.getBoolean("noReset", false));
        options.setUdid(ConfigManager.get("iosUdid"));
        options.setNewCommandTimeout(Duration.ofSeconds(ConfigManager.getInt("newCommandTimeout", 6000)));

        options.setWdaLocalPort(wdaPortCounter.getAndIncrement());
        String appPath = ConfigManager.get("iosAppPath");
        if (appPath == null)
            appPath = ConfigManager.get("appPath");
        options.setApp(System.getProperty("user.dir") + "/" + appPath);

        AppiumDriver driver = new io.appium.java_client.ios.IOSDriver(URI.create(hubUrl).toURL(), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getInt("implicitWait", 30)));
        return driver;
    }
}
