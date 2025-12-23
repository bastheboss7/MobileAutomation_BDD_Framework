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
│       │   ├── locators/
│       │   │   └── WdioLocators.java       # Centralized locator constants
│       │   └── screens/
│       │       └── LoginScreen.java        # Page object implementation
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

## 🎯 Key Design Patterns

| Pattern | Implementation | Benefit |
|---------|---------------|---------|
| **Page Object Model** | `BasePage` → `LoginScreen` → Step Definitions | Separation of concerns |
| **Factory Pattern** | `DriverFactory` with atomic port allocation | Platform-agnostic, thread-safe driver creation |
| **Singleton Pattern** | `ConfigManager` with environment layering | Centralized multi-env configuration |
| **Thread-Local** | `DriverManager` | Safe parallel execution |
| **Strategy Pattern** | Element-based assertions with fallbacks | Reliable mobile element detection |

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
iosAppPath=src/app/wdiodemoapp.app
bundleId=org.reactjs.native.example.wdiodemoapp

# Android Configuration
deviceName=emulator-5554
udid=emulator-5554
platformVersion=13
appPackage=com.wdiodemoapp
appActivity=com.wdiodemoapp.MainActivity
apkPath=src/app/android.wdio.native.app.v1.0.8.apk

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

