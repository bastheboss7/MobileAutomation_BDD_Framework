package pages;

import io.appium.java_client.MobileElement;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.HowToUseLocators;
import io.appium.java_client.pagefactory.LocatorGroupStrategy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import wrappers.LeafTapsWrappers;
public class LoginPage extends LeafTapsWrappers {


	@When("I login with email and password")
	public void iLoginWithEmailAndPassword() {
		reportStep("Login to application", "INFO");
		clickByAccessibility(prop.getProperty("LoginPage.SignIn.Accessibility"));
		enterByAccessibity(prop.getProperty("LoginPage.Email.Accessibility"), prop.getProperty("LoginPage.UserName.Data") );
		enterByAccessibity(prop.getProperty("LoginPage.Password.Accessibility"), prop.getProperty("LoginPage.Password.Data") );
		clickByAccessibility(prop.getProperty("LoginPage.Submit.Accessibility"));
	}
}
