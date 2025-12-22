package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class Reporter {
	public static ExtentReports extent;
	private static Map<RemoteWebDriver, ExtentTest> testDriver;
	public AppiumDriver driver;


	public void reportStep(String desc, String status) {
		long snapNumber = 100000L;
		ExtentTest test = testDriver.get(driver);
		if (!status.equalsIgnoreCase("INFO")) {
			try {
				snapNumber = takeSnap();
				byte[] screenshot = captureScreen();
				   // Allure reporting removed
				if (test != null) {
					test.addScreenCaptureFromPath("images/" + snapNumber + ".jpg", desc);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (test != null) {
			switch (status.toUpperCase()) {
				case "PASS":
					test.log(Status.PASS, desc);
					break;
				case "FAIL":
					test.log(Status.FAIL, desc);
					throw new RuntimeException("FAILED");
				case "INFO":
					test.log(Status.INFO, desc);
					break;
				case "WARN":
					test.log(Status.WARNING, desc);
					break;
				default:
					test.log(Status.INFO, desc);
			}
		}
	}

	public abstract long takeSnap();
	public abstract byte[] captureScreen();


	public ExtentReports startResult() {
		testDriver = new HashMap<RemoteWebDriver, ExtentTest>();
		ExtentSparkReporter spark = new ExtentSparkReporter("./target/reports/extent-report/result.html");
		try {
			spark.loadXMLConfig(new File("src/main/resources/extent-config.xml"));
		} catch (Exception e) {
			// Log and continue with default config
			e.printStackTrace();
		}
		extent = new ExtentReports();
		extent.attachReporter(spark);
		return extent;
	}

	public synchronized ExtentTest startTestCase(String testCaseName, String testDescription) {
		ExtentTest test = extent.createTest(testCaseName, testDescription);
		testDriver.put(driver, test);
		return test;
	}

	public void endResult() {
		extent.flush();
	}

	public void endTestcase() {
		// No explicit endTest in ExtentReports 5.x; flush is sufficient
	}
}