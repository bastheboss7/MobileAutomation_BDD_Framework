package com.automation.framework.core;

import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe driver manager using ThreadLocal.
 * Manages driver lifecycle for parallel test execution.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class DriverManager {
    private static final Logger logger = LoggerFactory.getLogger(DriverManager.class);
    private static final ThreadLocal<AppiumDriver> driverThreadLocal = new ThreadLocal<>();
    
    private DriverManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Get the driver for the current thread.
     * @return AppiumDriver instance
     */
    public static AppiumDriver getDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver == null) {
            logger.warn("Driver is null for current thread. Call setDriver() first.");
        }
        return driver;
    }
    
    /**
     * Set the driver for the current thread.
     * @param driver AppiumDriver instance
     */
    public static void setDriver(AppiumDriver driver) {
        if (driver != null) {
            logger.debug("Setting driver for thread: {}", Thread.currentThread().getName());
            driverThreadLocal.set(driver);
        }
    }
    
    /**
     * Remove the driver from current thread and quit if active.
     */
    public static void quitDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                logger.debug("Quitting driver for thread: {}", Thread.currentThread().getName());
                driver.quit();
            } catch (Exception e) {
                logger.error("Error quitting driver", e);
            } finally {
                driverThreadLocal.remove();
            }
        }
    }
    
    /**
     * Check if driver exists for current thread.
     * @return true if driver is set
     */
    public static boolean hasDriver() {
        return driverThreadLocal.get() != null;
    }
}
