package com.automation.tests.hooks;

import com.automation.framework.core.DriverFactory;
import com.automation.framework.core.DriverManager;
import com.automation.framework.utils.ScreenshotUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cucumber Hooks for test lifecycle management.
 * Handles driver setup/teardown and screenshots.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class CucumberHooks {
    private static final Logger logger = LoggerFactory.getLogger(CucumberHooks.class);
    
    @Before
    public void setUp(Scenario scenario) {
        logger.info("Starting scenario: {}", scenario.getName());
        logger.info("Tags: {}", scenario.getSourceTagNames());
        
        // Create driver based on platform configuration
        DriverFactory.createDriver();
        
        logger.info("Driver created successfully for scenario: {}", scenario.getName());
    }
    
    @After
    public void tearDown(Scenario scenario) {
        logger.info("Finishing scenario: {} - Status: {}", scenario.getName(), scenario.getStatus());
        
        if (scenario.isFailed()) {
            logger.warn("Scenario failed: {}", scenario.getName());
            
            // Capture screenshot on failure
            byte[] screenshot = ScreenshotUtils.captureAsBytes();
            if (screenshot.length > 0) {
                scenario.attach(screenshot, "image/png", "Failure Screenshot");
                logger.info("Attached failure screenshot");
            }
        }
        
        // Quit driver
        DriverManager.quitDriver();
        logger.info("Driver quit for scenario: {}", scenario.getName());
    }
}
