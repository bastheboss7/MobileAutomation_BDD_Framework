# Mobile Automation BDD Framework

Enterprise-grade BDD test automation framework for mobile applications using Appium, Cucumber, and TestNG.

## 🏗️ Architecture

```
src/
├── main/
│   ├── java/
│   │   ├── features/          # Cucumber feature files
│   │   ├── pages/             # Page Objects and Step Definitions
│   │   ├── runner/            # TestNG Cucumber Runner
│   │   ├── utils/             # Utility classes (Reporter, DataProvider)
│   │   └── wrappers/          # Appium/Selenium wrapper methods
│   └── resources/
│       ├── config.properties  # Test configuration
│       └── object.properties  # Element locators
└── test/
    └── resources/
        ├── config/            # Platform-specific configs
        ├── data/              # Test data files
        └── features/          # Additional feature files
```

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- Appium Server 2.x
- Android SDK / Xcode (for iOS)

### Installation

```bash
# Clone the repository
git clone <repository-url>

# Install dependencies
mvn clean install -DskipTests
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific tag
mvn test -Dcucumber.filter.tags="@Smoke"

# Run with Android configuration
mvn test -Dcucumber.filter.tags="@Scenario4" -Dplatform=android
```

## 📊 Reporting

### Extent Reports
Reports are generated at: `target/reports/extent-report/result.html`

Features:
- Screenshots on each step
- Detailed step logging
- HTML dashboard

### Cucumber Reports
Reports are generated at: `target/reports/cucumber-report/cucumber-html-reports/`

## ⚙️ Configuration

### config.properties
```properties
# Appium Server
HUB=http://0.0.0.0:4723/wd/hub

# Device Configuration
deviceName=<device-name>
udid=<device-udid>

# App Configuration
appPackage=<app-package>
appActivity=<app-activity>
```

## 🛠️ Technology Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | Programming Language |
| Appium | 8.5.1 | Mobile Automation |
| Selenium | 4.19.1 | WebDriver |
| Cucumber | 7.15.0 | BDD Framework |
| TestNG | 7.9.0 | Test Runner |
| ExtentReports | 5.1.1 | Reporting |

## 📝 Best Practices

1. **Page Object Model**: All page interactions are encapsulated in page classes
2. **Reusable Wrappers**: Common actions are abstracted in wrapper classes
3. **Data-Driven Testing**: Support for Excel data providers
4. **Parallel Execution**: Configured for parallel test runs via TestNG
5. **Screenshot on Failure**: Automatic screenshot capture on test failures

## 📄 License

This project is proprietary and confidential.
