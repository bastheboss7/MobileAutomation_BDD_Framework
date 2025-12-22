package wrappers;

import com.google.common.collect.ImmutableMap;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.Reporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic wrapper class providing core Appium/Selenium functionality.
 * Contains reusable methods for mobile automation across Android and iOS platforms.
 * 
 * @author Baskar
 * @version 1.0.0
 */
public class GenericWrappers extends Reporter {
    private static final Logger logger = LoggerFactory.getLogger(GenericWrappers.class);
    // Static ThreadLocal to share driver across all instances in the same thread
    private static final ThreadLocal<AppiumDriver> driverThreadLocal = new ThreadLocal<>();
    public Properties prop;
    public String sHubUrl;
    public String systemPort;
    public String appPackage;
    public String appActivity;
    public String deviceName;
    public String udid;
    public String installApp;
    public WebDriverWait wait;
    public String apkPath;
    public String platform;
    public String iosDeviceName;
    public String iosUdid;
    public String iosPlatformVersion;
    public String iosAppPath;
    public String bundleId;

    public GenericWrappers() {
        prop = new Properties();
        try {
            prop.load(new FileInputStream("./src/main/resources/config.properties"));
            sHubUrl = prop.getProperty("HUB");
            systemPort = prop.getProperty("PORT");
            appActivity = prop.getProperty("appActivity");
            appPackage = prop.getProperty("appPackage");
            deviceName = prop.getProperty("deviceName");
            udid = prop.getProperty("udid");
            apkPath = prop.getProperty("apkPath");
            // Resolve relative path to absolute
            if (apkPath != null && !apkPath.isEmpty() && !apkPath.startsWith("/")) {
                apkPath = System.getProperty("user.dir") + "/" + apkPath;
            }
            installApp = prop.getProperty("installApp");
            platform = prop.getProperty("platform", "android");
            iosDeviceName = prop.getProperty("iosDeviceName");
            iosUdid = prop.getProperty("iosUdid");
            iosPlatformVersion = prop.getProperty("iosPlatformVersion");
            iosAppPath = prop.getProperty("iosAppPath");
            // Resolve relative path to absolute
            if (iosAppPath != null && !iosAppPath.isEmpty() && !iosAppPath.startsWith("/")) {
                iosAppPath = System.getProperty("user.dir") + "/" + iosAppPath;
            }
            bundleId = prop.getProperty("bundleId");
        } catch (IOException e) {
            logger.error("Error loading config.properties", e);
        }
    }

    public AppiumDriver getDriver() {
        return driverThreadLocal.get();
    }
    public void setDriver(AppiumDriver driver) {
        driverThreadLocal.set(driver);
    }
    
    public boolean isIOS() {
        return "ios".equalsIgnoreCase(platform);
    }

    public void loadObjects() {
        prop = new Properties();
        try {
            prop.load(new FileInputStream("src/main/resources/object.properties"));
        } catch (IOException e) {
            logger.error("Error loading object.properties", e);
        }
    }

    public void unloadObjects() {
        prop = null;
    }

    /*
     * This method will launch the browser in local machine and maximise the browser and set the
     * wait for 30 seconds and load the url
     * @author Baskar
     * @param url - The url with http or https
     * @return
     *
     **/
    public synchronized AppiumDriver invokeApp() {
        try {
            if (isIOS()) {
                return invokeIOSApp();
            }
            UiAutomator2Options options = new UiAutomator2Options();
            options.setAppPackage(appPackage);
            options.setAppActivity(appActivity);
            // Set app path to auto-install APK
            if (apkPath != null && !apkPath.isEmpty()) {
                options.setApp(apkPath);
                logger.info("Setting APK path for auto-install: {}", apkPath);
            }
            options.setDeviceName(deviceName);
            options.setAutomationName("UiAutomator2");
            options.setNoReset(false);
            options.setUdid(udid);
            // Use dynamic system port to avoid conflicts
            int dynamicPort = 8200 + (int)(Math.random() * 100);
            options.setSystemPort(dynamicPort);
            logger.info("Using dynamic systemPort: {}", dynamicPort);
            options.setNewCommandTimeout(Duration.ofSeconds(6000));
            
            logger.info("Launching Android app: {} on device: {}", appPackage, deviceName);
            AppiumDriver driver = new AndroidDriver(java.net.URI.create(sHubUrl).toURL(), options);
            setDriver(driver);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
            logger.info("Android app launched successfully");
        } catch (Exception e) {
            logger.error("Error invoking app", e);
        }
        return getDriver();
    }
    
    /**
     * Launch iOS application using XCUITest driver.
     * @return AppiumDriver for iOS
     */
    public synchronized AppiumDriver invokeIOSApp() {
        try {
            XCUITestOptions options = new XCUITestOptions();
            options.setDeviceName(iosDeviceName);
            options.setUdid(iosUdid);
            options.setPlatformVersion(iosPlatformVersion);
            options.setAutomationName("XCUITest");
            options.setNoReset(false);
            options.setNewCommandTimeout(Duration.ofSeconds(6000));
            
            if (bundleId != null && !bundleId.isEmpty()) {
                options.setBundleId(bundleId);
            }
            if (iosAppPath != null && !iosAppPath.isEmpty()) {
                options.setApp(iosAppPath);
            }
            
            logger.info("Launching iOS app: {} on device: {}", bundleId, iosDeviceName);
            AppiumDriver driver = new IOSDriver(java.net.URI.create(sHubUrl).toURL(), options);
            setDriver(driver);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
            logger.info("iOS app launched successfully");
        } catch (Exception e) {
            logger.error("Error invoking iOS app", e);
        }
        return getDriver();
    }

    /**
     * Performs a swipe gesture using W3C Actions API.
     * This replaces the deprecated TouchAction API.
     *
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param endX Ending X coordinate
     * @param endY Ending Y coordinate
     */
    private void performSwipe(int startX, int startY, int endX, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        getDriver().perform(Arrays.asList(swipe));
    }

    /**
     * Scrolls to an element using UiAutomator2 UiScrollable (Android) or swipe gestures (iOS).
     * Uses accessibility id to find the element while scrolling.
     *
     * @param accessibilityId The accessibility id of the element to scroll to
     * @return WebElement if found, null otherwise
     */
    public WebElement scrollToElementByAccessibility(String accessibilityId) {
        try {
            if (isIOS()) {
                // iOS: Try to find element directly, scroll if not found
                try {
                    return getDriver().findElement(AppiumBy.accessibilityId(accessibilityId));
                } catch (NoSuchElementException e) {
                    // Swipe to find element
                    for (int i = 0; i < 5; i++) {
                        Dimension size = getDriver().manage().window().getSize();
                        performSwipe(size.width / 2, (int)(size.height * 0.7), 
                                     size.width / 2, (int)(size.height * 0.3));
                        try {
                            return getDriver().findElement(AppiumBy.accessibilityId(accessibilityId));
                        } catch (NoSuchElementException ignored) {}
                    }
                    return null;
                }
            } else {
                // Android: Use UiScrollable
                String uiSelector = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(\"" + accessibilityId + "\"))";
                return getDriver().findElement(AppiumBy.androidUIAutomator(uiSelector));
            }
        } catch (Exception e) {
            logger.debug("Could not scroll to element with accessibility: " + accessibilityId);
            return null;
        }
    }

    /**
     * Scrolls to an element using UiAutomator2 UiScrollable.
     * Uses text to find the element while scrolling.
     *
     * @param text The text of the element to scroll to
     * @return WebElement if found, null otherwise
     */
    public WebElement scrollToElementByText(String text) {
        try {
            String uiSelector = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(\"" + text + "\"))";
            return getDriver().findElement(AppiumBy.androidUIAutomator(uiSelector));
        } catch (Exception e) {
            logger.debug("Could not scroll to element with text: " + text);
            return null;
        }
    }

    /**
     * Scrolls to an element using UiAutomator2 UiScrollable.
     * Uses resource-id to find the element while scrolling.
     *
     * @param resourceId The resource id of the element to scroll to
     * @return WebElement if found, null otherwise
     */
    public WebElement scrollToElementById(String resourceId) {
        try {
            String uiSelector = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().resourceId(\"" + resourceId + "\"))";
            return getDriver().findElement(AppiumBy.androidUIAutomator(uiSelector));
        } catch (Exception e) {
            logger.debug("Could not scroll to element with id: " + resourceId);
            return null;
        }
    }

    /**
     * Scrolls to an element using UiAutomator2 UiScrollable.
     * Uses text contains to find the element while scrolling.
     *
     * @param partialText Partial text of the element to scroll to
     * @return WebElement if found, null otherwise
     */
    public WebElement scrollToElementByPartialText(String partialText) {
        try {
            String uiSelector = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains(\"" + partialText + "\"))";
            return getDriver().findElement(AppiumBy.androidUIAutomator(uiSelector));
        } catch (Exception e) {
            logger.debug("Could not scroll to element with partial text: " + partialText);
            return null;
        }
    }

    public void waitForElementToBeVisible(WebElement element, long timeoutInSeconds) {
        wait = new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutInSeconds));
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * This method will enter the value to the text field using id attribute to locate
     *
     * @param data - The data to be sent to the webelement
     * @author Baskar
     */
    //
    public void enterByEle(WebElement ele, String data) {
        try {
            ele.sendKeys(data);
            reportStep("The data: " + data + " entered successfully in ele :" + ele, "PASS");
        } catch (NoSuchElementException e) {
            reportStep("The data: " + data + " could not be entered in the ele :" + ele, "FAIL");
        } catch (Exception e) {
            logger.error("Unknown exception occurred while entering data in element", e);
            reportStep("Unknown exception occurred while entering " + data + " in the ele :" + ele, "FAIL");
        }
    }

    public void enterByXpath(String xpath, String data) {
        try {
            getDriver().findElement(By.xpath(xpath)).sendKeys(data);
            reportStep("The data: " + data + " entered successfully using xpath :" + xpath, "PASS");

        } catch (NoSuchElementException e) {
            reportStep("The data: " + data + " could not be entered in the ele :" + xpath, "FAIL");
        } catch (Exception e) {
            logger.error("Unknown exception occurred while entering data using xpath", e);
            reportStep("Unknown exception occurred while entering " + data + " in the ele :" + xpath, "FAIL");
        }
    }

    public void enterByAccessibity(String accessibility, String data) {
        try {
            // First try to scroll to the element
            WebElement element = scrollToElementByAccessibility(accessibility);
            if (element == null) {
                // If scroll didn't find it, try direct find
                element = getDriver().findElement(new AppiumBy.ByAccessibilityId(accessibility));
            }
            element.sendKeys(data);
            reportStep("The data: " + data + " entered successfully using accessibilityId :" + accessibility, "PASS");
        } catch (NoSuchElementException e) {
            reportStep("The data: " + data + " could not be entered in the ele :" + accessibility, "FAIL");
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception occured while entering " + data + " in the ele :" + accessibility, "FAIL");
        }
    }

    public void enterByClass(String className, String data) {
        try {
            // First try to scroll to the element using class name
            WebElement element = null;
            try {
                String uiSelector = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().className(\"" + className + "\"))";
                element = getDriver().findElement(AppiumBy.androidUIAutomator(uiSelector));
            } catch (Exception scrollEx) {
                // If scroll didn't find it, try direct find
                element = getDriver().findElement(By.className(className));
            }
            element.sendKeys(data);
            reportStep("The data: " + data + " entered successfully using className :" + className, "PASS");
        } catch (NoSuchElementException e) {
            reportStep("The data: " + data + " could not be entered in the className :" + className, "FAIL");
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception occured while entering " + data + " in the className :" + className, "FAIL");
        }
    }

    public void clickByEle(WebElement ele) {
        try {
            ele.click();
            reportStep("The element: " + ele + " clicked successfully", "PASS");

        } catch (NoSuchElementException e) {
            reportStep("The element: " + ele + " could not be clicked", "FAIL");
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception occured while clicking " + ele + "", "FAIL");
        }

    }

    public void clickByAccessibility(String accessibility) {
        try {
            // First try to scroll to the element
            WebElement element = scrollToElementByAccessibility(accessibility);
            if (element == null) {
                // If scroll didn't find it, try direct find
                element = getDriver().findElement(new AppiumBy.ByAccessibilityId(accessibility));
            }
            element.click();
            reportStep("The element with accessibility: " + accessibility + " clicked successfully", "PASS");
        } catch (NoSuchElementException e) {
            reportStep("The element with accessibility: " + accessibility + " could not be clicked", "FAIL");
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception occured while clicking element with accessibility" + accessibility + "", "FAIL");
        }
    }

    public void clickById(String id) {
        try {
            // First try to scroll to the element
            WebElement element = scrollToElementById(id);
            if (element == null) {
                // If scroll didn't find it, try direct find
                element = getDriver().findElement(By.id(id));
            }
            element.click();
            reportStep("The element with id: " + id + " clicked successfully", "PASS");
        } catch (NoSuchElementException e) {
            reportStep("The element with id: " + id + " could not be clicked", "FAIL");
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception occured while clicking element with id" + id + "", "FAIL");
        }
    }

    /**
     * Enter text into element by accessibility id.
     * Works for both Android and iOS.
     * 
     * @param accessibility The accessibility id of the element
     * @param text The text to enter
     */
    public void enterByAccessibility(String accessibility, String text) {
        try {
            WebElement element = getDriver().findElement(new AppiumBy.ByAccessibilityId(accessibility));
            element.clear();
            element.sendKeys(text);
            reportStep("Entered text '" + text + "' in element with accessibility: " + accessibility, "PASS");
        } catch (NoSuchElementException e) {
            reportStep("The element with accessibility: " + accessibility + " could not be found", "FAIL");
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception while entering text in element with accessibility: " + accessibility, "FAIL");
        }
    }

    public boolean scrollFromDownToUpinApp(String xpath) {
        try {
            Dimension size = getDriver().manage().window().getSize();
            int x1 = (int) (size.getWidth() * 0.5);
            int y1 = (int) (size.getHeight() * 0.8);
            int x0 = (int) (size.getWidth() * 0.5);
            int y0 = (int) (size.getHeight() * 0.2);
            performSwipe(x1, y1, x0, y0);
            getDriver().findElement(By.xpath(xpath));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean scrollFromUpToDowninApp() {
        try {
            Dimension size = getDriver().manage().window().getSize();
            int x1 = (int) (size.getWidth() * 0.5);
            int y1 = (int) (size.getHeight() * 0.2);
            int x0 = (int) (size.getWidth() * 0.5);
            int y0 = (int) (size.getHeight() * 0.8);
            performSwipe(x1, y1, x0, y0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean scrollFromRightToLeftinApp() {
        try {
            Dimension size = getDriver().manage().window().getSize();
            int x1 = (int) (size.getWidth() * 0.8);
            int y1 = (int) (size.getHeight() * 0.5);
            int x0 = (int) (size.getWidth() * 0.2);
            int y0 = (int) (size.getHeight() * 0.5);
            performSwipe(x1, y1, x0, y0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean eleIsDisplayed(WebElement ele) {

        try {
            if (ele.isDisplayed())
                return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public void eleIsDisplayed(String xpath) {
        try {
            WebElement ele = getDriver().findElement(By.xpath(xpath));
            ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", ele);
            if (ele.isDisplayed()) {
                reportStep("The element with xpath: " + xpath + " displayed successfully", "PASS");
            }
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("The element with xpath: " + xpath + " NOT displayed", "FAIL");
        }
    }

    public void eleIsDisplayedById(String id) {
        try {
           WebElement ele = getDriver().findElement(By.id(id));
            ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", ele);
            if (ele.isDisplayed()) {
                reportStep("The element with id: " + id + " displayed successfully", "PASS");
            }
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("The element with id: " + id + " NOT displayed", "FAIL");
        }
    }


    public boolean scrollFromUpToDowninAppWithWebElement(WebElement ele) {
        try {
            int x = ele.getLocation().getX();
            int y = ele.getLocation().getY();
            int x1 = x + 135;
            int y1 = y + 10;
            int x0 = x + 135;
            int y0 = y + 500;
            performSwipe(x1, y1, x0, y0);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean scrollFromDownToUpinAppWithWebElement(WebElement ele) {
        try {
            int x = ele.getLocation().getX();
            int y = ele.getLocation().getY();
            int x1 = x + 135;
            int y1 = y + 10;
            int x0 = x + 135;
            int y0 = y + 1400;
            performSwipe(x0, y0, x1, y1);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void enterById(String id, String text) {
        try {
            // First try to scroll to the element
            WebElement element = scrollToElementById(id);
            if (element == null) {
                // If scroll didn't find it, try direct find
                element = getDriver().findElement(By.id(id));
            }
            element.sendKeys(text);
            reportStep("The data: " + text + " entered successfully in element :" + id, "PASS");
        } catch (NoSuchElementException e) {
            reportStep("The data: " + text + " couldn't be entered in element :" + id, "FAIL");
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception occured while entering text in element with id" + id + "", "FAIL");
        }
    }

    public void clickByClass(String className) {
        try {
            // First try to scroll to the element using class name
            WebElement element = null;
            try {
                String uiSelector = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().className(\"" + className + "\"))";
                element = getDriver().findElement(AppiumBy.androidUIAutomator(uiSelector));
            } catch (Exception scrollEx) {
                // If scroll didn't find it, try direct find
                element = getDriver().findElement(By.className(className));
            }
            element.click();
            reportStep("The element with className: " + className + " clicked successfully", "PASS");
        } catch (NoSuchElementException e) {
            reportStep("The element with className: " + className + " could not be clicked", "FAIL");
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception occured while clicking element with className" + className + "", "FAIL");
        }
    }

    public void enterByCssJsEle(WebElement ele, String data) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].innerText = '" + data + "'", ele);
            reportStep("The data: " + data + " entered successfully in element :" + ele, "PASS");

        } catch (NoSuchElementException e) {
            reportStep("The data: " + data + " could not be entered in element :" + ele, "FAIL");

        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception occured while entering " + data + " in element :" + ele, "FAIL");
        }

    }

    /**
     * This method will verify the title of the browser
     *
     * @param title - The expected title of the browser
     * @author Baskar
     */
    public boolean verifyTitle(String title) {
        boolean bReturn = false;
        try {
            if (driver.getTitle().equalsIgnoreCase(title)) {
                reportStep("The title of the page matches with the value :" + title, "PASS");
                bReturn = true;
            } else
                reportStep("The title of the page:" + driver.getTitle() + " did not match with the value :" + title, "SUCCESS");

        } catch (Exception e) {
            reportStep("Unknown exception occured while verifying the title", "FAIL");
        }
        return bReturn;
    }

    /**
     * This method will verify the given text matches in the element text
     *
     * @param xpath - The locator of the object in xpath
     * @param text  - The text to be verified
     * @author Baskar
     */
    public void verifyTextByXpath(String xpath, String text) {
        try {
            WebElement element = getDriver().findElement(By.xpath(xpath));
            // Scroll element into view
            try {
                ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
            } catch (Exception scrollEx) {
                scrollToElementVisible(element);
            }
            String sText = element.getText();
            if (sText.equalsIgnoreCase(text)) {
                reportStep("The text: " + sText + " matches with the value :" + text, "PASS");
            } else {
                reportStep("The text: " + sText + " did not match with the value :" + text, "FAIL");
            }
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception occured while verifying the text", "FAIL");
        }
    }

    /**
     * This method will verify the given text is available in the element text
     *
     * @param xpath - The locator of the object in xpath
     * @param text  - The text to be verified
     * @author Baskar
     */
    public void verifyTextContainsByXpath(String xpath, String text) {
        try {
            String sText = getDriver().findElement(By.xpath(xpath)).getText();
            if (sText.contains(text)) {
                reportStep("The text: " + sText + " contains the value :" + text, "PASS");
            } else {
                reportStep("The text: " + sText + " did not contain the value :" + text, "FAIL");
            }
        } catch (Exception e) {
            reportStep("Unknown exception occured while verifying the title", "FAIL");
        }
    }

    /**
     * This method will verify the given text is available in the element text
     *
     * @param id   - The locator of the object in id
     * @param text - The text to be verified
     * @author Baskar
     */
    public void verifyTextById(String id, String text) {
        try {
            // First try to scroll to the element
            WebElement element = scrollToElementById(id);
            if (element == null) {
                element = getDriver().findElement(By.id(id));
            }
            String sText = element.getText();
            if (sText.equalsIgnoreCase(text)) {
                reportStep("The text: " + sText + " matches with the value :" + text, "PASS");
            } else {
                reportStep("The text: " + sText + " did not match with the value :" + text, "FAIL");
            }
        } catch (Exception e) {
            reportStep("Unknown exception occured while verifying the title", "FAIL");
        }
    }

    /**
     * This method will verify the given text is available in the element text
     *
     * @param id   - The locator of the object in id
     * @param text - The text to be verified
     * @author Baskar
     */
    public void verifyTextContainsById(String id, String text) {
        try {
            String sText = getDriver().findElement(By.id(id)).getText();
            if (sText.contains(text)) {
                reportStep("The text: " + sText + " contains the value :" + text, "PASS");
            } else {
                reportStep("The text: " + sText + " did not contain the value :" + text, "FAIL");
            }
        } catch (Exception e) {
            reportStep("Unknown exception occured while verifying the title", "FAIL");
        }
    }

    /**
     * This method will close all the browsers
     *
     * @author Baskar
     */
    public void quitApplication() {
        try {
            if (getDriver() instanceof AndroidDriver) {
                ((AndroidDriver) getDriver()).terminateApp(((AndroidDriver) getDriver()).getCurrentPackage());
            }
            getDriver().quit();
        } catch (Exception e) {
            reportStep("The browser could not be closed.", "FAIL");
        }

    }

    public void clickByXpath(String xpathVal) {
        try {
            WebElement element = getDriver().findElement(By.xpath(xpathVal));
            // Scroll element into view using JavaScript
            try {
                ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
            } catch (Exception scrollEx) {
                // For native apps, try swipe-based scroll
                scrollToElementVisible(element);
            }
            element.click();
            reportStep("The element : " + xpathVal + " is clicked.", "PASS");
        } catch (WebDriverException e) {
            reportStep("The element with xpath: " + xpathVal + " could not be clicked.", "FAIL");
        }
    }

    /**
     * Scrolls until the element is visible on screen using swipe gestures.
     *
     * @param element The WebElement to scroll to
     */
    private void scrollToElementVisible(WebElement element) {
        try {
            int maxScrolls = 5;
            int scrollCount = 0;
            while (scrollCount < maxScrolls) {
                try {
                    if (element.isDisplayed()) {
                        return;
                    }
                } catch (Exception e) {
                    // Element not visible, continue scrolling
                }
                scrollFromDownToUpinApp(null);
                scrollCount++;
            }
        } catch (Exception e) {
            logger.debug("Could not scroll to element");
        }
    }


    /**
     * This method will mouse over on the element using xpath as locator
     *
     * @param xpathVal The xpath (locator) of the element to be moused over
     * @author Baskar
     */
    public void mouseOverByXpath(String xpathVal) {
        try {
            new Actions(getDriver()).moveToElement(getDriver().findElement(By.xpath(xpathVal))).build().perform();
            reportStep("The mouse over by xpath : " + xpathVal + " is performed.", "PASS");
        } catch (Exception e) {
            reportStep("The mouse over by xpath : " + xpathVal + " could not be performed.", "FAIL");
        }
    }


    /* This method will mouse over on the element using link name as locator
     * @param xpathVal  The link name (locator) of the element to be moused over
     * @author Baskar*/

    public void mouseOverByLinkText(String linkName) {
        try {
            new Actions(getDriver()).moveToElement(getDriver().findElement(By.linkText(linkName))).build().perform();
            reportStep("The mouse over by link : " + linkName + " is performed.", "PASS");
        } catch (Exception e) {
            reportStep("The mouse over by link : " + linkName + " could not be performed.", "FAIL");
        }
    }

    /**
     * This method will return the text of the element using xpath as locator
     *
     * @param xpathVal The xpath (locator) of the element
     * @author Baskar
     */
    public String getTextByXpath(String xpathVal) {
        String bReturn = "";
        try {
            WebElement element = getDriver().findElement(By.xpath(xpathVal));
            // Scroll element into view
            try {
                ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
            } catch (Exception scrollEx) {
                scrollToElementVisible(element);
            }
            return element.getText();
        } catch (Exception e) {
            reportStep("The element with xpath: " + xpathVal + " could not be found.", "FAIL");
        }
        return bReturn;
    }

    /*
     * This method will return the text of the element using id as locator
     * @param xpathVal  The id (locator) of the element
     * @author Baskar
     */
    public String getTextById(String idVal) {
        String bReturn = "";
        try {
            // First try to scroll to the element
            WebElement element = scrollToElementById(idVal);
            if (element == null) {
                element = getDriver().findElement(By.id(idVal));
            }
            return element.getText();
        } catch (Exception e) {
            reportStep("The element with id: " + idVal + " could not be found.", "FAIL");
        }
        return bReturn;
    }


    /**
     * This method will select the drop down value using id as locator
     *
     * @param id    The id (locator) of the drop down element
     * @param value The value to be selected (visibletext) from the dropdown
     * @author Baskar
     */
    public void selectVisibileTextById(String id, String value) {
        try {
            new Select(getDriver().findElement(By.id(id))).selectByVisibleText(value);
            reportStep("The element with id: " + id + " is selected with value :" + value, "PASS");
        } catch (Exception e) {
            reportStep("The value: " + value + " could not be selected.", "FAIL");
        }
    }

    public void selectVisibileTextByXPath(String xpath, String value) {
        try {
            new Select(getDriver().findElement(By.xpath(xpath))).selectByVisibleText(value);
            reportStep("The element with xpath: " + xpath + " is selected with value :" + value, "PASS");
        } catch (Exception e) {
            reportStep("The value: " + value + " could not be selected.", "FAIL");
        }
    }

    public void selectIndexById(String id, String value) {
        try {
            new Select(getDriver().findElement(By.id(id))).selectByIndex(Integer.parseInt(value));
            reportStep("The element with id: " + id + " is selected with index :" + value, "PASS");
        } catch (Exception e) {
            reportStep("The index: " + value + " could not be selected.", "FAIL");
        }
    }

    public void switchToParentWindow() {
        try {
            Set<String> winHandles = getDriver().getWindowHandles();
            for (String wHandle : winHandles) {
                getDriver().switchTo().window(wHandle);
                break;
            }
        } catch (Exception e) {
            reportStep("The window could not be switched to the first window.", "FAIL");
        }
    }

    public void switchToLastWindow() {
        try {
            Set<String> winHandles = getDriver().getWindowHandles();
            for (String wHandle : winHandles) {
                getDriver().switchTo().window(wHandle);
            }
        } catch (Exception e) {
            reportStep("The window could not be switched to the last window.", "FAIL");
        }
    }

    public void acceptAlert() {
        try {
            getDriver().switchTo().alert().accept();
        } catch (NoAlertPresentException e) {
            reportStep("The alert could not be found.", "FAIL");
        } catch (Exception e) {
            reportStep("The alert could not be accepted.", "FAIL");
        }

    }

	public void switchToiFrameByXpath(String iFrameXpath) {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(30));
            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(iFrameXpath)));
            WebElement iFrame = getDriver().findElement(By.xpath(iFrameXpath));
            getDriver().switchTo().frame(iFrame);
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Some error occured.", "FAIL");
        }

	}

	public String getAlertText() {		
        @SuppressWarnings("unused")
        String text = null;
        try {
            getDriver().switchTo().alert().dismiss();
        } catch (NoAlertPresentException e) {
            reportStep("The alert could not be found.", "FAIL");
        } catch (Exception e) {
            reportStep("The alert could not be accepted.", "FAIL");
        }
        return null;

    }

    public long takeSnap() {
        long number = (long) Math.floor(Math.random() * 900000000L) + 10000000L;
        try {
            FileUtils.copyFile(getDriver().getScreenshotAs(OutputType.FILE), new File("./target/reports/extent-report/images/" + number + ".jpg"));
        } catch (WebDriverException e) {
            e.printStackTrace();
        } catch (IOException e) {
            reportStep("The snapshot could not be taken", "WARN");
        }
        return number;
    }

    public byte[] captureScreen() {
        return ((TakesScreenshot) (getDriver())).getScreenshotAs(OutputType.BYTES);
    }

}