package com.automation.framework.core;

/**
 * Framework-wide constants for BrowserStack cloud execution.
 * Centralized location for all configuration values.
 * 
 * @author Baskar
 * @version 3.0.0 - Pruned legacy constants; SDK-managed caps only
 */
public final class FrameworkConstants {
    
    // BrowserStack
    public static final String BROWSERSTACK_HUB_URL = "https://hub-cloud.browserstack.com/wd/hub";
    public static final String BROWSERSTACK_USERNAME_ENV = "BROWSERSTACK_USERNAME";
    public static final String BROWSERSTACK_ACCESS_KEY_ENV = "BROWSERSTACK_ACCESS_KEY";
    
    // Default Timeouts (seconds)
    public static final int DEFAULT_IMPLICIT_WAIT = 30;

    // Config Keys (only those still used by framework)
    public static final String CONFIG_KEY_IMPLICIT_WAIT = "implicitWait";
    
    private FrameworkConstants() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
}
