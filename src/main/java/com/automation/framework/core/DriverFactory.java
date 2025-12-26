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

        // BrowserStack Mode: Upload app and add device capabilities
        if (isBrowserStack) {
            String appPath = ConfigManager.get("androidAppPath");
            if (appPath == null) appPath = ConfigManager.get("appPath");
            // Fallback to raw YAML values if properties were not exported
            if (appPath == null) {
                Object raw = ConfigManager.getRawValue("androidAppPath");
                if (raw == null) raw = ConfigManager.getRawValue("appPath");
                if (raw == null) raw = ConfigManager.getRawValue("app");
                if (raw != null) appPath = raw.toString();
            }
            if (appPath == null || appPath.isBlank()) {
                throw new RuntimeException("App upload failed: App path missing in config.");
            }

            try {
                Map<String, String> credentials = BrowserStackAppUploader.getBrowserStackCredentials();

                String bsAppUrl = BrowserStackAppUploader.uploadApp(appPath, credentials.get("username"),
                        credentials.get("accessKey"));

                // Use the uploaded bs:// URL to avoid SDK override issues
                options.setApp(bsAppUrl);

                // Also set system property for SDK if active
                if (isBSDK) {
                    System.setProperty("browserstack.app", bsAppUrl);
                }
                logger.info("BrowserStack app URL set: {}", bsAppUrl);
            } catch (IOException e) {
                logger.error("Failed to upload app to BrowserStack", e);
                throw new RuntimeException("App upload failed: " + e.getMessage());
            }

            // Get device info from device pool (required for BrowserStack capabilities)
            String deviceName = DevicePool.getDeviceName();
            String platformVersion = DevicePool.getPlatformVersion();
            
            if (deviceName == null || deviceName.isEmpty()) {
                throw new RuntimeException("deviceName is required for BrowserStack execution. Check device pool configuration.");
            }
            if (platformVersion == null || platformVersion.isEmpty()) {
                logger.warn("platformVersion is empty, using 'default'");
                platformVersion = "default";
            }

            // Add BrowserStack-specific capabilities (deviceName, osVersion, etc.)
            BrowserStackCapabilityBuilder.addBrowserStackCapabilities(options, deviceName, platformVersion);
            
            // Log execution context
            BrowserStackCapabilityBuilder.logBrowserStackExecution(deviceName, platformVersion, hubUrl);

            // Always use authorized hub URL for reliability
            String authorizedHubUrl = BrowserStackCapabilityBuilder.getAuthorizedHubUrl(hubUrl);
            logger.info("Creating AndroidDriver on BrowserStack with authorized URL and options: {}", options.asMap());
            AppiumDriver driver = new AndroidDriver(URI.create(authorizedHubUrl).toURL(), options);
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

        // 1. SDK Mode: Minimum viable capabilities, let SDK handle the rest
        if (isBSDK && isBrowserStack) {
            String appPath = ConfigManager.get("iosAppPath");
            if (appPath == null)
                appPath = ConfigManager.get("appPath");
            // SDK Mode: Let SDK manage everything via system properties set in
            // ConfigManager
            logger.info("SDK Mode (iOS) - Delegating to SDK with system properties");
            AppiumDriver driver = new io.appium.java_client.ios.IOSDriver(URI.create(hubUrl).toURL(),
                    new BaseOptions<>());
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getInt("implicitWait", 30)));
            return driver;
        }

        // 2. Standard Mode: Full framework control
        String deviceName = DevicePool.getIOSDeviceName();
        String platformVersion = DevicePool.getIOSPlatformVersion();
        options.setDeviceName(deviceName);
        options.setPlatformVersion(platformVersion);
        options.setAutomationName("XCUITest");

        options.setNoReset(ConfigManager.getBoolean("noReset", false));
        options.setUdid(ConfigManager.get("iosUdid"));
        options.setNewCommandTimeout(Duration.ofSeconds(ConfigManager.getInt("newCommandTimeout", 6000)));

        if (isBrowserStack) {
            BrowserStackCapabilityBuilder.addBrowserStackCapabilities(options, deviceName, "default");
            String appPath = ConfigManager.get("iosAppPath");
            if (appPath == null)
                appPath = ConfigManager.get("appPath");
            try {
                Map<String, String> credentials = BrowserStackAppUploader.getBrowserStackCredentials();
                options.setApp(BrowserStackAppUploader.uploadApp(appPath, credentials.get("username"),
                        credentials.get("accessKey")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            options.setWdaLocalPort(wdaPortCounter.getAndIncrement());
            String appPath = ConfigManager.get("iosAppPath");
            if (appPath == null)
                appPath = ConfigManager.get("appPath");
            options.setApp(System.getProperty("user.dir") + "/" + appPath);
        }

        String authorizedHubUrl = BrowserStackCapabilityBuilder.getAuthorizedHubUrl(hubUrl);
        AppiumDriver driver = new io.appium.java_client.ios.IOSDriver(URI.create(authorizedHubUrl).toURL(), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getInt("implicitWait", 30)));
        return driver;
    }
}
