package com.automation.framework.core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
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
 * @version 2.1.0
 */
public class DriverFactory {
    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);

    // Atomic counters for thread-safe port allocation (avoids collision in parallel
    // execution)
    private static final AtomicInteger androidPortCounter = new AtomicInteger(8200);
    private static final int ANDROID_MAX_PORT = 8299;

    private static final AtomicInteger wdaPortCounter = new AtomicInteger(8100);
    private static final int WDA_MAX_PORT = 8199;

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

        // Device configuration - use DevicePool for parallel execution
        String deviceName = DevicePool.getDeviceName();
        String platformVersion = DevicePool.getPlatformVersion();

        options.setDeviceName(deviceName);
        options.setPlatformVersion(platformVersion);
        options.setAutomationName("UiAutomator2");
        options.setNoReset(ConfigManager.getBoolean("noReset", false));
        options.setNewCommandTimeout(Duration.ofSeconds(
                ConfigManager.getInt("newCommandTimeout", 6000)));

        logger.info("Using device from pool: {} (OS: {})", deviceName, platformVersion);

        // Get hub URL to determine execution environment
        String hubUrl = ConfigManager.get("HUB", "http://127.0.0.1:4723");

        // BrowserStack-specific capabilities (only when using BrowserStack hub)
        if (BrowserStackCapabilityBuilder.isBrowserStackHub(hubUrl)) {
            BrowserStackCapabilityBuilder.logBrowserStackExecution(deviceName, platformVersion, hubUrl);
            BrowserStackCapabilityBuilder.addBrowserStackCapabilities(options, deviceName, platformVersion);
        } else {
            // Local execution - configure system port for parallel execution
            int dynamicPort = androidPortCounter.getAndIncrement();
            if (dynamicPort > ANDROID_MAX_PORT) {
                androidPortCounter.set(8200);
                dynamicPort = androidPortCounter.getAndIncrement();
            }
            options.setSystemPort(dynamicPort);
            logger.info("Allocated Android systemPort: {} (thread-safe)", dynamicPort);
        }

        // Native app configuration
        String appPackage = ConfigManager.get("appPackage");
        String appActivity = ConfigManager.get("appActivity");

        if (appPackage != null && appActivity != null) {
            options.setAppPackage(appPackage);
            options.setAppActivity(appActivity);
        }

        // App installation
        String apkPath = ConfigManager.get("apkPath");
        if (apkPath != null && !apkPath.isEmpty()) {
            if (!apkPath.startsWith("/")) {
                apkPath = System.getProperty("user.dir") + "/" + apkPath;
            }

            if (BrowserStackCapabilityBuilder.isBrowserStackHub(hubUrl)) {
                // BrowserStack execution - upload app via REST API and get bs:// URL
                try {
                    Map<String, String> credentials = BrowserStackAppUploader.getBrowserStackCredentials();
                    String bsAppUrl = BrowserStackAppUploader.uploadApp(
                            apkPath,
                            credentials.get("username"),
                            credentials.get("accessKey"));
                    options.setApp(bsAppUrl);
                    logger.info("BrowserStack execution - app uploaded: {}", bsAppUrl);
                } catch (IOException e) {
                    logger.error("Failed to upload app to BrowserStack", e);
                    throw new RuntimeException("BrowserStack app upload failed", e);
                }
            } else {
                // Local execution - use local app path
                options.setApp(apkPath);
                logger.info("Local execution - installing app: {}", apkPath);
            }
        }

        logger.info("Configured for Android native app: {}", appPackage);
        logger.info("Connecting to Appium server: {}", hubUrl);

        AppiumDriver driver = new AndroidDriver(URI.create(hubUrl).toURL(), options);

        // Set implicit wait
        int implicitWait = ConfigManager.getInt("implicitWait", 30);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

        return driver;
    }

    /**
     * Create iOS driver with XCUITest.
     */
    private static AppiumDriver createIOSDriver() throws MalformedURLException {
        XCUITestOptions options = new XCUITestOptions();

        // Device configuration from pool or default
        String deviceName = DevicePool.getIOSDeviceName();
        String platformVersion = DevicePool.getIOSPlatformVersion();

        logger.info("Using iOS device from pool: {} (OS: {})", deviceName, platformVersion);

        options.setDeviceName(deviceName);
        options.setUdid(ConfigManager.get("iosUdid"));
        options.setPlatformVersion(platformVersion);
        options.setAutomationName("XCUITest");
        options.setNoReset(ConfigManager.getBoolean("noReset", false));
        options.setNewCommandTimeout(Duration.ofSeconds(
                ConfigManager.getInt("newCommandTimeout", 6000)));

        logger.info("Using iOS device: {} (OS: {})", deviceName, platformVersion);

        // Get hub URL to determine execution environment
        String hubUrl = ConfigManager.get("HUB", "http://127.0.0.1:4723");

        // BrowserStack-specific capabilities (only when using BrowserStack hub)
        if (BrowserStackCapabilityBuilder.isBrowserStackHub(hubUrl)) {
            BrowserStackCapabilityBuilder.logBrowserStackExecution(deviceName, platformVersion, hubUrl);
            BrowserStackCapabilityBuilder.addBrowserStackCapabilities(options, deviceName, platformVersion);
        } else {
            // Local execution - configure WDA port for parallel execution
            int wdaPort = wdaPortCounter.getAndIncrement();
            if (wdaPort > WDA_MAX_PORT) {
                wdaPortCounter.set(8100);
                wdaPort = wdaPortCounter.getAndIncrement();
            }
            options.setWdaLocalPort(wdaPort);
            logger.info("Allocated iOS wdaLocalPort: {} (thread-safe)", wdaPort);
        }

        // Native app configuration
        String bundleId = ConfigManager.get("bundleId");
        if (bundleId != null && !bundleId.isEmpty()) {
            options.setBundleId(bundleId);
        }

        // App installation
        String appPath = ConfigManager.get("iosAppPath");
        if (appPath != null && !appPath.isEmpty()) {
            if (!appPath.startsWith("/")) {
                appPath = System.getProperty("user.dir") + "/" + appPath;
            }

            if (BrowserStackCapabilityBuilder.isBrowserStackHub(hubUrl)) {
                // BrowserStack execution - upload app via REST API and get bs:// URL
                try {
                    Map<String, String> credentials = BrowserStackAppUploader.getBrowserStackCredentials();
                    String bsAppUrl = BrowserStackAppUploader.uploadApp(
                            appPath,
                            credentials.get("username"),
                            credentials.get("accessKey"));
                    options.setApp(bsAppUrl);
                    logger.info("BrowserStack execution - app uploaded: {}", bsAppUrl);
                } catch (IOException e) {
                    logger.error("Failed to upload app to BrowserStack", e);
                    throw new RuntimeException("BrowserStack app upload failed", e);
                }
            } else {
                // Local execution - use local app path
                options.setApp(appPath);
                logger.info("Local execution - installing app: {}", appPath);
            }
        }

        logger.info("Configured for iOS native app: {}", bundleId);
        logger.info("Connecting to Appium server: {}", hubUrl);

        AppiumDriver driver = new IOSDriver(URI.create(hubUrl).toURL(), options);

        // Set implicit wait
        int implicitWait = ConfigManager.getInt("implicitWait", 30);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

        return driver;
    }
}
