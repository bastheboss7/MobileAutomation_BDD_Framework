package pages;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import wrappers.SkyWrappers;

public class LoginPage extends SkyWrappers {

	@When("I try to sign in with invalid credentials")
	public void signInInvalid(){
		clickByXpath(prop.getProperty("Home.SignIn.Xpath"));
		switchToiFrameByXpath(prop.getProperty("LoginPage.Cookies.Xpath"));
		enterByXpath(prop.getProperty("LoginPage.UserName.Xpath"),prop.getProperty("LoginPage.UserName.Data") );
		clickByXpath(prop.getProperty("LoginPage.ContinueBtn.Xpath"));
	}

	@Then("I should see a box with the text ‘Create your My Sky password’")
	public void assertPasswordBox(){
		verifyTextByXpath(prop.getProperty("LoginPage.PasswordMsg.Xpath"),"Create your My Sky password");
	}

	@When("I login with email and password")
	public void iLoginWithEmailAndPassword() {
		reportStep("Login to application", "INFO");
		clickByAccessibility(prop.getProperty("LoginPage.SignIn.Accessibility"));
		enterByAccessibity(prop.getProperty("LoginPage.Email.Accessibility"), prop.getProperty("LoginPage.UserName.AsdaData") );
		enterByAccessibity(prop.getProperty("LoginPage.Password.Accessibility"), prop.getProperty("LoginPage.Password.Data") );
		clickByAccessibility(prop.getProperty("LoginPage.Submit.Accessibility"));
	}
}
