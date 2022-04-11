package pages;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import wrappers.LeafTapsWrappers;

public class BookingPage extends LeafTapsWrappers {

	@When("Enter my destination")
	public void enterMyDestination() {
		clickByXpath(prop.getProperty("BP.ClickDestination.Xpath"));
		enterById(prop.getProperty("BP.EnterDestination.Id"),"Manchester");
		clickById(prop.getProperty("BP.ChooseDestination.Id"));
	}

	@And("Click search")
	public void clickSearch() {
		clickByXpath(prop.getProperty("BP.ClkSearchBtn.Xpath"));
	}

	@Given("I signin with existing mail")
	public void iSigninWithExitingMail() {
		clickById(prop.getProperty("BP.SignIn.Id"));
		clickById(prop.getProperty("BP.StartSearch.Id"));
	}
}
