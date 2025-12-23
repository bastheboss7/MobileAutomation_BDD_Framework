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
    
    // Atomic counter for thread-safe port allocation (avoids collision)
    private static final AtomicInteger portCounter = new AtomicInteger(8200);
    private static final int MAX_PORT = 8299;
    
    public enum Platform {
        ANDROID,
        IOS
    }
    
    private DriverFactory() {
        // Private constructor
    }
    
    /**
     * Create a driver based on the current platform configuration.
     * @return AppiumDriver instance
     */
    public static AppiumDriver createDriver() {
        String platformStr = ConfigManager.getPlatform().toUpperCase();
        Platform platform = platformStr.equals("IOS") ? Platform.IOS : Platform.ANDROID;
        
        return createDriver(platform);
    }
    
    /**
     * Create a driver for a specific platform.
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
        
        // Device configuration
        options.setDeviceName(ConfigManager.get("deviceName", "emulator-5554"));
        options.setUdid(ConfigManager.get("udid", "emulator-5554"));
        options.setAutomationName("UiAutomator2");
        options.setNoReset(ConfigManager.getBoolean("noReset", false));
        options.setNewCommandTimeout(Duration.ofSeconds(
                ConfigManager.getInt("newCommandTimeout", 6000)));
        
        // Use atomic counter for thread-safe port allocation in parallel execution
        int dynamicPort = portCounter.getAndIncrement();
        if (dynamicPort > MAX_PORT) {
            portCounter.set(8200);
            dynamicPort = portCounter.getAndIncrement();
        }
        options.setSystemPort(dynamicPort);
        logger.info("Allocated systemPort: {} (thread-safe)", dynamicPort);
        
        // Native app configuration
        String appPackage = ConfigManager.get("appPackage");
        String appActivity = ConfigManager.get("appActivity");
        
        if (appPackage != null && appActivity != null) {
            options.setAppPackage(appPackage);
            options.setAppActivity(appActivity);
        }
        
        // Install app - resolve relative path to absolute
        String apkPath = ConfigManager.get("apkPath");
        if (apkPath != null && !apkPath.isEmpty()) {
            if (!apkPath.startsWith("/")) {
                apkPath = System.getProperty("user.dir") + "/" + apkPath;
            }
            options.setApp(apkPath);
            logger.info("Setting APK path for auto-install: {}", apkPath);
        }
        
        logger.info("Configured for Android native app: {}", appPackage);
        
        String hubUrl = ConfigManager.get("HUB", "http://127.0.0.1:4723");
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
        
        // Device configuration
        options.setDeviceName(ConfigManager.get("iosDeviceName", "iPhone 15"));
        options.setUdid(ConfigManager.get("iosUdid"));
        options.setPlatformVersion(ConfigManager.get("iosPlatformVersion", "17.0"));
        options.setAutomationName("XCUITest");
        options.setNoReset(ConfigManager.getBoolean("noReset", false));
        options.setNewCommandTimeout(Duration.ofSeconds(
                ConfigManager.getInt("newCommandTimeout", 6000)));
        
        // WDA configuration
        int wdaPort = ConfigManager.getInt("wdaLocalPort", 8100);
        options.setWdaLocalPort(wdaPort);
        
        // Native app configuration
        String bundleId = ConfigManager.get("bundleId");
        if (bundleId != null && !bundleId.isEmpty()) {
            options.setBundleId(bundleId);
        }
        
        // Install app - resolve relative path to absolute
        String appPath = ConfigManager.get("iosAppPath");
        if (appPath != null && !appPath.isEmpty()) {
            if (!appPath.startsWith("/")) {
                appPath = System.getProperty("user.dir") + "/" + appPath;
            }
            options.setApp(appPath);
            logger.info("Setting iOS app path for install: {}", appPath);
        }
        
        logger.info("Configured for iOS native app: {}", bundleId);
        
        String hubUrl = ConfigManager.get("HUB", "http://127.0.0.1:4723");
        logger.info("Connecting to Appium server: {}", hubUrl);
        
        AppiumDriver driver = new IOSDriver(URI.create(hubUrl).toURL(), options);
        
        // Set implicit wait
        int implicitWait = ConfigManager.getInt("implicitWait", 30);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        
        return driver;
    }
}
