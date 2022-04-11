package pages;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import wrappers.LeafTapsWrappers;

public class CalenderPage extends LeafTapsWrappers {

	@And("Set the search dates")
	public void setTheSearchDates() {
		clickByAccessibility(prop.getProperty("Calender.StartDate.Accessibility"));
		scrollFromDownToUpinApp(prop.getProperty("Calender.EndDate.Accessibility"));
		clickByXpath(prop.getProperty("Calender.ConfirmDateBtn.Xpath"));
	}
}
