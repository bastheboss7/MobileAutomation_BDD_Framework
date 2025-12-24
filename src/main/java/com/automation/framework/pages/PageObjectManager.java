package com.automation.framework.pages;

import com.automation.framework.pages.screens.HomeScreen;
import com.automation.framework.pages.screens.LoginScreen;

/**
 * Centralized manager for Page Object instances.
 * Uses lazy initialization to create page objects only when needed.
 * Provides single instances to avoid duplicate object creation across step definitions.
 * 
 * Thread-safe using ThreadLocal for parallel execution support.
 * 
 * @author Baskar
 * @version 1.0.0
 */
public class PageObjectManager {
    
    // ThreadLocal ensures each parallel thread gets its own set of page objects
    private static final ThreadLocal<PageObjectManager> instance = ThreadLocal.withInitial(PageObjectManager::new);
    
    // Lazy-initialized page objects
    private HomeScreen homeScreen;
    private LoginScreen loginScreen;
    
    private PageObjectManager() {
        // Private constructor for singleton per thread
    }
    
    /**
     * Get the PageObjectManager instance for the current thread.
     * @return PageObjectManager instance
     */
    public static PageObjectManager getInstance() {
        return instance.get();
    }
    
    /**
     * Get HomeScreen page object (lazy initialization).
     * @return HomeScreen instance
     */
    public HomeScreen getHomeScreen() {
        if (homeScreen == null) {
            homeScreen = new HomeScreen();
        }
        return homeScreen;
    }
    
    /**
     * Get LoginScreen page object (lazy initialization).
     * @return LoginScreen instance
     */
    public LoginScreen getLoginScreen() {
        if (loginScreen == null) {
            loginScreen = new LoginScreen();
        }
        return loginScreen;
    }
    
    // Add more page getters as needed:
    // public FormsScreen getFormsScreen() { ... }
    // public SwipeScreen getSwipeScreen() { ... }
    // public DragScreen getDragScreen() { ... }
    
    /**
     * Reset all page objects for the current thread.
     * Call this in @After hook to clean up between scenarios.
     */
    public static void reset() {
        instance.remove();
    }
}
