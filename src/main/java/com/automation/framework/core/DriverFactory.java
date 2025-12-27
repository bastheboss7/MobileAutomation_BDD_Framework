package com.automation.framework.core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.remote.options.BaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static com.automation.framework.core.FrameworkConstants.*;

/**
 * Factory class for creating platform-specific Appium drivers.
 * Supports Android and iOS automation with BrowserStack cloud or local execution.
 * 
 * <p>Architecture:
 * - BrowserStack Mode: Uses SDK-managed capabilities from YAML
 * - Local Mode: Uses device pool allocation and local app paths
 * 
 * @author Baskar
 * @version 4.0.0 - Refactored to use constants and reduce duplication
 */
public class DriverFactory {
    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);

    // Atomic counters for thread-safe port allocation
    private static final AtomicInteger androidPortCounter = new AtomicInteger(ANDROID_PORT_START);
    private static final AtomicInteger wdaPortCounter = new AtomicInteger(IOS_WDA_PORT_START);

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

        return createDriver(platform);
    }

    /**
     * Create a driver for a specific platform.
     * 
     * @param platform Target platform (ANDROID or IOS)
     * @return AppiumDriver instance
     * @throws RuntimeException if driver creation fails
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
     * Check if the environment is BrowserStack.
     * 
     * @return true if BrowserStack environment, false otherwise
     */
    private static boolean isBrowserStackEnvironment() {
        String env = ConfigManager.getEnvironment();
        return env != null && (env.toLowerCase().contains(ENV_BS_SHORT) || 
                                env.toLowerCase().contains(ENV_BROWSERSTACK));
    }
    
    /**
     * Get BrowserStack hub URL with embedded credentials.
     * 
     * @return BrowserStack hub URL
     * @throws IllegalStateException if credentials are not set
     */
    private static String getBrowserStackHubUrl() {
        String username = System.getenv(BROWSERSTACK_USERNAME_ENV);
        String accessKey = System.getenv(BROWSERSTACK_ACCESS_KEY_ENV);
        
        if (username == null || accessKey == null) {
            throw new IllegalStateException(
                String.format("BrowserStack credentials not found. Set %s and %s environment variables.",
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
     * Create Android driver with UiAutomator2.
     * 
     * @return AndroidDriver instance
     * @throws MalformedURLException if hub URL is invalid
     */
    private static AppiumDriver createAndroidDriver() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();

        String hubUrl = ConfigManager.get(CONFIG_KEY_HUB_URL, 
                                         ConfigManager.get(CONFIG_KEY_HUB_LEGACY, DEFAULT_HUB_URL));

        // BrowserStack Mode: Use SDK-managed capabilities from YAML
        if (isBrowserStackEnvironment()) {
            logger.info("BrowserStack Mode (Android) - Using cloud execution");
            loadBrowserStackCapabilities(options);
            
            AppiumDriver driver = new AndroidDriver(URI.create(getBrowserStackHubUrl()).toURL(), options);
            configureImplicitWait(driver);
            return driver;
        }

        // Local Mode: Use device pool allocation
        logger.info("Local Mode (Android) - Using local execution");
        configureLocalAndroidOptions(options);
        
        AppiumDriver driver = new AndroidDriver(URI.create(hubUrl).toURL(), options);
        configureImplicitWait(driver);
        return driver;
    }
    
    /**
     * Configure Android options for local execution.
     * 
     * @param options UiAutomator2Options to configure
     */
    private static void configureLocalAndroidOptions(UiAutomator2Options options) {
        // Device configuration from pool
        options.setDeviceName(DevicePool.getDeviceName());
        options.setPlatformVersion(DevicePool.getPlatformVersion());
        options.setAutomationName(AUTOMATION_UIAUTOMATOR2);
        
        // Reset and timeout settings
        options.setNoReset(ConfigManager.getBoolean(CONFIG_KEY_NO_RESET, false));
        options.setNewCommandTimeout(Duration.ofSeconds(
            ConfigManager.getInt(CONFIG_KEY_NEW_COMMAND_TIMEOUT, DEFAULT_COMMAND_TIMEOUT)));
        
        // Thread-safe port allocation
        int systemPort = androidPortCounter.getAndIncrement();
        if (systemPort > ANDROID_PORT_END) {
            androidPortCounter.set(ANDROID_PORT_START);
            systemPort = androidPortCounter.getAndIncrement();
        }
        options.setSystemPort(systemPort);
        
        // App path resolution
        String appPath = ConfigManager.get(CONFIG_KEY_ANDROID_APP_PATH);
        if (appPath == null) {
            appPath = ConfigManager.get(CONFIG_KEY_APP_PATH);
        }
        if (appPath == null || appPath.isBlank()) {
            throw new IllegalStateException(
                "Local Android app path is missing. Set 'androidAppPath' or 'appPath' in config.");
        }
        options.setApp(System.getProperty("user.dir") + "/" + appPath);
        
        logger.info("Local Android options configured: device={}, port={}", 
                    options.getDeviceName().orElse("unknown"), systemPort);
    }

    /**
     * Create iOS driver with XCUITest.
     * 
     * @return IOSDriver instance
     * @throws MalformedURLException if hub URL is invalid
     */
    private static AppiumDriver createIOSDriver() throws MalformedURLException {
        XCUITestOptions options = new XCUITestOptions();

        String hubUrl = ConfigManager.get(CONFIG_KEY_HUB_URL, 
                                         ConfigManager.get(CONFIG_KEY_HUB_LEGACY, DEFAULT_HUB_URL));

        // BrowserStack Mode: Use SDK-managed capabilities from YAML
        if (isBrowserStackEnvironment()) {
            logger.info("BrowserStack Mode (iOS) - Using cloud execution");
            loadBrowserStackCapabilities(options);
            
            AppiumDriver driver = new IOSDriver(URI.create(getBrowserStackHubUrl()).toURL(), options);
            configureImplicitWait(driver);
            return driver;
        }

        // Local Mode: Use device pool allocation
        logger.info("Local Mode (iOS) - Using local execution");
        configureLocalIOSOptions(options);
        
        AppiumDriver driver = new IOSDriver(URI.create(hubUrl).toURL(), options);
        configureImplicitWait(driver);
        return driver;
    }
    
    /**
     * Configure iOS options for local execution.
     * 
     * @param options XCUITestOptions to configure
     */
    private static void configureLocalIOSOptions(XCUITestOptions options) {
        // Device configuration from pool
        options.setDeviceName(DevicePool.getIOSDeviceName());
        options.setPlatformVersion(DevicePool.getIOSPlatformVersion());
        options.setAutomationName(AUTOMATION_XCUITEST);
        
        // Reset and timeout settings
        options.setNoReset(ConfigManager.getBoolean(CONFIG_KEY_NO_RESET, false));
        options.setUdid(ConfigManager.get(CONFIG_KEY_IOS_UDID));
        options.setNewCommandTimeout(Duration.ofSeconds(
            ConfigManager.getInt(CONFIG_KEY_NEW_COMMAND_TIMEOUT, DEFAULT_COMMAND_TIMEOUT)));
        
        // Thread-safe WDA port allocation
        int wdaPort = wdaPortCounter.getAndIncrement();
        if (wdaPort > IOS_WDA_PORT_END) {
            wdaPortCounter.set(IOS_WDA_PORT_START);
            wdaPort = wdaPortCounter.getAndIncrement();
        }
        options.setWdaLocalPort(wdaPort);
        
        // App path resolution
        String appPath = ConfigManager.get(CONFIG_KEY_IOS_APP_PATH);
        if (appPath == null) {
            appPath = ConfigManager.get(CONFIG_KEY_APP_PATH);
        }
        if (appPath == null || appPath.isBlank()) {
            throw new IllegalStateException(
                "Local iOS app path is missing. Set 'iosAppPath' or 'appPath' in config.");
        }
        options.setApp(System.getProperty("user.dir") + "/" + appPath);
        
        logger.info("Local iOS options configured: device={}, wdaPort={}", 
                    options.getDeviceName().orElse("unknown"), wdaPort);
    }

    /**
     * Load BrowserStack capabilities from YAML configuration.
     * Applies app, automationName, source, debug, and logging settings to the options.
     * 
     * @param options BaseOptions to configure
     * @throws IllegalStateException if required capabilities (app) are missing
     */
    private static void loadBrowserStackCapabilities(BaseOptions<?> options) {
        logger.info("Loading BrowserStack capabilities from YAML...");
        
        // Load app reference (REQUIRED - must be bs://... or app path)
        Object appObj = ConfigManager.getRawValue(CONFIG_KEY_APP);
        String app = appObj != null ? appObj.toString() : null;
        
        if (app == null || app.isBlank()) {
            throw new IllegalStateException(
                "CRITICAL: 'app' capability is missing from YAML. " +
                "BrowserStack requires an app reference (e.g., bs://... or app/path/to/app.ipa)");
        }
        logger.info("✓ App reference: {}", app);
        options.setCapability(CONFIG_KEY_APP, app);
        
        // Load automationName (especially important for iOS - must be XCUITest)
        String automationName = ConfigManager.get(CONFIG_KEY_AUTOMATION_NAME);
        if (automationName != null && !automationName.isBlank()) {
            logger.info("✓ Automation engine: {}", automationName);
            options.setCapability(CONFIG_KEY_AUTOMATION_NAME, automationName);
        }
        
        // Load optional capabilities
        loadOptionalCapability(options, CONFIG_KEY_SOURCE);
        loadOptionalBooleanCapability(options, CONFIG_KEY_DEBUG);
        loadOptionalBooleanCapability(options, CONFIG_KEY_NETWORK_LOGS);
        loadOptionalBooleanCapability(options, CONFIG_KEY_DEVICE_LOGS);
        loadOptionalBooleanCapability(options, CONFIG_KEY_APPIUM_LOGS);
        loadOptionalCapability(options, CONFIG_KEY_CONSOLE_LOGS);
        loadOptionalBooleanCapability(options, CONFIG_KEY_BROWSERSTACK_LOCAL);
        
        logger.info("✓ BrowserStack capabilities loaded successfully");
    }
    
    /**
     * Load optional string capability from configuration.
     * 
     * @param options BaseOptions to configure
     * @param key Configuration key
     */
    private static void loadOptionalCapability(BaseOptions<?> options, String key) {
        String value = ConfigManager.get(key);
        if (value != null && !value.isBlank()) {
            options.setCapability(key, value);
            logger.debug("✓ {} = {}", key, value);
        }
    }
    
    /**
     * Load optional boolean capability from configuration.
     * Only sets if value is true.
     * 
     * @param options BaseOptions to configure
     * @param key Configuration key
     */
    private static void loadOptionalBooleanCapability(BaseOptions<?> options, String key) {
        if (ConfigManager.getBoolean(key, false)) {
            options.setCapability(key, true);
            logger.debug("✓ {} = enabled", key);
        }
    }
}