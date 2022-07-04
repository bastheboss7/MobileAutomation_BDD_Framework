package pages;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.testng.Assert;
import wrappers.SkyWrappers;

public class DealsPage extends SkyWrappers {

    @And("I select deals for broadband")
    public void broadbandDeals() {
        clickByXpath(prop.getProperty("Deals.filterBroadband.Xpath"));
    }

    @Then("I see a list of deals with a price to it")
    public void assertDeals() {
        try {
            eleIsDisplayed(prop.getProperty("Deals.offerName1.Xpath"));
            Assert.assertEquals(getTextByXpath(prop.getProperty("Deals.offerPrice1.Xpath")).substring(6), prop.getProperty("Deals.OfferPrice1.Data"));
            reportStep( "Offer 1 - Name and price asserted", "PASS");

            eleIsDisplayed(prop.getProperty("Deals.offerName2.Xpath"));
            Assert.assertEquals(getTextByXpath(prop.getProperty("Deals.offerPrice2.Xpath")).substring(6), prop.getProperty("Deals.OfferPrice2.Data"));
            reportStep("Offer 2 - Name and price asserted", "PASS");

            eleIsDisplayed(prop.getProperty("Deals.offerName3.Xpath"));
            Assert.assertEquals(getTextByXpath(prop.getProperty("Deals.offerPrice3.Xpath")).substring(6), prop.getProperty("Deals.OfferPrice3.Data"));
            reportStep("Offer 3 - Name and price asserted", "PASS");
        } catch (Exception e) {
            e.printStackTrace();
            reportStep("Error occured while asserting the offers", "FAIL");
        }
    }
}
