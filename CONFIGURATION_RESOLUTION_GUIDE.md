# Configuration Resolution Guide

## Overview
This guide explains how `ConfigManager` resolves YAML configuration files in local and CI environments.

## Problem Statement
Previously, the framework failed on GitHub runners with:
```
WARNING: "YAML configuration browserstack-android.yml not found"
ERROR: "BrowserStack rejects the session because no capabilities were provided"
```

**Root Cause**: ConfigManager only looked for YAML files in the current working directory without intelligent path resolution.

---

## Solution: Multi-Path Configuration Resolution

### ConfigManager.java Changes

#### New Method: `resolveConfigFile(String fileName)`
```java
private static java.io.File resolveConfigFile(String fileName) {
    java.util.List<String> searchPaths = java.util.Arrays.asList(
        fileName,                           // 1. Project root (development)
        "src/test/resources/" + fileName,   // 2. Maven test resources (test phase)
        "target/test-classes/" + fileName   // 3. Compiled test classes (post-compile)
    );
    
    for (String path : searchPaths) {
        java.io.File file = new java.io.File(path);
        if (file.exists()) {
            logger.info("✓ Found config file at: {}", file.getAbsolutePath());
            return file;
        } else {
            logger.debug("✗ Config file not found at: {}", file.getAbsolutePath());
        }
    }
    return null;
}
```

#### Search Path Priority
1. **Project Root** (`browserstack-android.yml`)
   - Location: `/Users/baskarp/MobileAutomation_BDD_Framework/browserstack-android.yml`
   - Used for: Local development and testing
   - Populated by: Manually in workspace or pulled from git

2. **Maven Test Resources** (`src/test/resources/browserstack-android.yml`)
   - Location: `/Users/baskarp/MobileAutomation_BDD_Framework/src/test/resources/browserstack-android.yml`
   - Used for: If YAML needs to be bundled with compiled artifacts
   - Populated by: Maven includes this directory in test classpath
   - On GitHub: `${GITHUB_WORKSPACE}/src/test/resources/browserstack-android.yml`

3. **Compiled Test Classes** (`target/test-classes/browserstack-android.yml`)
   - Location: `/Users/baskarp/MobileAutomation_BDD_Framework/target/test-classes/browserstack-android.yml`
   - Used for: After Maven compilation
   - Populated by: Maven copies from `src/test/resources/` during build
   - On GitHub: `${GITHUB_WORKSPACE}/target/test-classes/browserstack-android.yml`

### DriverFactory.java Changes

#### Enhanced Capability Validation
```java
private static void loadBrowserStackCapabilities(BaseOptions<?> options) {
    logger.info("Loading BrowserStack capabilities from YAML...");
    
    // Access 'app' from rawYamlData (not excluded from properties)
    Object appObj = ConfigManager.getRawValue("app");
    String app = appObj != null ? appObj.toString() : null;
    
    // VALIDATION: Throw explicit error if 'app' is missing
    if (app == null || app.isBlank()) {
        throw new IllegalStateException(
            "CRITICAL: 'app' capability is missing from YAML. " +
            "BrowserStack requires an app reference (e.g., bs://... or app/path/to/app.ipa)"
        );
    }
    
    logger.info("✓ Setting app from YAML: {}", app);
    options.setCapability("app", app);
    
    // ... load other capabilities ...
    
    logger.info("✓ BrowserStack capabilities successfully loaded from YAML");
}
```

#### Why `getRawValue()` Instead of `get()`?
The `app` key is **intentionally excluded** from ConfigManager's properties store to prevent the BrowserStack SDK from auto-injecting it into `bstack:options`. Instead:
- `getRawValue("app")` accesses the raw YAML Map (`rawYamlData`)
- Manual capability loading ensures proper control over BrowserStack options
- Environment variable overrides (via placeholders) work correctly in CI

---

## GitHub Actions Workflow Configuration

### Debug Steps Added
The workflow now includes diagnostic steps that display file locations:

```yaml
- name: Debug - Verify Configuration Files
  run: |
    echo "=== Configuration File Debug ==="
    echo "Working Directory: $(pwd)"
    echo ""
    echo "=== Root Level Files ==="
    ls -la browserstack-*.yml* 2>/dev/null || echo "No browserstack files in root"
    echo ""
    echo "=== src/test/resources/ ==="
    ls -la src/test/resources/browserstack-*.yml* 2>/dev/null || echo "No files in src/test/resources"
    echo ""
    echo "=== target/test-classes/ ==="
    ls -la target/test-classes/browserstack-*.yml* 2>/dev/null || echo "No files in target/test-classes"
```

### Expected Output on GitHub Runners
```
=== Configuration File Debug ===
Working Directory: /home/runner/work/MobileAutomation_BDD_Framework/MobileAutomation_BDD_Framework

=== Root Level Files ===
-rw-r--r-- browserstack-android-ci.yml
-rw-r--r-- browserstack-ios-ci.yml

=== src/test/resources/ ===
No files in src/test/resources

=== target/test-classes/ ===
-rw-r--r-- browserstack-android-ci.yml
-rw-r--r-- browserstack-ios-ci.yml
```

---

## Configuration File Strategy

### Local Development
**Files Used**:
- `browserstack-android.yml` (with actual BrowserStack credentials)
- `browserstack-ios.yml` (with actual BrowserStack credentials)

**Credentials**: Embedded in YAML files (not in git, added to `.gitignore`)

**Example Content**:
```yaml
userName: aishwaryalakshmi_flVCjb
accessKey: RsRLTRVEKShpDovu5sgb
app: bs://02d88594d8c7d0ba4cecde791474bbb7cba23f73
...
```

### GitHub Actions CI
**Files Used**:
- `browserstack-android-ci.yml` (with credential placeholders)
- `browserstack-ios-ci.yml` (with credential placeholders)

**Credentials**: From GitHub Secrets via environment variables

**Example Content**:
```yaml
userName: ${BROWSERSTACK_USERNAME}
accessKey: ${BROWSERSTACK_ACCESS_KEY}
app: bs://02d88594d8c7d0ba4cecde791474bbb7cba23f73
...
```

**Workflow Override**:
```yaml
- name: Run Android SDK Tests
  env:
    BROWSERSTACK_USERNAME: ${{ secrets.BROWSERSTACK_USERNAME }}
    BROWSERSTACK_ACCESS_KEY: ${{ secrets.BROWSERSTACK_ACCESS_KEY }}
  run: |
    mvn clean test \
      -Pbrowserstack \
      -Dbrowserstack.config=browserstack-android-ci.yml \
      -Dplatform=android \
      -Denv=browserstack \
      ...
```

---

## Troubleshooting

### Problem: "YAML configuration not found"
**Solution**: Check ConfigManager logs for search paths attempted:
```
✗ Config file not found at: /path/to/browserstack-android.yml
✗ Config file not found at: src/test/resources/browserstack-android.yml
✓ Found config file at: target/test-classes/browserstack-android.yml
```

### Problem: "'app' capability is missing from YAML"
**Solution**: 
1. Verify YAML file is being found (check debug logs)
2. Verify YAML contains `app:` key at root level:
   ```yaml
   app: bs://02d88594d8c7d0ba4cecde791474bbb7cba23f73
   ```
3. Check that ConfigManager.getRawValue("app") returns non-null value

### Problem: Configuration files not found on GitHub Runner
**Solution**: 
1. Check that `-ci.yml` files are committed to repository:
   ```bash
   git ls-files | grep browserstack.*-ci.yml
   ```
2. Verify file exists in checked-out workspace:
   ```bash
   ls -la browserstack-*-ci.yml
   ```
3. Check GitHub Actions debug step output for file locations

### Problem: Credentials not being replaced on GitHub
**Solution**:
1. Verify GitHub Secrets are set:
   - Go to: Settings → Secrets and Variables → Actions
   - Check: `BROWSERSTACK_USERNAME` and `BROWSERSTACK_ACCESS_KEY` exist
2. Verify environment variables are being passed to Maven:
   ```yaml
   env:
     BROWSERSTACK_USERNAME: ${{ secrets.BROWSERSTACK_USERNAME }}
     BROWSERSTACK_ACCESS_KEY: ${{ secrets.BROWSERSTACK_ACCESS_KEY }}
   ```
3. Verify ConfigManager loads env vars:
   ```java
   String username = System.getenv("BROWSERSTACK_USERNAME");  // From GitHub Secrets
   ```

---

## Error Messages & Meanings

### "Found config file at: [path]"
✓ **SUCCESS**: Configuration file located and will be loaded

### "Config file not found at: [path]"
ℹ **INFO**: This search path did not contain the file; will try next path

### "Could not find configuration file [name] in any search path"
✗ **CRITICAL**: File was not found in ANY search location. Check:
- File exists in project
- File name matches (case-sensitive)
- File is not excluded by `.gitignore`
- Correct platform (android vs ios)

### "CRITICAL: 'app' capability is missing from YAML"
✗ **CRITICAL**: YAML was found but lacks `app:` key. Check:
- YAML syntax is valid (use `yamllint`)
- Key spelling: `app:` (lowercase)
- Key exists at root level (not nested)
- YAML is not empty or malformed

---

## Best Practices

### 1. Always Check Debug Logs
When tests fail, review logs for:
```
✓ Found config file at: [path]
✓ BrowserStack capabilities loaded from YAML
✓ Setting app from YAML: [value]
```

### 2. Use Separate `-ci.yml` Files
- Keep local YAML with actual credentials
- Use `-ci.yml` with placeholders for CI
- Prevents accidental credential exposure in git

### 3. Validate YAML Before Committing
```bash
# Install yamllint
brew install yamllint

# Validate syntax
yamllint browserstack-android-ci.yml
```

### 4. Test on GitHub Before Merging
- Push to feature branch
- Trigger GitHub Actions workflow
- Check debug step output
- Verify tests pass before merge

---

## Related Files
- `src/main/java/com/automation/framework/core/ConfigManager.java` - Configuration loading logic
- `src/main/java/com/automation/framework/core/DriverFactory.java` - Driver creation with capability validation
- `.github/workflows/browserstack-sdk.yml` - GitHub Actions workflow with debug steps
- `browserstack-android-ci.yml` - CI configuration template (with placeholders)
- `browserstack-ios-ci.yml` - CI configuration template (with placeholders)
- `.gitignore` - Excludes local YAML files with actual credentials

