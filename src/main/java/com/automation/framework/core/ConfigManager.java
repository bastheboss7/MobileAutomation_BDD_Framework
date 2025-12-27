package com.automation.framework.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * BrowserStack configuration manager.
 * Loads configuration exclusively from BrowserStack YAML files.
 * Supports Android and iOS platforms running on BrowserStack cloud.
 * 
 * YAML file resolution:
 * 1. Project root (development)
 * 2. src/test/resources/ (Maven test phase)
 * 3. target/test-classes/ (compiled tests)
 * 
 * File naming convention:
 * - Development: browserstack-{platform}.yml (e.g., browserstack-android.yml, browserstack-ios.yml)
 * - CI/CD: browserstack-{platform}-ci.yml (with hardcoded app IDs)
 * 
 * @author Baskar
 * @version 4.0.0
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final Map<String, Object> rawYamlData = new java.util.HashMap<>();
    private static boolean initialized = false;

    // System property keys
    public static final String PLATFORM_KEY = "platform";

    // Default values
    private static final String DEFAULT_PLATFORM = "android";

    private ConfigManager() {
        // Private constructor
    }

    /**
     * Initialize configuration from BrowserStack YAML files.
     * Always runs in BrowserStack mode with SDK enabled.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }

        logger.info("Initializing ConfigManager (BrowserStack Mode)...");
        String platform = System.getProperty(PLATFORM_KEY, DEFAULT_PLATFORM).toLowerCase();

        // Determine YAML file name (always BrowserStack)
        String yamlFileName = System.getProperty("browserstack.config");
        if (yamlFileName == null || yamlFileName.isEmpty()) {
            yamlFileName = "browserstack-" + (platform.contains("ios") ? "ios" : "android") + ".yml";
        }

        // Find YAML file in multiple locations
        java.io.File yamlFile = findYamlFile(yamlFileName);
        
        if (yamlFile == null) {
            String message = String.format("YAML file '%s' not found. Searched: project root, src/test/resources/, target/test-classes/", yamlFileName);
            logger.error(message);
            throw new RuntimeException(message);
        }
        
        logger.info("Loading YAML from: {} (Absolute: {})", yamlFileName, yamlFile.getAbsolutePath());
        try {
            loadYamlConfig(yamlFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Failed to load YAML: {}", yamlFile.getAbsolutePath(), e);
            throw new RuntimeException("Failed to load YAML configuration", e);
        }

        // Always enable BrowserStack SDK mode
        System.setProperty("browserstack.sdk", "true");

        if (System.getProperty("browserstack.platforms") == null) {
            System.setProperty("browserstack.platforms", platform);
        }

        initialized = true;
        logger.info("ConfigManager initialized - platform: {}", getPlatform());
    }

    /**
     * Find YAML file from multiple search locations.
     * Priority:
     * 1. Project root (for development)
     * 2. src/test/resources/ (for Maven test phase)
     * 3. target/test-classes/ (for compiled tests)
     * 
     * @param fileName YAML file name (e.g., browserstack-android.yml)
     * @return File object or null if not found
     */
    private static java.io.File findYamlFile(String fileName) {
        java.util.List<String> searchPaths = java.util.Arrays.asList(
            fileName,  // Current directory / project root
            "src/test/resources/" + fileName,
            "target/test-classes/" + fileName
        );
        
        for (String path : searchPaths) {
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                logger.info("✓ Found YAML at: {}", file.getAbsolutePath());
                return file;
            } else {
                logger.debug("✗ YAML not found at: {}", file.getAbsolutePath());
            }
        }
        
        return null;
    }

    /**
     * Load configuration from YAML into memory only.
     * No System property exports; values are read via getters.
     */
    private static void loadYamlConfig(String absolutePath) throws IOException {
        logger.info("Parsing YAML: {}", absolutePath);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.dataformat.yaml.YAMLMapper();
        Map<String, Object> yamlMap = mapper.readValue(new java.io.File(absolutePath), Map.class);

        if (yamlMap == null || yamlMap.isEmpty()) {
            logger.warn("YAML map is empty for: {}", absolutePath);
            return;
        }

        logger.info("Loaded YAML with {} top-level keys", yamlMap.size());

        // Store complete raw YAML only. No exporting to System properties.
        rawYamlData.clear();
        rawYamlData.putAll(yamlMap);
    }

    /**
     * Get current platform from system property or YAML config.
     * Priority: BrowserStack SDK > System property > YAML > default
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
        // Then check YAML configuration
        ensureInitialized();
        Object configPlatformObj = rawYamlData.get(PLATFORM_KEY);
        String configPlatform = configPlatformObj != null ? configPlatformObj.toString() : null;
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
        // 1. System properties override
        String sysValue = System.getProperty(key);
        if (sysValue != null && !sysValue.isEmpty()) {
            return sysValue;
        }
        // 2. Top-level YAML value
        Object topLevel = rawYamlData.get(key);
        if (topLevel != null && !(topLevel instanceof Map) && !(topLevel instanceof java.util.List)) {
            return topLevel.toString();
        }
        // 3. frameworkOptions nested value
        Object options = rawYamlData.get("frameworkOptions");
        if (options instanceof Map) {
            Object nested = ((Map<?, ?>) options).get(key);
            if (nested != null) {
                return nested.toString();
            }
        }
        return null;
    }

    /**
     * Get a configuration property with default value.
     * 
     * @param key          Property key
     * @param defaultValue Default if key not found
     * @return Property value or default
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
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

    /**
     * Get raw YAML value (for complex structures like lists or maps).
     * Useful for accessing nested YAML structures like 'platforms'.
     * 
     * @param key Key to retrieve
     * @return Raw object or null if not found
     */
    public static Object getRawValue(String key) {
        ensureInitialized();
        return rawYamlData.get(key);
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
        rawYamlData.clear();
        init();
    }
}
