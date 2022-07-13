package pages;

import io.cucumber.java.en.Then;
import wrappers.SkyWrappers;

public class SearchPage extends SkyWrappers {

	@Then("I see results for my search")
	public void iSeeResultsForMySearch() {
		eleIsDisplayed(prop.getProperty("Search.Results.Xpath"));
	}
}
