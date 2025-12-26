package com.automation.framework.pages;

/**
 * Centralized manager for Page Object instances.
 * Uses lazy initialization to create page objects only when needed.
 * Provides single instances to avoid duplicate object creation across step
 * definitions.
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

    private com.automation.framework.pages.screens.LocalSampleScreen localSampleScreen;
    private com.automation.framework.pages.screens.BStackSampleScreen bStackSampleScreen;
    private com.automation.framework.pages.screens.HomeScreen homeScreen;
    private com.automation.framework.pages.screens.LoginScreen loginScreen;

    private PageObjectManager() {
        // Private constructor for singleton per thread
    }

    /**
     * Get the PageObjectManager instance for the current thread.
     * 
     * @return PageObjectManager instance
     */
    public static PageObjectManager getInstance() {
        return instance.get();
    }

    /**
     * Get LocalSampleScreen page object (lazy initialization).
     * 
     * @return LocalSampleScreen instance
     */
    public com.automation.framework.pages.screens.LocalSampleScreen getLocalSampleScreen() {
        if (localSampleScreen == null) {
            localSampleScreen = new com.automation.framework.pages.screens.LocalSampleScreen();
        }
        return localSampleScreen;
    }

    /**
     * Get BStackSampleScreen page object (lazy initialization).
     * 
     * @return BStackSampleScreen instance
     */
    public com.automation.framework.pages.screens.BStackSampleScreen getBStackSampleScreen() {
        if (bStackSampleScreen == null) {
            bStackSampleScreen = new com.automation.framework.pages.screens.BStackSampleScreen();
        }
        return bStackSampleScreen;
    }

    public com.automation.framework.pages.screens.HomeScreen getHomeScreen() {
        if (homeScreen == null) {
            homeScreen = new com.automation.framework.pages.screens.HomeScreen();
        }
        return homeScreen;
    }

    public com.automation.framework.pages.screens.LoginScreen getLoginScreen() {
        if (loginScreen == null) {
            loginScreen = new com.automation.framework.pages.screens.LoginScreen();
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
