package com.automation.framework.core;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for building BrowserStack-specific capabilities.
 * Integrates device pool pattern with BrowserStack cloud execution.
 * 
 * @author Baskar
 * @version 1.0.0
 */
public class BrowserStackCapabilityBuilder {
    private static final Logger logger = LoggerFactory.getLogger(BrowserStackCapabilityBuilder.class);

    private static final String BROWSERSTACK_HUB_IDENTIFIER = "browserstack.com";

    private BrowserStackCapabilityBuilder() {
        // Private constructor - utility class
    }

    /**
     * Checks if the hub URL points to BrowserStack cloud.
     * 
     * @param hubUrl Appium hub URL
     * @return true if BrowserStack hub, false otherwise
     */
    public static boolean isBrowserStackHub(String hubUrl) {
        return hubUrl != null && hubUrl.contains(BROWSERSTACK_HUB_IDENTIFIER);
    }

    /**
     * Formats the hub URL to include credentials for BrowserStack execution.
     * 
     * @param hubUrl Original hub URL
     * @return Hub URL with username and access key embedded
     */
    public static String getAuthorizedHubUrl(String hubUrl) {
        if (!isBrowserStackHub(hubUrl)) {
            return hubUrl;
        }

        Map<String, String> credentials = BrowserStackAppUploader.getBrowserStackCredentials();
        String username = credentials.get("username");
        String accessKey = credentials.get("accessKey");

        if (username == null || accessKey == null) {
            logger.warn("BrowserStack credentials missing. Using unauthorized URL.");
            return hubUrl;
        }

        // Handle case where URL might already have credentials
        if (hubUrl.contains("@")) {
            return hubUrl;
        }

        // Format: https://username:accessKey@hub-cloud.browserstack.com/wd/hub
        String protocol = hubUrl.contains("://") ? hubUrl.split("://")[0] : "https";
        String hostPath = hubUrl.contains("://") ? hubUrl.split("://")[1] : hubUrl;

        return String.format("%s://%s:%s@%s", protocol, username, accessKey, hostPath);
    }

    /**
     * Adds BrowserStack-specific capabilities for Android driver.
     * This method should be called when executing tests on BrowserStack cloud.
     * 
     * @param options         UiAutomator2Options to enhance with BrowserStack
     *                        capabilities
     * @param deviceName      Device name from device pool
     * @param platformVersion OS version from device pool
     */
    public static void addBrowserStackCapabilities(
            UiAutomator2Options options,
            String deviceName,
            String platformVersion) {

        logger.info("Adding BrowserStack capabilities for Android device: {} (OS: {})",
                deviceName, platformVersion);

        Map<String, Object> bstackOptions = buildBrowserStackOptions(deviceName, platformVersion, "android");

        // Set BrowserStack options capability
        options.setCapability("bstack:options", bstackOptions);

        logger.debug("BrowserStack capabilities added: {}", bstackOptions);
    }

    /**
     * Adds BrowserStack-specific capabilities for iOS driver.
     * This method should be called when executing tests on BrowserStack cloud.
     * 
     * @param options         XCUITestOptions to enhance with BrowserStack
     *                        capabilities
     * @param deviceName      Device name from device pool
     * @param platformVersion OS version from device pool
     */
    public static void addBrowserStackCapabilities(
            XCUITestOptions options,
            String deviceName,
            String platformVersion) {

        logger.info("Adding BrowserStack capabilities for iOS device: {} (OS: {})",
                deviceName, platformVersion);

        Map<String, Object> bstackOptions = buildBrowserStackOptions(deviceName, platformVersion, "ios");

        // Set BrowserStack options capability
        options.setCapability("bstack:options", bstackOptions);

        logger.debug("BrowserStack capabilities added: {}", bstackOptions);
    }

    /**
     * Builds the BrowserStack options map with device and project configuration.
     * 
     * @param deviceName      Device name
     * @param platformVersion OS version
     * @param platform        Platform type (android/ios)
     * @return Map of BrowserStack options
     */
    private static Map<String, Object> buildBrowserStackOptions(
            String deviceName,
            String platformVersion,
            String platform) {

        Map<String, Object> bstackOptions = new HashMap<>();

        // Device configuration from device pool
        bstackOptions.put("deviceName", deviceName);
        bstackOptions.put("osVersion", platformVersion);
        bstackOptions.put("realDevice", true);

        // Project configuration from ConfigManager (optional)
        String projectName = ConfigManager.get("browserstack.projectName", "CrossMobiletest");
        String buildName = ConfigManager.get("browserstack.buildName", "Device Pool Execution");

        bstackOptions.put("projectName", projectName);
        bstackOptions.put("buildName", buildName);

        // Session name with thread info for parallel execution tracking
        String threadName = Thread.currentThread().getName();
        String sessionName = String.format("%s - %s - Thread: %s",
                platform, deviceName, threadName);
        bstackOptions.put("sessionName", sessionName);

        // Additional BrowserStack settings
        bstackOptions.put("debug", ConfigManager.getBoolean("browserstack.debug", true));
        bstackOptions.put("networkLogs", ConfigManager.getBoolean("browserstack.networkLogs", true));
        bstackOptions.put("consoleLogs", ConfigManager.get("browserstack.consoleLogs", "errors"));

        return bstackOptions;
    }

    /**
     * Logs BrowserStack execution details for debugging.
     * 
     * @param deviceName      Device name
     * @param platformVersion Platform version
     * @param hubUrl          Hub URL
     */
    public static void logBrowserStackExecution(String deviceName, String platformVersion, String hubUrl) {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("BrowserStack Execution Detected");
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("Hub URL: {}", hubUrl);
        logger.info("Device: {}", deviceName);
        logger.info("OS Version: {}", platformVersion);
        logger.info("Thread: {}", Thread.currentThread().getName());
        logger.info("Environment: {}", ConfigManager.getEnvironment());
        logger.info("═══════════════════════════════════════════════════════════");
    }
}
