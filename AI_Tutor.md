# 🤖 AI Prompts for Framework Understanding

This document contains curated AI prompts to help automation engineers understand and work with this Mobile Automation BDD Framework.

---

## 📚 Prompt: Framework Walkthrough for Engineers

**Copy and paste this prompt into your AI assistant (GitHub Copilot, ChatGPT, Claude) for a comprehensive framework explanation:**

---

### **PROMPT:**

```
Role: You are a Lead Automation Architect with 15+ years of experience in test automation, mobile testing, and enterprise frameworks. You are conducting an onboarding session for beginner/intermediate automation engineers joining the team.

Context: Explain the following Mobile Automation BDD Framework in a structured, educational manner. Use simple language, real-world analogies, and practical examples.

Framework Overview:
- Language: Java 21
- Build Tool: Maven
- Test Framework: TestNG + Cucumber BDD
- Mobile Automation: Appium (Android & iOS)
- Reporting: Extent Reports + Cucumber HTML Reports
- Design Patterns: Page Object Model, Factory Pattern, Singleton, ThreadLocal
- CI/CD: GitHub Actions, Azure DevOps, Docker

Project Structure (Maven Standard Layout):
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
│   ├── config-staging.properties           # Staging environment
│   ├── config-prod.properties              # Production environment
│   └── logback.xml                         # Logging configuration
│
├── test/java/                              # Test Code
│   ├── listeners/
│   │   ├── ExtentReportListener.java       # Report lifecycle
│   │   ├── RetryAnalyzer.java              # Retry failed tests
│   │   └── RetryTransformer.java           # Auto-apply retry
│   ├── runner/
│   │   └── TestNgRunner.java               # Cucumber-TestNG runner
│   └── stepdefinitions/
│       ├── Hooks.java                      # Cucumber hooks
│       └── WdioLoginSteps.java             # Step definitions
│
└── test/resources/
    └── features/
        └── wdioLogin.feature               # BDD feature files

Teaching Objectives - Cover these topics in order:

1. **What is BDD and Why Use It?**
   - Explain Behavior-Driven Development in simple terms
   - Given-When-Then format with real examples
   - How it bridges the gap between business and technical teams

2. **Understanding the Page Object Model (POM)**
   - Why we separate locators from test logic
   - The inheritance hierarchy: BasePage → LoginScreen
   - How it makes tests maintainable

3. **The Factory Pattern in Action**
   - What DriverFactory does and why
   - How it creates Android vs iOS drivers
   - Atomic port allocation for parallel execution
   - Real-world analogy: A car factory that builds different models

4. **Thread Safety with ThreadLocal**
   - Why DriverManager uses ThreadLocal
   - Parallel execution challenges
   - Simple analogy: Each thread has its own "locker"

5. **Multi-Environment Configuration**
   - How ConfigManager loads layered properties
   - Priority: System props → config-{env}.properties → config.properties
   - Switching between local/staging/prod

6. **The Test Execution Flow**
   - Trace a test from feature file to execution:
     Feature → Runner → Hooks → Step Definitions → Page Objects → Driver
   - What happens at each stage

7. **Reporting Architecture**
   - Extent Reports vs Cucumber Reports
   - Configurable screenshot strategy
   - How ExtentReportListener manages lifecycle

8. **Retry Mechanism for Mobile Flakiness**
   - Why mobile tests are flaky
   - How RetryAnalyzer and RetryTransformer work
   - Configuration via retry.maxCount

9. **CI/CD Integration**
   - GitHub Actions workflow structure
   - Azure DevOps pipeline configuration
   - Docker Compose for Appium Grid

10. **Practical Exercises for the Engineer**
    - Add a new screen (e.g., HomeScreen)
    - Create a new feature file for Forms screen
    - Run tests on a different platform/environment

11. **Common Pitfalls to Avoid**
    - Hardcoded waits vs explicit waits
    - XPath vs Accessibility IDs
    - Not cleaning up drivers
    - Forgetting to start Appium server

Format Requirements:
- Use headers and bullet points for clarity
- Include code snippets where helpful
- Add "💡 Pro Tip" sections for advanced insights
- Include "⚠️ Common Mistake" warnings
- End with a "🎯 Key Takeaways" summary
- Use emojis sparingly for visual appeal
- Keep explanations beginner-friendly but technically accurate
```

---

## 🏗️ Prompt: Architecture Explainer for Beginners

**Use this prompt to get a beginner-friendly explanation of the framework's smart design decisions with real-world analogies:**

---

### **PROMPT:**

```
You are a friendly, patient Senior QA Architect explaining a mobile automation framework to a complete beginner who just learned Java basics. Your goal is to make complex concepts feel simple and exciting.

Framework: MobileForge (Mobile Automation BDD Framework)
Tech Stack: Java 21, Appium, Cucumber BDD, TestNG

Use these teaching principles:
- Use real-world analogies (restaurants, factories, libraries)
- Explain the "WHY" before the "HOW"
- Build concepts progressively (don't overwhelm)
- Celebrate clever design decisions with "💡 Smart Design" callouts
- Use simple diagrams with ASCII art when helpful

Explain these architectural concepts in order:

---

**1. THE PROBLEM THIS FRAMEWORK SOLVES**
Why can't we just write Appium code directly? What pain points does this framework eliminate?

**2. THE LAYERED ARCHITECTURE (explain like a restaurant)**
Feature Files     →  "Menu" (what customer orders)
Step Definitions  →  "Waiter" (takes order to kitchen)  
Page Objects      →  "Chef" (knows how to make each dish)
Driver Layer      →  "Kitchen Equipment" (actual tools)

**3. SMART DESIGN #1: Factory Pattern (DriverFactory)**
- Analogy: A car factory that builds different car models (Android/iOS) from the same blueprint
- Why it's smart: One method createDriver() handles both platforms
- The atomic port counter trick for parallel execution

**4. SMART DESIGN #2: Singleton Pattern (ConfigManager)**
- Analogy: A library with ONE master catalog that everyone shares
- Why it's smart: Load config once, use everywhere
- The layered config trick (base + environment overrides)

**5. SMART DESIGN #3: Thread-Local Pattern (DriverManager)**
- Analogy: A gym where each person gets their own locker
- Why it's smart: Parallel tests don't fight over the same driver
- How ThreadLocal gives each test its own isolated driver

**6. SMART DESIGN #4: Page Object Model**
- Analogy: A recipe book - change recipe once, all dishes update
- Why it's smart: UI changes only need updates in ONE place
- The inheritance chain: BasePage → LoginScreen → StepDefinitions

**7. SMART DESIGN #5: Hooks with try-finally**
- Analogy: A responsible party host who ALWAYS cleans up
- Why it's smart: Drivers are cleaned up even if tests crash
- Resource leak prevention

**8. THE FLOW OF A TEST (trace one scenario)**
Walk through what happens when this runs:

Scenario: Login with valid credentials
  Given I am on the login screen
  When I enter email "test@example.com"
  And I enter password "Password123"
  And I tap the login button
  Then I should see login success message

Show the journey:
Feature → Runner → Hooks(@Before) → StepDef → PageObject → Driver → App → back up

**9. WHY THIS IS PRODUCTION-READY**
Explain these enterprise features simply:
- Retry mechanism for flaky mobile tests
- Multi-environment config (local/staging/prod)
- Parallel execution support
- Detailed HTML reports with screenshots
- CI/CD ready (GitHub Actions, Azure DevOps)

**10. COMMON BEGINNER MISTAKES TO AVOID**
List 5 things beginners do wrong and how this framework prevents them.

---

Format your response with:
- Headers for each section
- 💡 callouts for clever designs
- ⚠️ warnings for common pitfalls
- Simple code snippets (max 10 lines each)
- ASCII diagrams where helpful
- A "🎯 Key Takeaway" at the end of each section

End with a "🏆 Summary: Why This Framework is Well-Designed" section that a beginner could explain to someone else in 2 minutes.
```

---

## 📱 Prompt: Starting Simulators & Emulators

```
I need help starting mobile simulators/emulators for my Appium tests.

My setup:
- macOS (for both Android and iOS)
- Appium 2.x with uiautomator2 and xcuitest drivers
- Android SDK installed
- Xcode installed

Please provide:
1. Step-by-step commands to list and start Android emulators
2. Step-by-step commands to list and start iOS simulators
3. How to verify the device is ready for Appium
4. How to get the UDID for my config.properties
5. Common troubleshooting for "device not found" errors
```

---

## 🔧 Additional Useful Prompts

### **Prompt: Debug a Test Failure**
```
I'm getting this error when running my Appium test:
[paste error here]

Framework context:
- Using DriverFactory with ThreadLocal
- TestNG + Cucumber integration
- Running on [Android/iOS]
- Environment: [local/staging/prod]

Help me understand:
1. What is the root cause?
2. Which file should I check?
3. How do I fix it?
```

### **Prompt: Add a New Page Object**
```
I need to add a new Page Object for the [Screen Name] screen in this framework.

The screen has these elements:
- [Element 1]: [description]
- [Element 2]: [description]

Following the existing pattern, generate:
1. Locator constants in com.automation.framework.pages.locators
2. Page Object class in com.automation.framework.pages.screens
3. Step definitions in src/test/java/stepdefinitions
4. Feature file in src/test/resources/features
```

### **Prompt: Create a New Feature File**
```
Create a Cucumber BDD feature file for testing [Feature Name].

Requirements:
- Follow Scenario Outline pattern with Examples table
- Use proper tags (@Smoke, @Regression, etc.)
- Include positive and negative test cases
- Match the style of wdioLogin.feature in this project
- Place in src/test/resources/features/
```

### **Prompt: Explain a Specific File**
```
Explain this file from my Mobile Automation Framework like I'm a junior engineer:

[Paste file content here]

Cover:
1. What is its purpose?
2. How does it fit into the overall architecture?
3. Key methods and what they do
4. How other files depend on it
```

### **Prompt: Review My Test Code**
```
Review this test code for best practices:

[Paste your code here]

Check for:
- Page Object Model violations
- Hardcoded values that should be configurable
- Missing error handling
- Thread-safety issues for parallel execution
- Appium best practices
```

### **Prompt: Configure for Cloud Device Farm**
```
I want to run my tests on [BrowserStack/SauceLabs/AWS Device Farm].

Current config:
- Using config-{env}.properties pattern
- Appium 2.x with Java client 9.4.0
- TestNG + Cucumber

Help me:
1. Create a config-cloud.properties file
2. Update DriverFactory to support cloud capabilities
3. Set up CI/CD to use cloud devices
```

---

## 📋 Quick Reference Commands

### Run Tests
```bash
# Run all tests (default platform from config.properties)
mvn clean test

# Run on specific platform
mvn clean test -Dplatform=android
mvn clean test -Dplatform=ios

# Run with specific environment
mvn clean test -Denv=staging
mvn clean test -Denv=prod

# Run specific tags
mvn clean test -Dcucumber.filter.tags="@Smoke"
mvn clean test -Dcucumber.filter.tags="@Login and @Regression"

# Run with custom retry count
mvn clean test -Dretry.maxCount=3
```

### Start Simulators/Emulators
```bash
# Android: List available AVDs
emulator -list-avds

# Android: Start emulator
emulator -avd <avd_name>

# Android: Start headless (for CI)
emulator -avd <avd_name> -no-window -no-audio -gpu swiftshader_indirect

# Android: Check devices
adb devices

# iOS: List simulators
xcrun simctl list devices available

# iOS: Boot simulator
xcrun simctl boot "iPhone 15 Pro"

# iOS: Open Simulator app
open -a Simulator

# iOS: Get booted UDID
xcrun simctl list devices | grep Booted
```

### Appium Commands
```bash
# Start Appium
appium

# Start on custom port
appium --port 4724

# Check status
curl http://127.0.0.1:4723/status

# Install drivers
appium driver install uiautomator2
appium driver install xcuitest

# List installed drivers
appium driver list --installed
```

### Docker Commands
```bash
# Start Appium Grid
docker-compose up -d

# Run tests in container
docker-compose -f docker-compose.test.yml up --abort-on-container-exit

# View VNC (Android emulator)
open http://localhost:6080

# Stop all containers
docker-compose down
```

### Project Locations
| Component | Location |
|-----------|----------|
| Feature files | `src/test/resources/features/` |
| Step Definitions | `src/test/java/stepdefinitions/` |
| Page Objects | `src/main/java/com/automation/framework/pages/screens/` |
| Locators | `src/main/java/com/automation/framework/pages/locators/` |
| Configuration | `src/main/resources/config*.properties` |
| Reports | `target/extent-reports/` |
| Logs | `target/logs/` |
| Cucumber Reports | `target/reports/cucumber-report/` |

---

## 🎯 Learning Path

| Week | Focus Area | Activities |
|------|------------|------------|
| 1 | BDD Fundamentals | Understand Cucumber, run existing tests |
| 2 | Feature Files | Create new scenarios, use Scenario Outlines |
| 3 | Page Objects | Add new screens, locators, step definitions |
| 4 | Configuration | Switch platforms/environments, understand layering |
| 5 | Parallel & CI/CD | Run parallel tests, understand GitHub Actions |
| 6 | Advanced | Docker, cloud device farms, custom reporting |

---

## 🔍 Troubleshooting Prompts

### **Appium Connection Issues**
```
My Appium test fails with "Could not start a new session".

Error message: [paste error]

Setup:
- Appium version: [version]
- Platform: [Android/iOS]
- Device: [emulator name or device]

Help me debug step by step.
```

### **Element Not Found**
```
My test fails with "NoSuchElementException" for element [element name].

I'm using:
- Locator strategy: [accessibility ID/XPath/etc]
- Locator value: [value]
- Platform: [Android/iOS]

The element is visible on screen. Why can't Appium find it?
```

### **Parallel Execution Issues**
```
My tests work individually but fail in parallel execution.

Symptoms:
- [describe what happens]

Framework uses:
- ThreadLocal DriverManager
- Atomic port allocation in DriverFactory

What could be causing thread-safety issues?
```

---

*Created for Mobile Automation BDD Framework v4.0.0*  
*Last updated: December 2025*
