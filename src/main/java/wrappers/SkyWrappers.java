package wrappers;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;

import utils.DataInputProvider;

public class SkyWrappers extends GenericWrappers {
	
	public static String excelName;

	@BeforeSuite
	public void beforeSuite(){
		startResult();
	}

	@BeforeTest
	public void beforeTest(){
		loadObjects();
	}
	
	@BeforeMethod
	public void beforeMethod(){

	}
	
	@AfterMethod
	public void afterMethod(){
		endTestcase();
		//quitBrowser();
		
	}	

	@AfterTest
	public void afterTest(){
		unloadObjects();
	}
	
	@AfterSuite
	public void afterSuite(){
		endResult();
	}
	
	@DataProvider(name="fetchData")
	public  Object[][] getData(){
		System.out.println("I am inside dataprovider");
		return DataInputProvider.readExcel(excelName);		
	}
}