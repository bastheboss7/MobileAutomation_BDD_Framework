package com.automation.framework.core;

/**
 * Framework-wide constants.
 * Centralized location for all magic strings and configuration values.
 * 
 * @author Baskar
 * @version 1.0.0
 */
public final class FrameworkConstants {
    
    // BrowserStack
    public static final String BROWSERSTACK_HUB_URL = "https://hub-cloud.browserstack.com/wd/hub";
    public static final String BROWSERSTACK_USERNAME_ENV = "BROWSERSTACK_USERNAME";
    public static final String BROWSERSTACK_ACCESS_KEY_ENV = "BROWSERSTACK_ACCESS_KEY";
    
    // Automation Names
    public static final String AUTOMATION_UIAUTOMATOR2 = "UiAutomator2";
    public static final String AUTOMATION_XCUITEST = "XCUITest";
    
    // Default Ports
    public static final int ANDROID_PORT_START = 8200;
    public static final int ANDROID_PORT_END = 8299;
    public static final int IOS_WDA_PORT_START = 8100;
    public static final int IOS_WDA_PORT_END = 8199;
    
    // Default Timeouts (seconds)
    public static final int DEFAULT_IMPLICIT_WAIT = 30;
    public static final int DEFAULT_COMMAND_TIMEOUT = 6000;
    
    // Default Appium Hub
    public static final String DEFAULT_HUB_URL = "http://127.0.0.1:4723";
    
    // Config Keys
    public static final String CONFIG_KEY_HUB_URL = "hubUrl";
    public static final String CONFIG_KEY_HUB_LEGACY = "HUB";
    public static final String CONFIG_KEY_APP = "app";
    public static final String CONFIG_KEY_AUTOMATION_NAME = "automationName";
    public static final String CONFIG_KEY_SOURCE = "source";
    public static final String CONFIG_KEY_DEBUG = "debug";
    public static final String CONFIG_KEY_NETWORK_LOGS = "networkLogs";
    public static final String CONFIG_KEY_DEVICE_LOGS = "deviceLogs";
    public static final String CONFIG_KEY_APPIUM_LOGS = "appiumLogs";
    public static final String CONFIG_KEY_CONSOLE_LOGS = "consoleLogs";
    public static final String CONFIG_KEY_BROWSERSTACK_LOCAL = "browserstackLocal";
    public static final String CONFIG_KEY_NO_RESET = "noReset";
    public static final String CONFIG_KEY_IMPLICIT_WAIT = "implicitWait";
    public static final String CONFIG_KEY_NEW_COMMAND_TIMEOUT = "newCommandTimeout";
    public static final String CONFIG_KEY_ANDROID_APP_PATH = "androidAppPath";
    public static final String CONFIG_KEY_IOS_APP_PATH = "iosAppPath";
    public static final String CONFIG_KEY_APP_PATH = "appPath";
    public static final String CONFIG_KEY_IOS_UDID = "iosUdid";
    
    // Environment identifiers
    public static final String ENV_BROWSERSTACK = "browserstack";
    public static final String ENV_BS_SHORT = "bs";
    
    private FrameworkConstants() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
}
