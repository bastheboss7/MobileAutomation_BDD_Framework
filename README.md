# Mobile Automation BDD Framework

Enterprise-grade BDD test automation framework for mobile applications using Appium, Cucumber, and TestNG. Supports both **Android** and **iOS** platforms with a clean, modular architecture.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Appium](https://img.shields.io/badge/Appium-2.x-purple.svg)](https://appium.io/)
[![Cucumber](https://img.shields.io/badge/Cucumber-7.20-green.svg)](https://cucumber.io/)

## 🏗️ Architecture

```
src/
├── main/java/                              # Framework Library
│   └── com/automation/framework/
│       ├── core/
│       │   ├── ConfigManager.java          # Multi-environment configuration
│       │   ├── DriverFactory.java          # Platform-specific driver creation
│       │   └── DriverManager.java          # Thread-safe driver management
│       ├── pages/
│       │   ├── BasePage.java               # Base class for all page objects
│       │   ├── PageObjectManager.java      # Thread-safe page object lifecycle
│       │   ├── locators/
│       │   │   └── WdioLocators.java       # Centralized locator constants
│       │   └── screens/
│       │       ├── HomeScreen.java         # Home screen with navigation
│       │       └── LoginScreen.java        # Login page object
│       └── reports/
│           └── ExtentReportManager.java    # Extent Reports management
│
├── main/resources/
│   ├── config.properties                   # Base configuration
│   ├── config-local.properties             # Local environment overrides
│   ├── config-staging.properties           # Staging environment overrides
│   ├── config-prod.properties              # Production environment overrides
│   └── logback.xml                         # Logging configuration
│
├── test/java/                              # Test Code
│   ├── listeners/
│   │   ├── ExtentReportListener.java       # Report lifecycle management
│   │   ├── RetryAnalyzer.java              # Retry failed tests
│   │   └── RetryTransformer.java           # Auto-apply retry to all tests
│   ├── runner/
│   │   └── TestNgRunner.java               # Cucumber-TestNG integration
│   └── stepdefinitions/
│       ├── Hooks.java                      # Cucumber Before/After hooks
│       └── WdioLoginSteps.java             # Step definitions
│
└── test/resources/
    └── features/
        └── wdioLogin.feature               # BDD feature files
```

---

## 🧵 Parallel Execution Architecture & Thread Lifecycle

This framework is designed from the ground up for **thread-safe parallel execution**. Each component uses specific patterns to ensure complete isolation between concurrent test threads.

### Thread Lifecycle Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                        PARALLEL TEST EXECUTION LIFECYCLE                             │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   ┌──────────────────────────────────────────────────────────────────────────────┐  │
│   │                         TestNG Orchestration Layer                            │  │
│   │  testngParallel.xml: parallel="methods" thread-count="N"                     │  │
│   └──────────────────────────────────────────────────────────────────────────────┘  │
│                                    │                                                 │
│                    ┌───────────────┼───────────────┐                                │
│                    ▼               ▼               ▼                                │
│   ┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐          │
│   │     Thread-1        │ │     Thread-2        │ │     Thread-N        │          │
│   │   (Scenario A)      │ │   (Scenario B)      │ │   (Scenario X)      │          │
│   └─────────┬───────────┘ └─────────┬───────────┘ └─────────┬───────────┘          │
│             │                       │                       │                        │
│   ┌─────────▼───────────────────────▼───────────────────────▼───────────┐          │
│   │                      SHARED SINGLETON LAYER                          │          │
│   │  ┌─────────────────────────────────────────────────────────────────┐│          │
│   │  │ ConfigManager (synchronized init, immutable properties)        ││          │
│   │  │ ExtentReports (synchronized createTest, single report file)    ││          │
│   │  └─────────────────────────────────────────────────────────────────┘│          │
│   └─────────────────────────────────────────────────────────────────────┘          │
│             │                       │                       │                        │
│   ┌─────────▼───────────────────────▼───────────────────────▼───────────┐          │
│   │                      THREAD-LOCAL ISOLATION LAYER                    │          │
│   │                                                                      │          │
│   │   ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐       │          │
│   │   │DriverManager    │ │DriverManager    │ │DriverManager    │       │          │
│   │   │ThreadLocal<     │ │ThreadLocal<     │ │ThreadLocal<     │       │          │
│   │   │ AppiumDriver>   │ │ AppiumDriver>   │ │ AppiumDriver>   │       │          │
│   │   │ Port: 8200      │ │ Port: 8201      │ │ Port: 8202      │       │          │
│   │   └────────┬────────┘ └────────┬────────┘ └────────┬────────┘       │          │
│   │            │                   │                   │                 │          │
│   │   ┌────────▼────────┐ ┌────────▼────────┐ ┌────────▼────────┐       │          │
│   │   │PageObjectMgr    │ │PageObjectMgr    │ │PageObjectMgr    │       │          │
│   │   │ThreadLocal<     │ │ThreadLocal<     │ │ThreadLocal<     │       │          │
│   │   │ POM Instance>   │ │ POM Instance>   │ │ POM Instance>   │       │          │
│   │   │ └─HomeScreen    │ │ └─HomeScreen    │ │ └─HomeScreen    │       │          │
│   │   │ └─LoginScreen   │ │ └─LoginScreen   │ │ └─LoginScreen   │       │          │
│   │   └────────┬────────┘ └────────┬────────┘ └────────┬────────┘       │          │
│   │            │                   │                   │                 │          │
│   │   ┌────────▼────────┐ ┌────────▼────────┐ ┌────────▼────────┐       │          │
│   │   │ExtentTest       │ │ExtentTest       │ │ExtentTest       │       │          │
│   │   │ThreadLocal<     │ │ThreadLocal<     │ │ThreadLocal<     │       │          │
│   │   │ ExtentTest>     │ │ ExtentTest>     │ │ ExtentTest>     │       │          │
│   │   │ "Scenario A"    │ │ "Scenario B"    │ │ "Scenario X"    │       │          │
│   │   └─────────────────┘ └─────────────────┘ └─────────────────┘       │          │
│   └──────────────────────────────────────────────────────────────────────┘          │
│             │                       │                       │                        │
│   ┌─────────▼───────────────────────▼───────────────────────▼───────────┐          │
│   │                      ATOMIC PORT ALLOCATION LAYER                    │          │
│   │  ┌─────────────────────────────────────────────────────────────────┐│          │
│   │  │ DriverFactory.androidPortCounter = AtomicInteger(8200)         ││          │
│   │  │ DriverFactory.wdaPortCounter = AtomicInteger(8100)             ││          │
│   │  │                                                                 ││          │
│   │  │ Thread-1 → getAndIncrement() → Port 8200                       ││          │
│   │  │ Thread-2 → getAndIncrement() → Port 8201  (atomic, no locks!)  ││          │
│   │  │ Thread-N → getAndIncrement() → Port 8202                       ││          │
│   │  └─────────────────────────────────────────────────────────────────┘│          │
│   └──────────────────────────────────────────────────────────────────────┘          │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘

                              CLEANUP PHASE (per thread)
┌─────────────────────────────────────────────────────────────────────────────────────┐
│   @After Hook (Hooks.java) - GUARANTEED via try-finally                             │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │  finally {                                                                   │   │
│   │      PageObjectManager.reset();   // ThreadLocal.remove() ← clears POM      │   │
│   │      DriverManager.quitDriver();  // driver.quit() + ThreadLocal.remove()   │   │
│   │  }                                                                           │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Component-Level Thread Safety

#### 1️⃣ DriverManager - Thread-Local Driver Isolation

Each thread gets its own isolated `AppiumDriver` instance via `ThreadLocal`:

```java
// DriverManager.java - Thread-safe driver management
public class DriverManager {
    // Each thread has its own driver instance - ZERO contention
    private static final ThreadLocal<AppiumDriver> driverThreadLocal = new ThreadLocal<>();
    
    public static AppiumDriver getDriver() {
        return driverThreadLocal.get();  // Returns THIS thread's driver only
    }
    
    public static void setDriver(AppiumDriver driver) {
        driverThreadLocal.set(driver);   // Sets driver for THIS thread only
    }
    
    public static void quitDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();           // Quit THIS thread's driver
            } finally {
                driverThreadLocal.remove(); // CRITICAL: Prevent memory leak
            }
        }
    }
}
```

**Why ThreadLocal?**
- No synchronization overhead (each thread has its own copy)
- Zero contention between parallel tests
- Memory is automatically cleaned up when `remove()` is called

---

#### 2️⃣ DriverFactory - Atomic Port Allocation

Parallel tests need unique ports to avoid collision. Uses `AtomicInteger` for lock-free thread-safe counters:

```java
// DriverFactory.java - Lock-free port allocation
public class DriverFactory {
    // Atomic counters - thread-safe without synchronized blocks
    private static final AtomicInteger androidPortCounter = new AtomicInteger(8200);
    private static final AtomicInteger wdaPortCounter = new AtomicInteger(8100);
    
    private static AppiumDriver createAndroidDriver() {
        UiAutomator2Options options = new UiAutomator2Options();
        
        // Atomic increment - guaranteed unique port per thread
        int dynamicPort = androidPortCounter.getAndIncrement();
        if (dynamicPort > 8299) {
            androidPortCounter.set(8200);  // Wrap around
            dynamicPort = androidPortCounter.getAndIncrement();
        }
        options.setSystemPort(dynamicPort);
        
        // Thread-1 gets 8200, Thread-2 gets 8201, etc. - NO COLLISION
        return new AndroidDriver(url, options);
    }
    
    private static AppiumDriver createIOSDriver() {
        XCUITestOptions options = new XCUITestOptions();
        
        // Same pattern for iOS WDA ports
        int wdaPort = wdaPortCounter.getAndIncrement();
        options.setWdaLocalPort(wdaPort);  // 8100, 8101, 8102...
        
        return new IOSDriver(url, options);
    }
}
```

**Port Allocation Strategy:**
| Platform | Port Range | Purpose |
|----------|------------|---------|
| Android | 8200-8299 | UiAutomator2 system port |
| iOS | 8100-8199 | WebDriverAgent local port |

---

#### 3️⃣ PageObjectManager - Thread-Local Page Objects

Each thread gets its own set of page objects, preventing cross-contamination:

```java
// PageObjectManager.java - Thread-isolated page objects
public class PageObjectManager {
    // Each thread gets its own PageObjectManager with its own page objects
    private static final ThreadLocal<PageObjectManager> instance = 
        ThreadLocal.withInitial(PageObjectManager::new);
    
    // Lazy-initialized page objects (created once per thread, reused)
    private HomeScreen homeScreen;
    private LoginScreen loginScreen;
    
    public static PageObjectManager getInstance() {
        return instance.get();  // Returns THIS thread's manager
    }
    
    public HomeScreen getHomeScreen() {
        if (homeScreen == null) {
            homeScreen = new HomeScreen();  // Lazy init for THIS thread
        }
        return homeScreen;
    }
    
    public LoginScreen getLoginScreen() {
        if (loginScreen == null) {
            loginScreen = new LoginScreen();  // Lazy init for THIS thread
        }
        return loginScreen;
    }
    
    public static void reset() {
        instance.remove();  // Clears THIS thread's manager + all page objects
    }
}
```

**Step Definitions use composition (not inheritance):**
```java
// WdioLoginSteps.java - Clean composition pattern
public class WdioLoginSteps {
    // Method references avoid creating instances upfront
    private HomeScreen homeScreen() {
        return PageObjectManager.getInstance().getHomeScreen();
    }
    
    private LoginScreen loginScreen() {
        return PageObjectManager.getInstance().getLoginScreen();
    }

    @Given("I navigate to the Login screen")
    public void iNavigateToTheLoginScreen() {
        homeScreen().navigateToLogin();  // Gets THIS thread's HomeScreen
    }
}
```

---

#### 4️⃣ ConfigManager - Thread-Safe Singleton

Configuration is loaded once and never modified (immutable after init):

```java
// ConfigManager.java - Double-checked locking singleton
public class ConfigManager {
    private static final Properties properties = new Properties();
    private static boolean initialized = false;
    
    public static synchronized void init() {
        if (initialized) return;  // Fast-path: already initialized
        
        // Load properties (happens only ONCE across all threads)
        loadProperties("config.properties", properties);
        loadProperties("config-" + env + ".properties", properties);
        
        initialized = true;
    }
    
    public static String get(String key) {
        init();  // Ensure initialized (no-op if already done)
        // Safe to read from multiple threads - Properties is thread-safe for reads
        return System.getProperty(key, properties.getProperty(key));
    }
}
```

**Why safe?**
- `synchronized init()` ensures one-time initialization
- After init, properties are read-only (immutable)
- `java.util.Properties` is thread-safe for concurrent reads

---

#### 5️⃣ ExtentReportManager - Thread-Safe Reporting

Single report file with thread-local test instances:

```java
// ExtentReportManager.java - Shared report, isolated tests
public class ExtentReportManager {
    private static ExtentReports extentReports;  // SHARED across threads
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();  // ISOLATED
    
    public static synchronized void initReports() {
        if (extentReports == null) {
            // One-time initialization (synchronized)
            extentReports = new ExtentReports();
            extentReports.attachReporter(new ExtentSparkReporter(path));
        }
    }
    
    public static ExtentTest createTest(String testName) {
        initReports();
        // Thread-safe: createTest is synchronized internally by ExtentReports
        ExtentTest test = extentReports.createTest(testName);
        extentTest.set(test);  // Store in THIS thread's local
        return test;
    }
    
    public static void logPass(String message) {
        ExtentTest test = extentTest.get();  // Get THIS thread's test
        if (test != null) {
            test.pass(message);  // Log to THIS thread's test section
        }
    }
    
    public static synchronized void flushReports() {
        if (extentReports != null) {
            extentReports.flush();  // Write all results to file (once at end)
        }
    }
}
```

**Architecture:**
```
ExtentReports (Singleton)
├── Thread-1: ExtentTest "Login Scenario"
│   ├── Step: "Navigate to login" ✅
│   └── Step: "Enter credentials" ✅
├── Thread-2: ExtentTest "Forms Scenario"  
│   ├── Step: "Open forms" ✅
│   └── Step: "Submit form" ❌
└── Thread-N: ExtentTest "Swipe Scenario"
    └── ...
```

---

#### 6️⃣ Hooks - Guaranteed Cleanup with try-finally

Critical for preventing resource leaks in parallel execution:

```java
// Hooks.java - Guaranteed cleanup pattern
public class Hooks {
    
    @Before
    public void launchApplication(Scenario scenario) {
        ExtentReportManager.createTest(scenario.getName());
        DriverFactory.createDriver();  // Creates driver, stores in ThreadLocal
    }
    
    @After
    public void executeAfterScenario(Scenario scenario) {
        try {
            // Handle screenshots, logging, etc.
            if (scenario.isFailed() && DriverManager.hasDriver()) {
                byte[] screenshot = DriverManager.getDriver().getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "failure");
            }
        } catch (Exception e) {
            logger.warn("Screenshot failed: {}", e.getMessage());
        } finally {
            // ALWAYS executes - even if screenshot fails!
            PageObjectManager.reset();   // Clear page objects
            DriverManager.quitDriver();  // Quit driver + clear ThreadLocal
        }
    }
}
```

**Why try-finally?**
- Ensures cleanup happens even if screenshot capture fails
- Prevents driver/memory leaks that could crash parallel runs
- Maintains isolation between test scenarios

---

### Parallel Execution Memory Model

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           JVM HEAP                                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │              SHARED MEMORY (Singletons)                             │ │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │ │
│  │  │  ConfigManager   │  │  ExtentReports   │  │  AtomicInteger   │  │ │
│  │  │  (Properties)    │  │  (Report File)   │  │  (Port Counter)  │  │ │
│  │  └──────────────────┘  └──────────────────┘  └──────────────────┘  │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │              THREAD-LOCAL MEMORY (Per Thread)                       │ │
│  │                                                                      │ │
│  │  Thread-1 Stack                    Thread-2 Stack                    │ │
│  │  ┌─────────────────────┐          ┌─────────────────────┐           │ │
│  │  │ ThreadLocal Data:   │          │ ThreadLocal Data:   │           │ │
│  │  │ ├─ AppiumDriver     │          │ ├─ AppiumDriver     │           │ │
│  │  │ ├─ PageObjectMgr    │          │ ├─ PageObjectMgr    │           │ │
│  │  │ │  ├─ HomeScreen    │          │ │  ├─ HomeScreen    │           │ │
│  │  │ │  └─ LoginScreen   │          │ │  └─ LoginScreen   │           │ │
│  │  │ └─ ExtentTest       │          │ └─ ExtentTest       │           │ │
│  │  └─────────────────────┘          └─────────────────────┘           │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Running Parallel Tests

```bash
# Enable parallel execution in TestNG
mvn clean test -Dsurefire.suiteXmlFiles=testngParallel.xml

# Or via command line
mvn test -Dparallel=methods -DthreadCount=4 -Dplatform=android
```

**testngParallel.xml configuration:**
```xml
<suite name="MobileAutomation-BDD-Suite" parallel="methods" thread-count="4">
    <listeners>
        <listener class-name="listeners.ExtentReportListener"/>
        <listener class-name="listeners.RetryTransformer"/>
    </listeners>
    <test name="Mobile-BDD-Tests">
        <classes>
            <class name="runner.TestNgRunner"/>
        </classes>
    </test>
</suite>
```

### Thread Safety Checklist

| Component | Pattern | Thread-Safe? | Notes |
|-----------|---------|--------------|-------|
| DriverManager | ThreadLocal | ✅ | Each thread has isolated driver |
| PageObjectManager | ThreadLocal | ✅ | Each thread has isolated page objects |
| ExtentReportManager | ThreadLocal + Synchronized | ✅ | Shared report, isolated test nodes |
| ConfigManager | Synchronized init | ✅ | One-time load, immutable after |
| DriverFactory | AtomicInteger | ✅ | Lock-free port allocation |
| BasePage | Stateless | ✅ | Uses thread's driver via DriverManager |
| Hooks | try-finally | ✅ | Guaranteed cleanup |

## 🎯 Key Design Patterns

| Pattern | Implementation | Benefit |
|---------|---------------|---------|
| **Page Object Model** | `BasePage` → `HomeScreen`/`LoginScreen` → Step Definitions | Separation of concerns, maintainable |
| **Factory Pattern** | `DriverFactory` with atomic port allocation | Platform-agnostic, thread-safe driver creation |
| **Singleton Pattern** | `ConfigManager` with synchronized init | Centralized multi-env configuration |
| **Thread-Local** | `DriverManager`, `PageObjectManager`, `ExtentTest` | Complete thread isolation for parallel execution |
| **Composition over Inheritance** | Step definitions use `PageObjectManager.getInstance()` | Flexible, avoids diamond problem |
| **Atomic Operations** | `AtomicInteger` port counters | Lock-free thread-safe port allocation |
| **Lazy Initialization** | `PageObjectManager.getHomeScreen()` | Memory efficient, on-demand creation |
| **Template Method** | `BasePage` defines common actions | DRY, consistent element interactions |
| **Strategy Pattern** | Element-based assertions with fallbacks | Reliable mobile element detection |

## Architectural Traceability & Rationale ##

Why This Design?
Our framework is architected for scalability, maintainability, and parallel execution. Key design choices include:

Layered Traceability:
Each test scenario flows from .feature files (business intent), through step definitions (glue), into screen/page objects (UI abstraction), and down to base classes and utilities (engine). This clear separation ensures traceability from business requirements to code.

OOP Principles:

Abstract BasePage: Enforces a contract for all screens, sharing common actions while preventing direct instantiation.
Protected Methods: Restrict low-level actions to subclasses, encapsulating driver logic and reducing accidental misuse.
Final Utility Classes: (e.g., WdioLocators) Prevent inheritance and instantiation, ensuring a single source of truth for constants and locators.
Composition Over Inheritance:
Utility classes (e.g., ElementActions, WaitUtils) are injected into page objects, promoting loose coupling and easier testing.

Thread Safety:
DriverManager uses ThreadLocal to isolate driver instances, enabling safe parallel test execution and maximizing resource utilization.

Design Patterns:

Singleton: For configuration management, ensuring consistent settings.
Factory: For driver creation, supporting multiple platforms.
Page Object Manager: Centralizes and reuses page objects, reducing duplication.
Architectural Intelligence
This design:

Supports parallelism (via ThreadLocal drivers).
Enforces encapsulation (via access modifiers and abstract classes).
Promotes reusability and maintainability (via composition and design patterns).
Enables clear traceability from business logic to engine code, making debugging and onboarding easier.


## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- Appium Server 2.x (`npm install -g appium`)
- Android SDK (for Android testing)
- Xcode (for iOS testing on macOS)

### Installation

```bash
# Clone the repository
git clone <repository-url>
cd MobileAutomation_BDD_Framework

# Install dependencies
mvn clean install -DskipTests

# Install Appium drivers
appium driver install uiautomator2   # Android
appium driver install xcuitest       # iOS
```

### Running Tests

```bash
# Run with default settings (uses config.properties + config-local.properties)
mvn clean test

# Run on specific platform
mvn clean test -Dplatform=android
mvn clean test -Dplatform=ios

# Run with specific environment
mvn clean test -Denv=staging -Dplatform=android
mvn clean test -Denv=prod -Dplatform=ios

# Run specific Cucumber tags
mvn clean test -Dcucumber.filter.tags="@Smoke"
mvn clean test -Dcucumber.filter.tags="@Login and @Smoke"

# Run with TestNG suite file
mvn clean verify -Dsurefire.suiteXmlFiles=testngParallel.xml
```

---

## 📱 Starting Simulators & Emulators

### Android Emulator

```bash
# List available AVDs (Android Virtual Devices)
emulator -list-avds

# Start emulator by name
emulator -avd <avd_name>

# Start with specific options
emulator -avd Pixel_7_API_33 -no-snapshot-load

# Start headless (for CI)
emulator -avd Pixel_7_API_33 -no-window -no-audio -gpu swiftshader_indirect

# Check connected devices
adb devices

# Cold boot (fresh start)
emulator -avd <avd_name> -no-snapshot
```

**Create new AVD (if none exist):**
```bash
# List available system images
sdkmanager --list | grep system-images

# Download system image
sdkmanager "system-images;android-33;google_apis;x86_64"

# Create AVD
avdmanager create avd -n Pixel_7_API_33 -k "system-images;android-33;google_apis;x86_64" --device "pixel_7"
```

### iOS Simulator (macOS only)

**Prerequisites:**
```bash
# Install Xcode Command Line Tools (if not installed)
xcode-select --install

# If you get "simctl not found" error, ensure Xcode path is set:
sudo xcode-select -s /Applications/Xcode.app/Contents/Developer

# Verify installation
xcrun simctl help
```

**Simulator Commands:**
```bash
# List available simulators
xcrun simctl list devices available

# Check which simulators are already running
xcrun simctl list devices | grep Booted

# Boot a specific simulator by name
xcrun simctl boot "iPhone 16e"

# "Booted" (running its background services) without the "Simulator" window actually being visible on your screen.
open -a Simulator

# Boot by UDID (more reliable)
xcrun simctl boot 473154E6-C7B8-494F-A943-CAE9B3033BCC

# Open Simulator app (shows booted simulators)
open -a Simulator

# Shutdown simulator
xcrun simctl shutdown "iPhone 16e"

# Shutdown all simulators
xcrun simctl shutdown all

# Erase simulator (reset to clean state)
xcrun simctl erase "iPhone 16e"
```

**Common Errors:**
| Error | Cause | Solution |
|-------|-------|----------|
| `Unable to boot device in current state: Booted` | Simulator already running | Just run `open -a Simulator` to view it |
| `Unable to boot device in current state: Shutdown` | Normal state | Boot command should work |
| `Invalid device` | Wrong name/UDID | Run `xcrun simctl list devices available` to get correct name |

**Useful iOS Simulator Commands:**
```bash
# Install app on simulator
xcrun simctl install booted /path/to/app.app

# Uninstall app
xcrun simctl uninstall booted <bundle_id>

# Open URL in simulator
xcrun simctl openurl booted "https://example.com"

# Take screenshot
xcrun simctl io booted screenshot screenshot.png

# Record video
xcrun simctl io booted recordVideo video.mov
```

### Start Appium Server

```bash
# Start Appium (default port 4723)
appium

# Start on custom port
appium --port 4724

# Start with relaxed security (allows all capabilities)
appium --relaxed-security

# Start with specific drivers only
appium --use-drivers=uiautomator2,xcuitest

# Check Appium status
curl http://127.0.0.1:4723/status
```

---

## ⚙️ Configuration

### Multi-Environment Configuration

The framework uses a layered configuration approach:

```
Priority (highest to lowest):
1. System Properties (-Dkey=value)
2. config-{env}.properties (environment-specific)
3. config.properties (base defaults)
```

**Usage:**
```bash
mvn test -Denv=local      # Loads config.properties + config-local.properties
mvn test -Denv=staging    # Loads config.properties + config-staging.properties
mvn test -Denv=prod       # Loads config.properties + config-prod.properties
```

### Configuration Files

| File | Purpose |
|------|---------|
| `config.properties` | Base defaults (always loaded) |
| `config-local.properties` | Local development (localhost, emulators) |
| `config-staging.properties` | Staging environment (staging servers) |
| `config-prod.properties` | Production (cloud device farms) |

### Key Configuration Properties

```properties
# Platform: android or ios
platform=android

# Appium Server
HUB=http://127.0.0.1:4723

# iOS Configuration
iosDeviceName=iPhone 15 Pro
iosUdid=<simulator-udid>
iosPlatformVersion=17.0
iosAppPath=app/wdiodemoapp.app
bundleId=org.reactjs.native.example.wdiodemoapp

# Android Configuration
deviceName=emulator-5554
udid=emulator-5554
platformVersion=13
appPackage=com.wdiodemoapp
appActivity=com.wdiodemoapp.MainActivity
apkPath=app/android.wdio.native.app.v1.0.8.apk

# Timeouts (seconds)
implicitWait=30
explicitWait=10

# Test Execution
retry.maxCount=2
screenshot.on.step=false
screenshot.on.failure=true
```

---

## 🐳 Docker Support

### Run with Docker Compose

```bash
# Start Appium Grid with Android emulator
docker-compose up -d

# View running containers
docker-compose ps

# Run tests in container
docker-compose -f docker-compose.test.yml up --abort-on-container-exit

# View emulator via VNC (port 6080)
open http://localhost:6080

# Stop all containers
docker-compose down
```

---

## 🔄 CI/CD Integration

### GitHub Actions
Pipeline file: `.github/workflows/mobile-tests.yml`

```bash
# Triggers on push to main/develop
# Supports manual trigger with platform/environment selection
```

### Azure DevOps
Pipeline file: `.azure-pipelines/mobile-tests.yml`

```bash
# Parameterized pipeline for platform, environment, tags
# Publishes test results and artifacts
```

---

## 📊 Reporting

### Extent Reports
Rich HTML reports at: `target/extent-reports/TestReport_<timestamp>.html`

Features:
- 📸 Screenshots (configurable: on failure only or every step)
- ✅ Pass/Fail status with detailed logs
- 📱 Platform and environment info
- ⏱️ Execution timestamps
- 🔄 Retry attempt tracking

### Cucumber Reports
JSON/HTML reports at: `target/reports/cucumber-report/`

---

## 🛠️ Technology Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | Programming Language |
| Appium Java Client | 9.4.0 | Mobile Automation |
| Selenium | 4.27.0 | WebDriver |
| Cucumber | 7.20.1 | BDD Framework |
| TestNG | 7.10.2 | Test Runner |
| ExtentReports | 5.1.1 | HTML Reporting |
| SLF4J + Logback | 2.0.9 | Logging |
| Docker | - | Containerization |

---

## 🔧 Extending the Framework

### Adding a New Screen

1. **Add Locators** (`src/main/java/com/automation/framework/pages/locators/`):
```java
public class HomeLocators {
    public static final String WELCOME_TEXT = "welcome-message";
    public static final String LOGOUT_BUTTON = "button-logout";
}
```

2. **Create Page Object** (`src/main/java/com/automation/framework/pages/screens/`):
```java
public class HomeScreen extends BasePage {
    public String getWelcomeMessage() {
        return getTextByAccessibility(HomeLocators.WELCOME_TEXT);
    }
    
    public void tapLogout() {
        clickByAccessibility(HomeLocators.LOGOUT_BUTTON);
    }
}
```

3. **Create Step Definitions** (`src/test/java/stepdefinitions/`):
```java
public class HomeSteps extends HomeScreen {
    @Then("I should see welcome message {string}")
    public void verifyWelcomeMessage(String expected) {
        String actual = getWelcomeMessage();
        Assert.assertEquals(actual, expected);
        reportStep("Welcome message verified", "PASS");
    }
}
```

4. **Add Feature File** (`src/test/resources/features/`):
```gherkin
@Home
Feature: Home Screen
  Scenario: Verify welcome message after login
    Given I am logged in
    Then I should see welcome message "Welcome!"
```

---

## 📝 Quick Reference

| Command | Description |
|---------|-------------|
| `mvn clean test` | Run all tests |
| `mvn test -Dplatform=ios` | Run on iOS |
| `mvn test -Denv=staging` | Run on staging environment |
| `mvn test -Dcucumber.filter.tags="@Smoke"` | Run smoke tests |
| `appium` | Start Appium server |
| `adb devices` | List Android devices |
| `xcrun simctl list devices` | List iOS simulators |
| `emulator -list-avds` | List Android AVDs |
| `docker-compose up -d` | Start Appium Grid |

---

## 📄 License

This project is proprietary and confidential.

---

*Framework Version: 4.0.0*  
*Last Updated: December 2025*

