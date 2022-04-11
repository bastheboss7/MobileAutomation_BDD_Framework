package pages;

import io.cucumber.java.en.Then;
import wrappers.LeafTapsWrappers;

public class SearchPage extends LeafTapsWrappers {

	@Then("I see results for my search")
	public void iSeeResultsForMySearch() {
		eleIsDisplayed(prop.getProperty("Search.Results.Xpath"));
	}
}
