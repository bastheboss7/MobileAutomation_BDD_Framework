package pages;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import wrappers.SkyWrappers;

public class MyHomePage extends SkyWrappers {

	@Given("I am on the home page")
	public void cookiesHomePage(){
		switchToiFrameByXpath(prop.getProperty("Home.Cookies.Xpath"));
		clickByXpath(prop.getProperty("Home.CookiesAcceptBtn.Xpath"));
		driver.switchTo().parentFrame();
	}

	@When("I navigate to ‘Deals’")
	public void navigateToDeals(){
		clickByXpath(prop.getProperty("Home.Deals.Xpath"));
	}

	@Then("the user should be on the below page(.*)")
	public void assertLandingPage(String url){
		try {
			Assert.assertTrue(driver.getCurrentUrl().contains(url));
			reportStep("Asserted User navigated to "+url, "PASS");
		} catch (Exception e) {
			e.printStackTrace();
			reportStep("Assertion Failed", "FAIL");
		}
	}

	@When("I search ‘sky’ in the search bar")
	public void skySearch(){
		clickById(prop.getProperty("Home.HamburBtn.id"));
		enterByXpath(prop.getProperty("Home.SearchInput.Xpath"), prop.getProperty("Home.Search.Data"));
	}

	@Then("I should see an editorial section")
	public void editorial(){
		eleIsDisplayedById(prop.getProperty("Home.Editorial.Id"));
	}
}
