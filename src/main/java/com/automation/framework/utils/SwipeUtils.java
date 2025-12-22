package com.automation.framework.utils;

import com.automation.framework.core.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;

/**
 * Utility class for swipe operations using W3C Actions API.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class SwipeUtils {
    private static final Logger logger = LoggerFactory.getLogger(SwipeUtils.class);
    private static final Duration DEFAULT_SWIPE_DURATION = Duration.ofMillis(500);
    
    private SwipeUtils() {
        // Private constructor
    }
    
    /**
     * Perform a swipe gesture from one point to another.
     */
    public static void swipe(int startX, int startY, int endX, int endY) {
        swipe(startX, startY, endX, endY, DEFAULT_SWIPE_DURATION);
    }
    
    /**
     * Perform a swipe gesture with custom duration.
     */
    public static void swipe(int startX, int startY, int endX, int endY, Duration duration) {
        AppiumDriver driver = DriverManager.getDriver();
        
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(duration, PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        
        driver.perform(Collections.singletonList(swipe));
        logger.debug("Swiped from ({},{}) to ({},{})", startX, startY, endX, endY);
    }
    
    /**
     * Swipe left on the screen.
     */
    public static void swipeLeft() {
        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int startX = (int) (size.getWidth() * 0.8);
        int endX = (int) (size.getWidth() * 0.2);
        int y = size.getHeight() / 2;
        
        swipe(startX, y, endX, y);
    }
    
    /**
     * Swipe right on the screen.
     */
    public static void swipeRight() {
        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int startX = (int) (size.getWidth() * 0.2);
        int endX = (int) (size.getWidth() * 0.8);
        int y = size.getHeight() / 2;
        
        swipe(startX, y, endX, y);
    }
    
    /**
     * Swipe up on the screen.
     */
    public static void swipeUp() {
        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.8);
        int endY = (int) (size.getHeight() * 0.2);
        
        swipe(x, startY, x, endY);
    }
    
    /**
     * Swipe down on the screen.
     */
    public static void swipeDown() {
        Dimension size = DriverManager.getDriver().manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.2);
        int endY = (int) (size.getHeight() * 0.8);
        
        swipe(x, startY, x, endY);
    }
}
