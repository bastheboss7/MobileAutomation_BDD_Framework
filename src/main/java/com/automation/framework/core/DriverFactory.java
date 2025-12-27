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
        String env = ConfigManager.getEnvironment();

        // BrowserStack Mode: Connect to BrowserStack hub with credentials and capabilities from YAML
        if (env != null && (env.toLowerCase().contains("bs") || env.toLowerCase().contains("browserstack"))) {
            logger.info("BrowserStack Mode (Android) - Connecting to hub-cloud.browserstack.com");
            String username = System.getenv("BROWSERSTACK_USERNAME");
            String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
            if (username == null || accessKey == null) {
                throw new IllegalStateException("BrowserStack credentials not found. Set BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESS_KEY environment variables.");
            }
            String bsHubUrl = "https://" + username + ":" + accessKey + "@hub-cloud.browserstack.com/wd/hub";
            logger.info("Connecting to BrowserStack hub: {}", bsHubUrl.replaceAll(":.*@", ":***@"));
            
            // Load capabilities from YAML
            UiAutomator2Options bsOptions = new UiAutomator2Options();
            loadBrowserStackCapabilities(bsOptions);
            
            AppiumDriver driver = new AndroidDriver(URI.create(bsHubUrl).toURL(), bsOptions);
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

        int systemPort = androidPortCounter.getAndIncrement();
        if (systemPort > 8299) {
            androidPortCounter.set(8200);
            systemPort = androidPortCounter.getAndIncrement();
        }
        options.setSystemPort(systemPort);
        String appPath = ConfigManager.get("androidAppPath");
        if (appPath == null) {
            appPath = ConfigManager.get("appPath");
        }
        if (appPath == null || appPath.isBlank()) {
            throw new IllegalStateException("Local Android app path is missing. Set 'androidAppPath' or 'appPath' in config.");
        }
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
        String env = ConfigManager.getEnvironment();

        // BrowserStack Mode: Connect to BrowserStack hub with credentials and SDK-managed capabilities
        if (env != null && (env.toLowerCase().contains("bs") || env.toLowerCase().contains("browserstack"))) {
            logger.info("BrowserStack Mode (iOS) - Connecting to hub-cloud.browserstack.com");
            String username = System.getenv("BROWSERSTACK_USERNAME");
            String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
            if (username == null || accessKey == null) {
                throw new IllegalStateException("BrowserStack credentials not found. Set BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESS_KEY environment variables.");
            }
            String bsHubUrl = "https://" + username + ":" + accessKey + "@hub-cloud.browserstack.com/wd/hub";
            logger.info("Connecting to BrowserStack hub: {}", bsHubUrl.replaceAll(":.*@", ":***@"));
            
            // Load capabilities from YAML
            XCUITestOptions bsOptions = new XCUITestOptions();
            loadBrowserStackCapabilities(bsOptions);
            
            AppiumDriver driver = new io.appium.java_client.ios.IOSDriver(URI.create(bsHubUrl).toURL(), bsOptions);
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

        int wdaPort = wdaPortCounter.getAndIncrement();
        if (wdaPort > 8199) {
            wdaPortCounter.set(8100);
            wdaPort = wdaPortCounter.getAndIncrement();
        }
        options.setWdaLocalPort(wdaPort);
        String appPath = ConfigManager.get("iosAppPath");
        if (appPath == null) {
            appPath = ConfigManager.get("appPath");
        }
        if (appPath == null || appPath.isBlank()) {
            throw new IllegalStateException("Local iOS app path is missing. Set 'iosAppPath' or 'appPath' in config.");
        }
        options.setApp(System.getProperty("user.dir") + "/" + appPath);

        AppiumDriver driver = new io.appium.java_client.ios.IOSDriver(URI.create(hubUrl).toURL(), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getInt("implicitWait", 30)));
        return driver;
    }

    /**
     * Load BrowserStack capabilities from YAML configuration.
     * Applies app, source, debug, and other settings to the options.
     */
    private static void loadBrowserStackCapabilities(BaseOptions<?> options) {
        // Load app reference from YAML
        String app = ConfigManager.get("app");
        if (app != null && !app.isBlank()) {
            logger.debug("Setting app from YAML: {}", app);
            options.setCapability("app", app);
        }
        
        // Load source agent (if configured)
        String source = ConfigManager.get("source");
        if (source != null && !source.isBlank()) {
            logger.debug("Setting source agent: {}", source);
            options.setCapability("source", source);
        }
        
        // Load debug flag
        Boolean debug = ConfigManager.getBoolean("debug", false);
        if (debug) {
            logger.debug("Enabling debug logging");
            options.setCapability("debug", true);
        }
        
        // Load network and device logs if configured
        if (ConfigManager.getBoolean("networkLogs", false)) {
            options.setCapability("networkLogs", true);
        }
        if (ConfigManager.getBoolean("deviceLogs", false)) {
            options.setCapability("deviceLogs", true);
        }
        if (ConfigManager.getBoolean("appiumLogs", false)) {
            options.setCapability("appiumLogs", true);
        }
        
        // Load console logs setting
        String consoleLogs = ConfigManager.get("consoleLogs");
        if (consoleLogs != null && !consoleLogs.isBlank()) {
            options.setCapability("consoleLogs", consoleLogs);
        }
        
        // Load browserstack.local if configured
        if (ConfigManager.getBoolean("browserstackLocal", false)) {
            options.setCapability("browserstackLocal", true);
        }
        
        logger.info("BrowserStack capabilities loaded from YAML");
    }}