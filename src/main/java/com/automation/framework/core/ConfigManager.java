package com.automation.framework.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Environment-aware configuration manager.
 * Supports multiple environments (dev, staging, prod) and platforms (android, ios).
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final Properties properties = new Properties();
    private static final Properties objectProperties = new Properties();
    private static boolean initialized = false;
    
    // System property keys
    public static final String ENV_KEY = "env";
    public static final String PLATFORM_KEY = "platform";
    
    // Default values
    private static final String DEFAULT_ENV = "local";
    private static final String DEFAULT_PLATFORM = "android";
    
    private ConfigManager() {
        // Private constructor
    }
    
    /**
     * Initialize configuration from properties files.
     * Loads base config.properties file.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }
        
        logger.info("Initializing ConfigManager...");
        
        try {
            // Load base configuration
            loadProperties("src/main/resources/config.properties", properties);
            
            initialized = true;
            
            // Log platform after initialization (safe to call getPlatform now)
            logger.info("ConfigManager initialized - platform: {}", getPlatform());
            
        } catch (IOException e) {
            logger.error("Failed to load configuration", e);
            throw new RuntimeException("Configuration initialization failed", e);
        }
    }
    
    private static void loadProperties(String path, Properties props) throws IOException {
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
            logger.debug("Loaded properties from: {}", path);
        }
    }
    
    /**
     * Get current environment from system property or default.
     */
    public static String getEnvironment() {
        return System.getProperty(ENV_KEY, DEFAULT_ENV);
    }
    
    /**
     * Get current platform from system property or config file.
     * Priority: System property > config.properties > default
     */
    public static String getPlatform() {
        // First check system property (for CI/command line override)
        String systemPlatform = System.getProperty(PLATFORM_KEY);
        if (systemPlatform != null && !systemPlatform.isEmpty()) {
            return systemPlatform;
        }
        // Then check config.properties
        ensureInitialized();
        String configPlatform = properties.getProperty(PLATFORM_KEY);
        if (configPlatform != null && !configPlatform.isEmpty()) {
            return configPlatform;
        }
        // Fall back to default
        return DEFAULT_PLATFORM;
    }
    
    /**
     * Get a configuration property.
     * @param key Property key
     * @return Property value or null
     */
    public static String get(String key) {
        ensureInitialized();
        return properties.getProperty(key);
    }
    
    /**
     * Get a configuration property with default value.
     * @param key Property key
     * @param defaultValue Default if key not found
     * @return Property value or default
     */
    public static String get(String key, String defaultValue) {
        ensureInitialized();
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get an object locator property.
     * @param key Locator key
     * @return Locator value or null
     */
    public static String getLocator(String key) {
        ensureInitialized();
        return objectProperties.getProperty(key);
    }
    
    /**
     * Get integer property.
     */
    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for {}: {}", key, value);
            }
        }
        return defaultValue;
    }
    
    /**
     * Get boolean property.
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value != null) {
            return Boolean.parseBoolean(value) || "yes".equalsIgnoreCase(value);
        }
        return defaultValue;
    }
    
    private static void ensureInitialized() {
        if (!initialized) {
            init();
        }
    }
    
    /**
     * Reload configuration (useful for tests).
     */
    public static synchronized void reload() {
        initialized = false;
        properties.clear();
        objectProperties.clear();
        init();
    }
}
