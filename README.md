# Mobile Automation BDD Framework

Enterprise-grade BDD test automation framework for mobile applications using Appium, Cucumber, and TestNG. Supports both **Android** and **iOS** platforms with a clean, modular architecture.

## 🏗️ Architecture

```
src/main/
├── java/
│   ├── com/automation/framework/     # Core Framework
│   │   ├── core/
│   │   │   ├── ConfigManager.java    # Centralized configuration management
│   │   │   ├── DriverFactory.java    # Platform-specific driver creation
│   │   │   └── DriverManager.java    # Thread-safe driver management
│   │   └── pages/
│   │       └── BasePage.java         # Base class for all page objects
│   │
│   ├── pages/                         # Page Objects & Step Definitions
│   │   ├── Hooks.java                 # Cucumber Before/After hooks
│   │   ├── WdioLoginPage.java         # Step definitions (extends LoginScreen)
│   │   ├── locators/
│   │   │   └── WdioLocators.java      # Locator constants
│   │   └── screens/
│   │       └── LoginScreen.java       # Page object (extends BasePage)
│   │
│   ├── features/                      # Cucumber feature files
│   │   └── *.feature
│   │
│   └── runner/
│       └── TestNgRunner.java          # TestNG Cucumber runner
│
├── resources/
│   └── config.properties              # Single configuration file
│
└── app/                               # Mobile applications
    ├── android.wdio.native.app.v1.0.8.apk
    └── wdiodemoapp.app
```

## 🎯 Key Design Patterns

| Pattern | Implementation | Benefit |
|---------|---------------|---------|
| **Page Object Model** | `BasePage` → `LoginScreen` → `WdioLoginPage` | Separation of concerns |
| **Factory Pattern** | `DriverFactory` | Platform-agnostic driver creation |
| **Singleton Pattern** | `ConfigManager` | Centralized configuration |
| **Thread-Local** | `DriverManager` | Safe parallel execution |

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- Appium Server 2.x running (`appium`)
- Android Emulator or iOS Simulator

### Installation

```bash
# Clone the repository
git clone <repository-url>

# Install dependencies
mvn clean install -DskipTests
```

### Running Tests

```bash
# Run with platform from config.properties (default)
mvn clean verify -Dsurefire.suiteXmlFiles=testngParallel.xml

# Run on Android (override config)
mvn clean verify -Dsurefire.suiteXmlFiles=testngParallel.xml -Dplatform=android

# Run on iOS (override config)
mvn clean verify -Dsurefire.suiteXmlFiles=testngParallel.xml -Dplatform=ios

# Run specific Cucumber tag
mvn test -Dcucumber.filter.tags="@Smoke"
```

## ⚙️ Configuration

### config.properties
Single configuration file for all settings. Platform is determined by `platform` property.

```properties
# Platform: android or ios
platform=android

# Appium Server
HUB=http://127.0.0.1:4723

# iOS Configuration
iosDeviceName=iPhone 16e
iosUdid=473154E6-C7B8-494F-A943-CAE9B3033BCC
iosPlatformVersion=26.2
iosAppPath=src/app/wdiodemoapp.app
bundleId=org.reactjs.native.example.wdiodemoapp

# Android Configuration
deviceName=emulator-5554
udid=emulator-5554
appPackage=com.wdiodemoapp
appActivity=com.wdiodemoapp.MainActivity
apkPath=src/app/android.wdio.native.app.v1.0.8.apk

# Timeouts (seconds)
implicitWait=30
explicitWait=10
```

### Platform Priority
Configuration values are resolved in this order:
1. **System Property** (`-Dplatform=ios`) - for CI/command line
2. **config.properties** - default settings
3. **Default values** - fallback

## 📁 Page Object Structure

### Three-Layer Architecture

```
WdioLocators.java          → Locator constants (accessibility IDs, XPaths)
        ↓
LoginScreen.java           → Page actions (extends BasePage)
        ↓
WdioLoginPage.java         → Cucumber step definitions (extends LoginScreen)
```

### Example Usage

```java
// WdioLocators.java - Locator constants
public class WdioLocators {
    public static final String LOGIN_MENU = "Login";
    public static final String EMAIL_INPUT = "input-email";
    public static final String PASSWORD_INPUT = "input-password";
    public static final String LOGIN_BUTTON = "button-LOGIN";
}

// LoginScreen.java - Page actions
public class LoginScreen extends BasePage {
    public void enterEmail(String email) {
        enterByAccessibility(WdioLocators.EMAIL_INPUT, email);
    }
}

// WdioLoginPage.java - Step definitions
public class WdioLoginPage extends LoginScreen {
    @When("I enter email {string}")
    public void enterEmailStep(String email) {
        enterEmail(email);
        reportStep("PASS", "Entered email: " + email);
    }
}
```

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

## 📊 Reporting

### Extent Reports
Rich HTML reports generated at: `target/extent-reports/TestReport_<timestamp>.html`

Features:
- 📸 Screenshots on each step
- ✅ Pass/Fail status with detailed logs
- 📱 Platform info (Android/iOS)
- ⏱️ Execution timestamps
- 🎨 Clean, modern UI

### Cucumber Reports
JSON/HTML reports at: `target/cucumber-reports/`

Features:
- Step-by-step execution details
- Embedded screenshots
- JSON output for CI integration

## 📝 Core Classes

### ConfigManager
Centralized configuration loading from `config.properties`:
```java
String platform = ConfigManager.getPlatform();      // "android" or "ios"
String value = ConfigManager.get("appPackage");     // Get any property
int timeout = ConfigManager.getInt("implicitWait", 30);  // With default
```

### DriverFactory
Creates platform-specific drivers:
```java
AppiumDriver driver = DriverFactory.createDriver();  // Uses ConfigManager.getPlatform()
AppiumDriver driver = DriverFactory.createDriver(Platform.IOS);  // Explicit platform
```

### DriverManager
Thread-safe driver management:
```java
DriverManager.setDriver(driver);
AppiumDriver driver = DriverManager.getDriver();
DriverManager.quitDriver();
```

### BasePage
Base class with common mobile actions:
```java
public class LoginScreen extends BasePage {
    // Inherited methods:
    // - clickByAccessibility(String accessibilityId)
    // - enterByAccessibility(String accessibilityId, String text)
    // - reportStep(String status, String message)
    // - getDriver()
}
```

## ✅ Best Practices Implemented

1. **Clean Architecture**: Core framework separated from test code
2. **Page Object Model**: Three-layer structure (Locators → Screens → Steps)
3. **Single Configuration**: One `config.properties` file for all settings
4. **Cross-Platform Support**: Same tests run on Android and iOS
5. **Thread-Safe Execution**: `ThreadLocal` driver management for parallel tests
6. **Relative Paths**: App paths resolved relative to project root
7. **Dynamic Ports**: Avoids port conflicts in parallel execution

## 🔧 Extending the Framework

### Adding a New Screen

1. **Create Locators** (`pages/locators/NewLocators.java`):
```java
public class NewLocators {
    public static final String ELEMENT_NAME = "accessibility-id";
}
```

2. **Create Screen** (`pages/screens/NewScreen.java`):
```java
public class NewScreen extends BasePage {
    public void performAction() {
        clickByAccessibility(NewLocators.ELEMENT_NAME);
    }
}
```

3. **Create Step Definitions** (`pages/NewPage.java`):
```java
public class NewPage extends NewScreen {
    @When("I perform action")
    public void iPerformAction() {
        performAction();
        reportStep("PASS", "Action performed");
    }
}
```

## 📄 License

This project is proprietary and confidential.

