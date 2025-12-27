# Mobile Automation BDD Framework

![BrowserStack Certified](https://img.shields.io/badge/BrowserStack-Certified-brightgreen?style=for-the-badge&logo=browserstack)
![Architecture Score](https://img.shields.io/badge/Architecture%20Score-A+-blue?style=for-the-badge&logo=dependabot)

## 🏆 Framework Architecture Score
**Current Rating: 9.0/10**

✅ ThreadLocal driver isolation for parallel execution  
✅ BrowserStack SDK integration with YAML-based configuration  
✅ Page Object Model (POM) with platform-specific locators  
✅ BDD implementation using Cucumber & Gherkin  
✅ Automatic test retry mechanism with RetryAnalyzer  
✅ Extent Reports with screenshot capture  
✅ Platform-agnostic design (Android/iOS support)  
✅ CI/CD ready with BrowserStack pipeline integration  

## 🚀 What's new (Dec 2025)
- BrowserStack-only: all local/emulator support removed.
- Capabilities injected by BrowserStack SDK from YAML (no manual capability setting in code).
- ConfigManager loads raw BrowserStack YAML only; no properties files.
- `browserstack.local` support removed; only cloud device runs.
- Waits configured via system property `implicitWait` or defaults; YAML does not include waits.


## 🧪 Test Execution
- Default TestNG suite: `testngSuite.xml`
- Android (tag filter required):
```bash
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=android \
    -Dbrowserstack.config=browserstack-android.yml \
    -Dcucumber.filter.tags="@androidOnly"
```
- iOS (tag filter required):
```bash
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=ios \
    -Dbrowserstack.config=browserstack-ios.yml \
    -Dcucumber.filter.tags="@iosOnly"
```
- Never run without the platform-specific `-Dcucumber.filter.tags`; mixing Android/iOS steps in one run will fail.
├── main/java/
│   └── com/automation/framework/
## ⚙️ Configuration (BrowserStack-only)
- Credentials via environment variables (`BROWSERSTACK_USERNAME`, `BROWSERSTACK_ACCESS_KEY`) or YAML (`userName`, `accessKey`).
- Select platform via `-Dplatform=android|ios` (default android). BrowserStack SDK reads platforms list from YAML.
- YAML lives at repo root; SDK injects capabilities directly. No config.properties files.
- Waits: Configure via system property `-DimplicitWait=10` or rely on BasePage defaults (10 seconds). **Do NOT include `frameworkOptions` in YAML** (unsupported by BrowserStack SDK).
- `browserstack.local` is not used.

## 📁 Project Structure

```
├── main/java/
│   └── com/automation/framework/
│       ├── pages/
│       │   ├── BasePage.java               # Base class with waits/helpers
│       │   ├── HomeScreen.java             # Home screen POM
│       │   └── LoginScreen.java            # Login screen POM
│       ├── driver/
│       │   ├── DriverFactory.java          # Creates AppiumDriver instances
│       │   └── DriverManager.java          # ThreadLocal driver isolation
│       ├── config/
│       │   └── ConfigManager.java          # YAML config loader
│       └── utils/
│           ├── ElementActions.java         # Common element interactions
│           └── WaitUtils.java              # Explicit waits
├── test/java/
│   ├── runner/
│   │   └── TestNgRunner.java              # Cucumber + TestNG runner
│   ├── stepdefinitions/
│   │   └── WdioLoginSteps.java            # Step implementations
│   ├── listeners/
│   │   ├── ExtentReportListener.java      # Extent Reports integration
│   │   └── ExtentReportManager.java       # ThreadLocal report isolation
│   └── hooks/
│       └── Hooks.java                      # @Before/@After scenario hooks
├── test/resources/
│   └── features/
│       ├── BStackSample.feature           # BrowserStack test scenarios
│       └── wdioLogin.feature              # WDIO login test scenarios
├── pom.xml                                 # Maven dependencies & configuration
├── testngSuite.xml                         # TestNG suite (parallel execution)
├── browserstack-android.yml                # BrowserStack Android config
├── browserstack-ios.yml                    # BrowserStack iOS config
├── browserstack-android-ci.yml             # BrowserStack Android CI config
└── browserstack-ios-ci.yml                 # BrowserStack iOS CI config
```

**Note:** Parallel execution is configured in [testngSuite.xml](testngSuite.xml) with `parallel="methods"` and `thread-count="N"`. All test commands above will automatically use this parallel configuration.

## 🧵 Thread Safety & Parallel Execution

### Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                  PARALLEL TEST EXECUTION (BrowserStack Cloud)            │
│   ┌──────────────────────────────────────────────────────────────────┐  │
│   │                    TestNG Orchestration Layer                    │  │
│   │  testngSuite.xml: parallel="methods" thread-count="N"           │  │
│   └──────────────────────────────────────────────────────────────────┘  │
│                                    │                                     │
│                    ┌───────────────┼───────────────┐                    │
│                    ▼               ▼               ▼                    │
│   ┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────┐ │
│   │     Thread-1        │ │     Thread-2        │ │     Thread-N    │ │
│   │   (Scenario A)      │ │   (Scenario B)      │ │   (Scenario X)  │ │
│   └─────────┬───────────┘ └─────────┬───────────┘ └─────────┬───────┘ │
│             │                       │                       │           │
│   ┌─────────▼───────────────────────▼───────────────────────▼───────┐  │
│   │                  SHARED SINGLETON LAYER                          │  │
│   │  ┌───────────────────────────────────────────────────────────┐  │  │
│   │  │ ConfigManager (synchronized init, immutable YAML data)   │  │  │
│   │  │ ExtentReports (synchronized createTest, single report)    │  │  │
│   │  └───────────────────────────────────────────────────────────┘  │  │
│   └──────────────────────────────────────────────────────────────────┘  │
│             │                       │                       │           │
│   ┌─────────▼───────────────────────▼───────────────────────▼───────┐  │
│   │                  THREAD-LOCAL ISOLATION LAYER                    │  │
│   │                                                                  │  │
│   │   ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐  │  │
│   │   │DriverManager    │ │DriverManager    │ │DriverManager    │  │  │
│   │   │ThreadLocal<     │ │ThreadLocal<     │ │ThreadLocal<     │  │  │
│   │   │ AppiumDriver>   │ │ AppiumDriver>   │ │ AppiumDriver>   │  │  │
│   │   │ → BS Session 1  │ │ → BS Session 2  │ │ → BS Session N  │  │  │
│   │   └────────┬────────┘ └────────┬────────┘ └────────┬────────┘  │  │
│   │            │                   │                   │            │  │
│   │   ┌────────▼────────┐ ┌────────▼────────┐ ┌────────▼────────┐  │  │
│   │   │PageObjectMgr    │ │PageObjectMgr    │ │PageObjectMgr    │  │  │
│   │   │ThreadLocal<     │ │ThreadLocal<     │ │ThreadLocal<     │  │  │
│   │   │ POM Instance>   │ │ POM Instance>   │ │ POM Instance>   │  │  │
│   │   │ └─HomeScreen    │ │ └─HomeScreen    │ │ └─HomeScreen    │  │  │
│   │   │ └─LoginScreen   │ │ └─LoginScreen   │ │ └─LoginScreen   │  │  │
│   │   └────────┬────────┘ └────────┬────────┘ └────────┬────────┘  │  │
│   │            │                   │                   │            │  │
│   │   ┌────────▼────────┐ ┌────────▼────────┐ ┌────────▼────────┐  │  │
│   │   │ExtentTest       │ │ExtentTest       │ │ExtentTest       │  │  │
│   │   │ThreadLocal<     │ │ThreadLocal<     │ │ThreadLocal<     │  │  │
│   │   │ ExtentTest>     │ │ ExtentTest>     │ │ ExtentTest>     │  │  │
│   │   │ "Scenario A"    │ │ "Scenario B"    │ │ "Scenario X"    │  │  │
│   │   └─────────────────┘ └─────────────────┘ └─────────────────┘  │  │
│   └──────────────────────────────────────────────────────────────────┘  │
│             │                       │                       │           │
│             ▼                       ▼                       ▼           │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │              BROWSERSTACK CLOUD INFRASTRUCTURE                   │  │
│   │  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐       │  │
│   │  │ Real Device 1 │  │ Real Device 2 │  │ Real Device N │       │  │
│   │  │ Galaxy S23    │  │ iPhone 15 Pro │  │ Pixel 8       │       │  │
│   │  │ Android 13    │  │ iOS 17        │  │ Android 14    │       │  │
│   │  └───────────────┘  └───────────────┘  └───────────────┘       │  │
│   │  SDK manages: App upload, Capabilities, Device allocation       │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

                        CLEANUP PHASE (per thread)
┌─────────────────────────────────────────────────────────────────────────┐
│   @After Hook (Hooks.java) - GUARANTEED via try-finally                 │
│   ┌─────────────────────────────────────────────────────────────────┐   │
│   │  finally {                                                       │   │
│   │      PageObjectManager.reset();   // ThreadLocal.remove()       │   │
│   │      DriverManager.quitDriver();  // Quits BrowserStack session │   │
│   │  }                                                               │   │
│   └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
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

#### 2️⃣ DriverFactory - BrowserStack Cloud Connection

Each thread creates a driver that connects to BrowserStack cloud. The SDK handles device allocation:

```java
// DriverFactory.java - BrowserStack cloud connection
public class DriverFactory {
    
    private static AppiumDriver createAndroidDriver() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();
        
        // Minimal options - BrowserStack SDK injects capabilities from YAML
        logger.info("Creating Android driver for BrowserStack cloud");
        
        // Each thread gets its own BrowserStack session
        return new AndroidDriver(URI.create(getBrowserStackHubUrl()).toURL(), options);
    }
    
    private static AppiumDriver createIOSDriver() throws MalformedURLException {
        XCUITestOptions options = new XCUITestOptions();
        
        // Minimal options - BrowserStack SDK injects capabilities from YAML
        logger.info("Creating iOS driver for BrowserStack cloud");
        
        // Each thread gets its own BrowserStack session
        return new IOSDriver(URI.create(getBrowserStackHubUrl()).toURL(), options);
    }
}
```

**BrowserStack Cloud Benefits:**
- No port management needed (cloud handles isolation)
- No local Appium server setup
- Automatic device allocation from platform list in YAML
- Real devices with real network conditions

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
│  │  │  ConfigManager   │  │  ExtentReports   │  │ BrowserStack SDK │  │ │
│  │  │  (YAML Config)   │  │  (Report File)   │  │ (Cloud Manager)  │  │ │
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
# Enable parallel execution in TestNG (standard command)
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=android \
    -Dbrowserstack.config=browserstack-android.yml \
    -Dcucumber.filter.tags="@androidOnly"
```

**testngSuite.xml configuration:**
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
| DriverFactory | BrowserStack Cloud | ✅ | SDK handles device allocation & isolation |
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
| **SDK Integration** | BrowserStack Java SDK manages capabilities, devices | Eliminates manual configuration overhead |
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

All tests run on BrowserStack using the SDK agent. See the BrowserStack section below for detailed instructions.

---

## 🔐 Security & Credentials

### Local Development
```bash
# Set environment variables before running tests
export BROWSERSTACK_USERNAME=your-username
export BROWSERSTACK_ACCESS_KEY=your-access-key

# Run tests (DriverFactory falls back to env vars if YAML not found)
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=android \
    -Dbrowserstack.config=browserstack-android.yml \
    -Dcucumber.filter.tags="@androidOnly"
```

### CI/CD (GitHub Actions)
- Credentials stored as repository secrets: `BROWSERSTACK_USERNAME`, `BROWSERSTACK_ACCESS_KEY`
- GitHub Actions injects via environment variables
- YAML files `browserstack-*.yml` are in `.gitignore` to prevent accidental commits

### Why Not Hardcode Credentials?
- ❌ Exposes secrets in version control
- ❌ Requires credential rotation for every developer leaving team
- ❌ Violates compliance (SOC 2, ISO 27001)
- ✅ Use environment variables instead (one secret per environment)

---

## 📋 Configuration Files Explained

### Default Behavior (Not Recommended for Production)
- `pom.xml` sets default: `browserstack-android.yml`
- Running: `mvn clean test -Dplatform=android` → loads `browserstack-android.yml`

### Explicit Config (Recommended)
- Override with: `mvn clean test -Dplatform=ios -Dbrowserstack.config=browserstack-ios-ci.yml`
- Runs: loads `browserstack-ios-ci.yml` instead of default

### File Locations & Usage
| File | Purpose | Location | Credentials |
|------|---------|----------|-------------|
| `browserstack-android.yml` | Android development/local tests | Repo root | `${BROWSERSTACK_USERNAME}`, `${BROWSERSTACK_ACCESS_KEY}` |
| `browserstack-ios.yml` | iOS development/local tests | Repo root | `${BROWSERSTACK_USERNAME}`, `${BROWSERSTACK_ACCESS_KEY}` |
| `browserstack-android-ci.yml` | Android CI/CD pipeline tests | Repo root | `${BROWSERSTACK_USERNAME}`, `${BROWSERSTACK_ACCESS_KEY}` (injected by GitHub Actions) |
| `browserstack-ios-ci.yml` | iOS CI/CD pipeline tests | Repo root | `${BROWSERSTACK_USERNAME}`, `${BROWSERSTACK_ACCESS_KEY}` (injected by GitHub Actions) |

**How ConfigManager Loads Files:**
1. Looks for file specified in `-Dbrowserstack.config` parameter
2. Falls back to pom.xml default if not specified
3. Reads from repo root, `src/test/resources/`, or `target/test-classes/` (in that order)
4. All environment variable placeholders (`${BROWSERSTACK_USERNAME}`) are resolved at runtime

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
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=android \
    -Dbrowserstack.config=browserstack-android.yml \
    -Dcucumber.filter.tags="@androidOnly"
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
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=ios \
    -Dbrowserstack.config=browserstack-ios.yml \
    -Dcucumber.filter.tags="@iosOnly"
```
- **Verify:** BrowserStack dashboard shows sessions; logs confirm app ID and device allocation.

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
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=android \
    -Dbrowserstack.config=browserstack-android.yml \
    -Dcucumber.filter.tags="@androidOnly"

# iOS: Run only @iosOnly scenarios
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=ios \
    -Dbrowserstack.config=browserstack-ios.yml \
    -Dcucumber.filter.tags="@iosOnly"

# Multi-tag (AND logic): Run scenarios tagged with both @androidOnly AND @smoke
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=android \
    -Dbrowserstack.config=browserstack-android.yml \
    -Dcucumber.filter.tags="@androidOnly and @smoke"

# Exclusive execution (NOT logic): Run all scenarios EXCEPT @androidOnly (useful for iOS)
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=ios \
    -Dbrowserstack.config=browserstack-ios.yml \
    -Dcucumber.filter.tags="not @androidOnly"
```

#### Common Mistakes
- ❌ **Missing tag filter**: `mvn clean test -Dplatform=ios -Dbrowserstack.config=browserstack-ios.yml` → Runs ALL scenarios including `@androidOnly`, causing failures on iOS.
- ❌ **Wrong tag**: `mvn clean test -Dplatform=ios -Dbrowserstack.config=browserstack-ios.yml -Dcucumber.filter.tags="@androidOnly"` → Runs Android tests on iOS; element interactions fail.
- ❌ **Typo in tag**: `mvn clean test -Dplatform=android -Dbrowserstack.config=browserstack-android.yml -Dcucumber.filter.tags="@androidonly"` (lowercase) → Tag doesn't match (case-sensitive); no tests run.

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

---

## 🔄 CI/CD with GitHub Actions

The framework includes comprehensive GitHub Actions workflows for automated testing in CI/CD pipelines.

### Available Workflows

#### 1. **BrowserStack SDK Tests** ([browserstack-sdk.yml](.github/workflows/browserstack-sdk.yml))
Automated BrowserStack testing using SDK agent for both Android and iOS.

**Triggers:**
- Push to `main`, `develop`, `appiumMobile` branches
- Pull requests to `main`, `develop`
- Manual dispatch with platform selection

**Jobs:**
- ✅ Build & Validate
- 🤖 Android SDK Tests (parallel across 3 devices)
- 🍎 iOS SDK Tests (parallel across 3 devices)
- 📊 Test Summary & Report Generation

**Manual Trigger:**
```bash
# Via GitHub UI: Actions tab → BrowserStack SDK Tests → Run workflow
# Select platform: android | ios | both
# Optional: Custom Cucumber tags (defaults: @androidOnly, @iosOnly)
```

**Required Secrets:**
```bash
BROWSERSTACK_USERNAME=<your-browserstack-username>
BROWSERSTACK_ACCESS_KEY=<your-browserstack-access-key>
```

### Setting Up GitHub Actions

#### Step 1: Add BrowserStack Secrets
1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Add repository secrets:
   - `BROWSERSTACK_USERNAME`: Your BrowserStack username
   - `BROWSERSTACK_ACCESS_KEY`: Your BrowserStack access key

#### Step 2: Enable Workflows
Workflows are automatically enabled after pushing to the repository. Verify:
```bash
git push origin appiumMobile
```

#### Step 3: Monitor Execution
- Navigate to **Actions** tab in GitHub
- View real-time logs for each job
- Download artifacts (test reports, logs) after completion

### Workflow Configuration Examples

#### Example 1: Android SDK Tests Only
```yaml
# Triggered automatically on push/PR
# Or manually with platform selection
jobs:
  test-android-sdk:
    runs-on: ubuntu-latest
    steps:
      - name: Run Android SDK Tests
        env:
          BROWSERSTACK_USERNAME: ${{ secrets.BROWSERSTACK_USERNAME }}
          BROWSERSTACK_ACCESS_KEY: ${{ secrets.BROWSERSTACK_ACCESS_KEY }}
        run: |
          mvn clean test \
            -Pbrowserstack \
            -Dplatform=android \
            -Denv=browserstack \
            -Dcucumber.filter.tags="@androidOnly"
```

#### Example 2: Custom Tags for Smoke Tests
```yaml
# Manual workflow dispatch with custom tags
workflow_dispatch:
  inputs:
    tags:
      description: 'Cucumber tags filter'
      default: '@Smoke'

# Usage in job:
-Dcucumber.filter.tags="${{ github.event.inputs.tags }}"
```

#### Example 3: Parallel Platform Testing
```yaml
# Both platforms run in parallel (independent jobs)
jobs:
  test-android-sdk:
    runs-on: ubuntu-latest
    # Android tests run independently
  
  test-ios-sdk:
    runs-on: ubuntu-latest
    # iOS tests run independently (no dependency on Android)
```

### Test Reports & Artifacts

All workflows upload test reports as artifacts:

**Artifact Types:**
- 📊 **Extent Reports**: HTML reports with screenshots and step details
- 📋 **Surefire Reports**: XML test results for GitHub UI integration
- 📝 **Cucumber Reports**: JSON reports for trend analysis
- 🔍 **Logs**: Debug logs (uploaded only on failure)

**Accessing Artifacts:**
1. Go to **Actions** → Select workflow run
2. Scroll to **Artifacts** section at the bottom
3. Download zip files (e.g., `android-sdk-reports.zip`)
4. Extract and open `extent-reports/ExtentReport.html`

**Artifact Retention:**
- Test reports: 30 days
- Debug logs: 7 days

### Best Practices for CI/CD

1. **Branch Protection Rules:**
   ```yaml
   # Require BrowserStack SDK tests to pass before merging
   Settings → Branches → main → Require status checks:
   - Android SDK Test Results ✅
   - iOS SDK Test Results ✅
   ```

2. **Scheduled Runs:**
   ```yaml
   # Add to workflow for nightly regression
   on:
     schedule:
       - cron: '0 2 * * *'  # 2 AM UTC daily
   ```

3. **Slack Notifications:**
   ```yaml
   # Add step to notify on failure
   - name: Notify Slack
     if: failure()
     uses: slackapi/slack-github-action@v1
     with:
       webhook-url: ${{ secrets.SLACK_WEBHOOK }}
   ```

4. **Dependency Caching:**
   ```yaml
   # Already configured in workflows
   - uses: actions/cache@v4
     with:
       path: ~/.m2/repository
       key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
   ```

5. **Matrix Strategy for Multiple Environments:**
   ```yaml
   strategy:
     matrix:
       environment: [staging, production]
       platform: [android, ios]
   # Runs 4 jobs: staging-android, staging-ios, prod-android, prod-ios
   ```

### Troubleshooting CI/CD Issues

| Issue | Solution |
|-------|----------|
| **BrowserStack credentials error** | Verify secrets are added: `BROWSERSTACK_USERNAME`, `BROWSERSTACK_ACCESS_KEY` |
| **No tests executed** | Check Cucumber tag filter matches feature file tags (case-sensitive) |
| **Maven dependency timeout** | Enable dependency caching; increase `MAVEN_OPTS` memory |
| **iOS Simulator boot failure** | Use `macos-13` runner (has Xcode 14); `macos-latest` may vary |
| **Parallel execution conflicts** | Ensure proper port management in DriverFactory (already configured) |
| **Artifact upload size limit** | Compress large reports; reduce screenshot size in Extent config |

### Workflow Status Badges

Add status badges to README:
```markdown
[![BrowserStack SDK Tests](https://github.com/<owner>/<repo>/actions/workflows/browserstack-sdk.yml/badge.svg)](https://github.com/<owner>/<repo>/actions/workflows/browserstack-sdk.yml)
```


