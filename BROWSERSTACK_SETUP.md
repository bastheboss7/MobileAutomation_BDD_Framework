# Running Tests - Configuration Strategy

## Configuration Overview

| Config File | Environment | Triggered From | Appium Server | Purpose |
|------------|-------------|----------------|---------------|---------|
| `config-local.properties` | Local | Developer machine | Local Appium (localhost:4723) | Local development & debugging |
| `config-staging.properties` | BrowserStack Staging | Developer machine | BrowserStack Cloud | Manual BrowserStack testing from local |
| `config-prod.properties` | BrowserStack Production | CI/CD Pipeline | BrowserStack Cloud | Automated production testing in GitHub Actions |

---

## Usage

### 1. Local Development (Developer Machine)
```bash
# Start Appium locally
appium

# Run tests on local Appium
mvn clean test -Dplatform=android -Denv=local
```
**Use Case**: Quick feedback during development, debugging

---

### 2. BrowserStack Staging (Developer Machine)
```bash
# Run on BrowserStack from your local machine
mvn clean test -Dplatform=android -Denv=staging
```
**Use Case**: 
- Test on real devices before committing
- Verify cross-device compatibility manually
- Debug BrowserStack-specific issues locally

---

### 3. BrowserStack Production (CI/CD Pipeline)
```bash
# Triggered automatically by GitHub Actions
mvn clean test -Dplatform=android -Denv=prod
```
**Use Case**:
- Automated testing on every PR/merge
- Production-grade device testing
- Continuous integration validation

---

## Developer Workflow

### Daily Development
```bash
# 1. Develop locally with fast feedback
mvn clean test -Dplatform=android -Denv=local

# 2. Before committing, test on BrowserStack staging
mvn clean test -Dplatform=android -Denv=staging

# 3. Commit & push (triggers prod tests automatically in CI/CD)
git commit -m "feature: new login flow"
git push
```

---

## Configuration Details

### Local (`config-local.properties`)
```properties
# Local Appium server
HUB=http://127.0.0.1:4723
PORT=8203

# Apps from local folder
iosAppPath=./app/wdiodemoapp.app
apkPath=./app/android.wdio.native.app.v1.0.8.apk

# Local devices/emulators
deviceName=emulator-5554
iosDeviceName=iPhone 16e
```
**Requirements**: 
- Appium server running locally
- Emulator/Simulator running

---

### Staging (`config-staging.properties`)
```properties
# BrowserStack cloud
HUB=https://hub-cloud.browserstack.com/wd/hub
PORT=443

# Apps auto-uploaded to BrowserStack
iosAppPath=./app/wdiodemoapp.app
apkPath=./app/android.wdio.native.app.v1.0.8.apk

# BrowserStack devices
deviceName=Samsung Galaxy S23 Ultra
iosDeviceName=iPhone 15 Pro
platformVersion=13.0
```
**Requirements**:
- Valid BrowserStack credentials in `browserstack.yml`
- Internet connection

**Triggered By**: Developer manually from local machine

---

### Production (`config-prod.properties`)
```properties
# BrowserStack cloud
HUB=https://hub-cloud.browserstack.com/wd/hub
PORT=443

# Apps auto-uploaded to BrowserStack
iosAppPath=./app/wdiodemoapp.app
apkPath=./app/android.wdio.native.app.v1.0.8.apk

# Production-grade devices
deviceName=Samsung Galaxy S24
iosDeviceName=iPhone 15 Pro Max
platformVersion=14
```
**Requirements**:
- Valid BrowserStack credentials (from CI/CD secrets)
- Configured in GitHub Actions workflow

**Triggered By**: GitHub Actions workflow (automated)

---

## GitHub Actions Integration

### Workflow File (`.github/workflows/mobile-tests.yml`)

```yaml
name: Mobile Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  browserstack-tests:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          
      - name: Run BrowserStack Tests
        env:
          BROWSERSTACK_USERNAME: ${{ secrets.BROWSERSTACK_USERNAME }}
          BROWSERSTACK_ACCESS_KEY: ${{ secrets.BROWSERSTACK_ACCESS_KEY }}
        run: |
          # Uses config-prod.properties
          mvn clean test -Dplatform=android -Denv=prod
```

---

## BrowserStack Credentials

### For Local Testing (Staging)
Credentials stored in `browserstack.yml`:
```yaml
userName: baskarp_lopTwX
accessKey: mFgqaySZL9yoy9fMdxPi
```

### For CI/CD (Production)
Credentials stored in GitHub Secrets:
- `BROWSERSTACK_USERNAME`
- `BROWSERSTACK_ACCESS_KEY`

The BrowserStack SDK automatically picks up credentials from:
1. `browserstack.yml` (for local)
2. Environment variables (for CI/CD)

---

## Complete Examples

### Example 1: Local Development
```bash
# Start Appium
appium

# Start emulator
emulator -avd Pixel_7_API_33

# Run tests locally
mvn clean test -Dplatform=android -Denv=local
```

### Example 2: Manual BrowserStack Testing (Before Commit)
```bash
# No setup needed - just run!
mvn clean test -Dplatform=android -Denv=staging

# Or with specific tags
mvn clean test -Dplatform=android -Denv=staging -Dcucumber.filter.tags="@Smoke"
```

### Example 3: CI/CD Pipeline (Automatic)
```yaml
# In .github/workflows/mobile-tests.yml
- name: Run Production Tests
  run: mvn clean test -Dplatform=android -Denv=prod
```

---

## Summary

✅ **Three distinct environments, three distinct purposes**:

1. **`local`** → Fast local development with Appium
2. **`staging`** → Manual BrowserStack testing from developer machine
3. **`prod`** → Automated BrowserStack testing in CI/CD pipeline

✅ **Same command structure** for all:
```bash
mvn clean test -Dplatform=<platform> -Denv=<environment>
```

✅ **Clear separation** between local development, manual cloud testing, and automated CI/CD testing

This gives you a complete testing strategy from development to production! 🎯
