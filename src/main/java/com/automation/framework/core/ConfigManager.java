package com.automation.framework.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Environment-aware configuration manager.
 * Supports multiple environments (local, staging, prod) and platforms (android,
 * ios).
 * 
 * Environment loading priority:
 * 1. config.properties (base defaults)
 * 2. config-{env}.properties (environment-specific overrides)
 * 3. System properties (CLI overrides)
 * 
 * @author Baskar
 * @version 3.0.0
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final Properties properties = new Properties();
    private static final Properties objectProperties = new Properties();
    private static boolean initialized = false;
    private static String currentEnvironment;

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
     * Initialize configuration from YAML files.
     * Supports both Local and BrowserStack environments using YAML.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }

        logger.info("Initializing ConfigManager (Unified YAML Mode)...");
        currentEnvironment = System.getProperty(ENV_KEY, DEFAULT_ENV);
        String platform = System.getProperty(PLATFORM_KEY, DEFAULT_PLATFORM).toLowerCase();

        // Determine config file path
        String configFileName = System.getProperty("browserstack.config");
        if (configFileName == null || configFileName.isEmpty()) {
            if (currentEnvironment.contains("bs") || currentEnvironment.contains("browserstack")) {
                configFileName = "browserstack-" + (platform.contains("ios") ? "ios" : "android") + ".yml";
            } else {
                configFileName = "local-" + (platform.contains("ios") ? "ios" : "android") + ".yml";
            }
        }

        java.io.File configFile = new java.io.File(configFileName);
        logger.info("Loading configuration from: {} (Absolute: {})", configFileName, configFile.getAbsolutePath());
        try {
            if (configFile.exists()) {
                loadYamlConfig(configFileName);
            }

            // Load companion framework file if exists (BS environments use platform-specific companion)
            String frameworkCompanion;
            if (currentEnvironment.contains("bs") || currentEnvironment.contains("browserstack")) {
                // Use framework-bs-<platform>.yml when running on BrowserStack
                frameworkCompanion = "framework-bs-" + (platform.contains("ios") ? "ios" : "android") + ".yml";
            } else {
                // Default companion naming for local envs: framework-<env>.yml
                frameworkCompanion = "framework-" + currentEnvironment + ".yml";
            }

            java.io.File companionFile = new java.io.File(frameworkCompanion);
            if (companionFile.exists()) {
                logger.info("Loading companion framework configuration: {} - Path: {}", frameworkCompanion,
                        companionFile.getAbsolutePath());
                loadYamlConfig(frameworkCompanion);
            } else if (!configFile.exists()) {
                logger.warn("YAML configuration {} not found at {}", configFileName, configFile.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("CRITICAL: Failed to load YAML configuration: {}", configFileName, e);
            throw new RuntimeException("Failed to load YAML configuration", e);
        }

        // Bridge platform for BrowserStack SDK if active
        if (currentEnvironment.contains("bs") || currentEnvironment.contains("browserstack")) {
            System.setProperty("browserstack.sdk", "true");
        }

        if (System.getProperty("browserstack.platforms") == null) {
            System.setProperty("browserstack.platforms", platform);
        }

        initialized = true;
        logger.info("ConfigManager initialized - env: {}, platform: {}", currentEnvironment, getPlatform());
    }

    /**
     * Load configuration from YAML and export to System properties.
     */
    private static void loadYamlConfig(String path) throws IOException {
        logger.info("Parsing YAML: {}", path);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.dataformat.yaml.YAMLMapper();
        Map<String, Object> yamlMap = mapper.readValue(new java.io.File(path), Map.class);

        if (yamlMap == null || yamlMap.isEmpty()) {
            logger.warn("YAML map is empty for: {}", path);
            return;
        }

        logger.info("Loaded YAML with {} top-level keys", yamlMap.size());
        // Export top-level keys
        yamlMap.forEach((k, v) -> {
            if (v != null && !(v instanceof Map)) {
                System.setProperty(k, v.toString());
                properties.setProperty(k, v.toString());
            }
        });

        // Export frameworkOptions specifically
        if (yamlMap.containsKey("frameworkOptions")) {
            Map<String, Object> options = (Map<String, Object>) yamlMap.get("frameworkOptions");
            options.forEach((k, v) -> {
                if (v != null) {
                    System.setProperty(k, v.toString());
                    properties.setProperty(k, v.toString());
                }
            });
        }
    }

    /**
     * Get current environment from system property or default.
     */
    public static String getEnvironment() {
        ensureInitialized(); // Ensure initialization before accessing currentEnvironment
        return currentEnvironment;
    }

    /**
     * Get current platform from system property or config file.
     * Priority: System property > config.properties > default
     */
    public static String getPlatform() {
        // 0. Check for BrowserStack SDK assigned platform (highest priority in forks)
        String sdkPlatform = System.getProperty("browserstack.platformName");
        if (sdkPlatform != null && !sdkPlatform.isEmpty()) {
            return sdkPlatform;
        }

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
     * 
     * @param key Property key
     * @return Property value or null
     */
    public static String get(String key) {
        ensureInitialized();
        return properties.getProperty(key);
    }

    /**
     * Get a configuration property with default value.
     * 
     * @param key          Property key
     * @param defaultValue Default if key not found
     * @return Property value or default
     */
    public static String get(String key, String defaultValue) {
        ensureInitialized();
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get an object locator property.
     * 
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
