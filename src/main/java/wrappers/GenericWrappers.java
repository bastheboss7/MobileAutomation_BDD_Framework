package wrappers;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.Reporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GenericWrappers extends Reporter{


//    protected static final ThreadLocal<GenericWrappers> driverThreadLocal = new ThreadLocal<>();
    public static AppiumDriver<MobileElement> driver;
    public static Properties prop;
    public String sHubUrl;
    public String systemPort;
    public static String appPackage;
    public String appActivity;
    public String deviceName;
    public String udid;
    public String installApp;
//    public String path = "C:\\Users\\dell\\AppData\\Local\\Programs\\Appium\\resources\\app\\node_modules\\appium\\node_modules\\appium-chromedriver\\chromedriver\\win\\chromedriver.exe";
    public WebDriverWait wait;
    public String apkPath;

    public GenericWrappers() {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("./src/main/resources/config.properties"));
            sHubUrl = prop.getProperty("HUB");
            systemPort = prop.getProperty("PORT");
            appActivity = prop.getProperty("appActivity");
            appPackage = prop.getProperty("appPackage");
            deviceName = prop.getProperty("deviceName");
            udid = prop.getProperty("udid");
            apkPath = prop.getProperty("apkPath");
            installApp = prop.getProperty("installApp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadObjects() {
        prop = new Properties();
        try {
            prop.load(new FileInputStream("src/main/resources/object.properties"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            DesiredCapabilities dc = new DesiredCapabilities();
            dc.setCapability("appPackage", appPackage);
            dc.setCapability("appActivity", appActivity);
            dc.setCapability("deviceName", deviceName);
            dc.setCapability("automationName", "UiAutomator2");
            dc.setCapability("noReset", false);
            dc.setCapability("udid", udid);
            dc.setCapability("systemPort", systemPort);
//            dc.setCapability("chromedriverExecutable", path);
            dc.setCapability("newCommandTimeout", 6000);
            driver = new AndroidDriver<MobileElement>(new URL(sHubUrl), dc);
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
            if (installApp.equalsIgnoreCase("true")) {
                driver.installApp(apkPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //reportStep("The browser:" + browser + " launched successfully", "PASS");
        return driver;
    }

    public void waitForElementToBeVisible(MobileElement element, long timeoutInSeconds) {
        wait = new WebDriverWait(driver, timeoutInSeconds);
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
            e.printStackTrace();
            reportStep("Unknown exception occured while entering " + data + " in the ele :" + ele, "FAIL");
        }
    }

    public void enterByXpath(String xpath, String data) {
        try {
            driver.findElement(By.xpath(xpath)).sendKeys(data);
            reportStep("The data: " + data + " entered successfully using xpath :" + xpath, "PASS");

        } catch (NoSuchElementException e) {
            reportStep("The data: " + data + " could not be entered in the ele :" + xpath, "FAIL");
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception occured while entering " + data + " in the ele :" + xpath, "FAIL");
        }
    }

    public void enterByAccessibity(String accessibility, String data) {
        try {
            driver.findElementByAccessibilityId(accessibility).sendKeys(data);
            reportStep("The data: " + data + " entered successfully using xpath :" + accessibility, "PASS");

        } catch (NoSuchElementException e) {
            reportStep("The data: " + data + " could not be entered in the ele :" + accessibility, "FAIL");
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception occured while entering " + data + " in the ele :" + accessibility, "FAIL");
        }
    }

    public void enterByClass(String className, String data) {
        try {
            driver.findElementByClassName(className).sendKeys(data);
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
            driver.findElementByAccessibilityId(accessibility).click();
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
            driver.findElementById(id).click();
            reportStep("The element with id: " + id + " clicked successfully", "PASS");

        } catch (NoSuchElementException e) {
            reportStep("The element with id: " + id + " could not be clicked", "FAIL");
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Unknown exception occured while clicking element with id" + id + "", "FAIL");
        }

    }

    public void clickByClass(String className) {
        try {
            driver.findElementByClassName(className).click();
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
            String sText = driver.findElementByXPath(xpath).getText();
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
            String sText = driver.findElementByXPath(xpath).getText();
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
            String sText = driver.findElementById(id).getText();
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
            String sText = driver.findElementById(id).getText();
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
            driver.closeApp();
            driver.quit();
        } catch (Exception e) {
            reportStep("The browser:" + driver.getCapabilities().getBrowserName() + " could not be closed.", "FAIL");
        }

    }

    public void clickByXpath(String xpathVal) {
        try {
            driver.findElement(By.xpath(xpathVal)).click();
            reportStep("The element : " + xpathVal + " is clicked.", "INFO");
        } catch (WebDriverException e) {
            reportStep("The element with xpath: " + xpathVal + " could not be clicked.", "FAIL");
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
            new Actions(driver).moveToElement(driver.findElement(By.xpath(xpathVal))).build().perform();
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
            new Actions(driver).moveToElement(driver.findElement(By.linkText(linkName))).build().perform();
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
            return driver.findElement(By.xpath(xpathVal)).getText();
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
            return driver.findElementById(idVal).getText();
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
            new Select(driver.findElement(By.id(id))).selectByVisibleText(value);
            reportStep("The element with id: " + id + " is selected with value :" + value, "PASS");
        } catch (Exception e) {
            reportStep("The value: " + value + " could not be selected.", "FAIL");
        }
    }

    public void selectVisibileTextByXPath(String xpath, String value) {
        try {
            new Select(driver.findElement(By.xpath(xpath))).selectByVisibleText(value);
            reportStep("The element with xpath: " + xpath + " is selected with value :" + value, "PASS");
        } catch (Exception e) {
            reportStep("The value: " + value + " could not be selected.", "FAIL");
        }
    }

    public void selectIndexById(String id, String value) {
        try {
            new Select(driver.findElement(By.id(id))).selectByIndex(Integer.parseInt(value));
            reportStep("The element with id: " + id + " is selected with index :" + value, "PASS");
        } catch (Exception e) {
            reportStep("The index: " + value + " could not be selected.", "FAIL");
        }
    }

    public void switchToParentWindow() {
        try {
            Set<String> winHandles = driver.getWindowHandles();
            for (String wHandle : winHandles) {
                driver.switchTo().window(wHandle);
                break;
            }
        } catch (Exception e) {
            reportStep("The window could not be switched to the first window.", "FAIL");
        }
    }

    public void switchToLastWindow() {
        try {
            Set<String> winHandles = driver.getWindowHandles();
            for (String wHandle : winHandles) {
                driver.switchTo().window(wHandle);
            }
        } catch (Exception e) {
            reportStep("The window could not be switched to the last window.", "FAIL");
        }
    }

    public void acceptAlert() {
        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException e) {
            reportStep("The alert could not be found.", "FAIL");
        } catch (Exception e) {
            reportStep("The alert could not be accepted.", "FAIL");
        }

    }

    public String getAlertText() {
        String text = null;
        try {
            driver.switchTo().alert().dismiss();
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
            FileUtils.copyFile(driver.getScreenshotAs(OutputType.FILE), new File("./target/reports/extent-report/images/" + number + ".jpg"));
        } catch (WebDriverException e) {
            e.printStackTrace();
        } catch (IOException e) {
            reportStep("The snapshot could not be taken", "WARN");
        }
        return number;
    }

    public byte[] captureScreen() {
        return ((TakesScreenshot) (driver)).getScreenshotAs(OutputType.BYTES);
    }

}