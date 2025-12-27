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
│       │   ├── DriverFactory.java          # Platform-specific driver creation (SDK-only)
│       │   ├── DriverManager.java          # Thread-safe driver management
│       │   └── BrowserStackCapabilityBuilder.java # BS specific capabilities (legacy)
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
│   ├── local/
│   │   ├── android/
│   │   │   └── local-android-config.properties # Local Android overrides
│   │   └── ios/
│   │       └── local-ios-config.properties     # Local iOS overrides
│   ├── browserstack/
│   │   └── config-*.properties             # BrowserStack environment overrides
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
# Local Android Test
mvn clean test -Pandroid -Denv=local

# Local iOS Test
mvn clean test -Pios -Denv=local

# Run specific tags locally
mvn clean test -Pandroid -Denv=local -Dcucumber.filter.tags="@MyTag"
```

### 🧪 Local Android (Emulator) Run

Use the local YAML [local-android.yml](local-android.yml) and run on an emulator.

**Prerequisites:**
- Android SDK + AVD created (e.g., `Pixel_6_API_33`)
- Appium 2.x installed

**Quick start:**
```bash
# 1) Start emulator (headless by default)
./scripts/start-emulator.sh Pixel_6_API_33

# 2) Start Appium
./scripts/start-appium.sh 4723

# 3) Run local Android tests (uses local-android.yml)
mvn clean test -Pandroid -Denv=local -Dcucumber.filter.tags="@Login and not @Skip"
```

**Local config values** come from [local-android.yml](local-android.yml):
- `deviceName: emulator-5554`
- `platformVersion: 12`
- `appPath: ./app/local/android/android.wdio.native.app.v1.0.8.apk`
- `HUB: http://127.0.0.1:4723`

**Tips:**
- Verify emulator device with `adb devices`.
- If your AVD name differs, pass it to the script: `./scripts/start-emulator.sh <YourAVDName>`.
- If you use a different local port, update `HUB` in [local-android.yml](local-android.yml).

### 🧪 Local iOS (Simulator) Run

Use the local YAML [local-ios.yml](local-ios.yml) and run on a simulator.

**Prerequisites:**
- Xcode installed with iOS Simulator
- Appium 2.x installed
- XCUITest driver: `appium driver install xcuitest`

**Quick start:**
```bash
# 1) Start Appium
./scripts/start-appium.sh 4723

# 2) Run local iOS tests (uses local-ios.yml)
mvn clean test -Pios -Denv=local -Dcucumber.filter.tags="@Login and not @Skip"
```

**Local config values** come from [local-ios.yml](local-ios.yml):
- `deviceName: iPhone 14`
- `platformVersion: 16.0`
- `appPath: ./app/local/ios/wdiodemoapp.app`
- `HUB: http://127.0.0.1:4723`

**Tips:**
- List available simulators: `xcrun simctl list devices available`
- If your simulator differs, update `deviceName` and `platformVersion` in [local-ios.yml](local-ios.yml).

---

## **BrowserStack (SDK-Only)**

### Overview
- **SDK-Driven:** BrowserStack Java SDK agent manages all capabilities, app upload, and session lifecycle.
- **No hubUrl in YAML:** SDK sets hub endpoint internally via agent. Remove `hubUrl` from config.
- **Credentials:** Load via environment variables (`BROWSERSTACK_USERNAME`, `BROWSERSTACK_ACCESS_KEY`). Never hardcode for CI/CD.
- **App Reference:** Use `custom_id` (string identifier) or `bs://<id>` (uploaded app ID) to avoid stale references.

### Android
- **Prerequisites:** BrowserStack account; Java 21+; Maven 3.8+.
- **Config:** [browserstack-android.yml](browserstack-android.yml) with:
  - `app: custom_id:my-android-app` (recommended) OR `app: bs://uploaded-app-id`
  - NO `hubUrl` (SDK handles endpoint)
  - `platforms` with device names and OS versions
- **Credentials:** Provide via environment:
```bash
export BROWSERSTACK_USERNAME=<your-username>
export BROWSERSTACK_ACCESS_KEY=<your-access-key>
```
- **Run:**
```bash
mvn clean test -Pbrowserstack -Dplatform=android -Denv=browserstack -Dcucumber.filter.tags="@androidOnly"
```
- **Verify:** BrowserStack dashboard shows sessions; logs confirm app ID and device allocation.

### iOS
- **Prerequisites:** BrowserStack account; Java 21+; Maven 3.8+.
- **Config:** [browserstack-ios.yml](browserstack-ios.yml) with:
  - `app: custom_id:my-ios-app` (recommended) OR `app: bs://uploaded-app-id`
  - NO `hubUrl` (SDK handles endpoint)
  - `platforms` with device names and OS versions
- **Credentials:** Provide via environment (same as Android above).
- **Run:**
```bash
mvn clean test -Pbrowserstack -Dplatform=ios -Denv=browserstack -Dcucumber.filter.tags="@iosOnly"
```
- **Verify:** BrowserStack dashboard shows sessions; logs confirm app ID and device allocation.

### Custom App IDs (Best Practice)
Instead of using ephemeral `bs://...` IDs, use `custom_id` for stable app references:
```yaml
# browserstack-android.yml (SDK-only)
app: custom_id:my-android-app-v1  # Human-readable, stable across runs

platforms:
  - deviceName: Samsung Galaxy S23 Ultra
    osVersion: 13.0
    platformName: android
```
Benefits:
- Stable app reference across CI/CD runs.
- Easy to rotate app versions without changing config.
- Self-documenting (e.g., `custom_id:my-app-v1` vs `bs://02d88594d8c7d0ba`).

### Platform Tag Filtering (Critical)

**All platform-specific commands MUST include the `-Dcucumber.filter.tags` parameter** to isolate tests by platform. This prevents cross-platform test contamination and ensures correct locator evaluation.

#### Why Tag Filtering is Essential
- **Locator Compatibility**: Android and iOS interpret XPath, CSS, and ID locators differently. Running Android-only tests on iOS (or vice versa) causes `ClassCastException` and element interaction failures.
- **Driver Response Marshalling**: The BrowserStack Java SDK formats element responses differently per platform. Platform-aware tests account for these differences.
- **Step Isolation**: Steps marked with `@androidOnly` use Android-specific navigation; steps marked with `@iosOnly` use iOS-specific gestures.

#### Tag Usage
- `@androidOnly`: Tests/steps that execute ONLY on Android
- `@iosOnly`: Tests/steps that execute ONLY on iOS
- `@BStackSample`: Shared scenarios (not platform-specific; optional)

#### Commands with Proper Filtering
```bash
# Android: Run only @androidOnly scenarios
mvn clean test -Pbrowserstack -Dplatform=android -Denv=browserstack -Dcucumber.filter.tags="@androidOnly"

# iOS: Run only @iosOnly scenarios
mvn clean test -Pbrowserstack -Dplatform=ios -Denv=browserstack -Dcucumber.filter.tags="@iosOnly"

# Multi-tag (AND logic): Run scenarios tagged with both @androidOnly AND @smoke
mvn clean test -Pbrowserstack -Dplatform=android -Denv=browserstack -Dcucumber.filter.tags="@androidOnly and @smoke"

# Exclusive execution (NOT logic): Run all scenarios EXCEPT @androidOnly (useful for iOS)
mvn clean test -Pbrowserstack -Dplatform=ios -Denv=browserstack -Dcucumber.filter.tags="not @androidOnly"
```

#### Common Mistakes
- ❌ **Missing tag filter**: `mvn clean test -Pbrowserstack -Dplatform=ios -Denv=browserstack` → Runs ALL scenarios including `@androidOnly`, causing failures on iOS.
- ❌ **Wrong tag**: `mvn clean test -Pbrowserstack -Dplatform=ios -Denv=browserstack -Dcucumber.filter.tags="@androidOnly"` → Runs Android tests on iOS; element interactions fail.
- ❌ **Typo in tag**: `mvn clean test -Pbrowserstack -Dplatform=android -Denv=browserstack -Dcucumber.filter.tags="@androidonly"` (lowercase) → Tag doesn't match (case-sensitive); no tests run.

#### Example Feature File Structure
```gherkin
@androidOnly
Scenario: Login with valid credentials (Android-specific)
  Given User opens the wdiodemoapp on Android
  When User enters username and taps login
  Then Login success is displayed

@iosOnly
Scenario: Verify Alert Functionality (iOS-specific)
  Given User opens the BStackSampleApp on iOS
  When User taps the alert trigger button
  Then Alert is displayed
  And User accepts the alert

@BStackSample
Scenario: Shared flow (both platforms)
  Given User opens the app
  When User performs a generic action
  Then Generic success is displayed
```

**Always validate your feature file tags match your `-Dcucumber.filter.tags` parameter before running tests.**


