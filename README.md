# Mobile Automation BDD Framework

## 🚀 What’s new (Dec 2025)
- BrowserStack-only: all local/emulator support removed.
- Capabilities injected by BrowserStack SDK from YAML (no manual capability setting in code).
- ConfigManager loads raw BrowserStack YAML only; no properties files.
- `browserstack.local` support removed; only cloud device runs.
- Waits configured via system property `implicitWait` or defaults; YAML does not include waits.

## 🚀 What’s new (Dec 2025)
## 🧪 Test Execution
- Default TestNG suite: `testngSuite.xml` (renamed from testngParallel.xml)
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
- Waits: configure via system property `implicitWait` or code defaults; do not include waits in YAML.
- `browserstack.local` is not used.
│       ├── pages/
│       │   ├── BasePage.java               # Base class with waits/helpers
### Running Parallel Tests

```bash
# Enable parallel execution (suite controls parallelism)
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=android \
    -Dbrowserstack.config=browserstack-android.yml \
    -Dcucumber.filter.tags="@androidOnly"
```

If you maintain a parallel TestNG suite, ensure the file is `testngSuite.xml` and includes your desired `parallel`/`thread-count` settings.
- Default TestNG suite: `testngSuite.xml` (renamed from testngParallel.xml)
- Android (tag filter required):
### Android
...
- **Run:**
```bash
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=android \
    -Dbrowserstack.config=browserstack-android.yml \
    -Dcucumber.filter.tags="@androidOnly"
```
mvn clean test -DsuiteXmlFile=testngSuite.xml -Dplatform=ios -Denv=browserstack -Dcucumber.filter.tags="@iosOnly"
```
iOS
...
- **Run:**
```bash
mvn clean test \
    -DsuiteXmlFile=testngSuite.xml \
    -Dplatform=ios \
    -Dbrowserstack.config=browserstack-ios.yml \
    -Dcucumber.filter.tags="@iosOnly"
```
- YAML lives at repo root; SDK injects capabilities directly. No config.properties files.
- Waits: set `frameworkOptions.implicitWait` and `frameworkOptions.explicitWait` in YAML; defaults exist in code.
#### Commands with Proper Filtering
```bash
# Android: Run only @androidOnly scenarios
mvn clean test -DsuiteXmlFile=testngSuite.xml -Dplatform=android -Dbrowserstack.config=browserstack-android.yml -Dcucumber.filter.tags="@androidOnly"

# iOS: Run only @iosOnly scenarios
mvn clean test -DsuiteXmlFile=testngSuite.xml -Dplatform=ios -Dbrowserstack.config=browserstack-ios.yml -Dcucumber.filter.tags="@iosOnly"

# Multi-tag (AND logic): Run scenarios tagged with both @androidOnly AND @smoke
mvn clean test -DsuiteXmlFile=testngSuite.xml -Dplatform=android -Dbrowserstack.config=browserstack-android.yml -Dcucumber.filter.tags="@androidOnly and @smoke"

# Exclusive execution (NOT logic): Run all scenarios EXCEPT @androidOnly (useful for iOS)
mvn clean test -DsuiteXmlFile=testngSuite.xml -Dplatform=ios -Dbrowserstack.config=browserstack-ios.yml -Dcucumber.filter.tags="not @androidOnly"
```
- ❌ **Missing tag filter**: `mvn clean test -Dplatform=ios -Dbrowserstack.config=browserstack-ios.yml` → Runs ALL scenarios including `@androidOnly`, causing failures on iOS.
- ❌ **Wrong tag**: `mvn clean test -Dplatform=ios -Dbrowserstack.config=browserstack-ios.yml -Dcucumber.filter.tags="@androidOnly"` → Runs Android tests on iOS; element interactions fail.
- ❌ **Typo in tag**: `mvn clean test -Dplatform=android -Dbrowserstack.config=browserstack-android.yml -Dcucumber.filter.tags="@androidonly"` (lowercase) → Tag doesn't match (case-sensitive); no tests run.
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

All tests run on BrowserStack using the SDK agent. See the BrowserStack section below for detailed instructions.

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


