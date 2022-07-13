package pages;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import wrappers.SkyWrappers;

public class ShoppingListPage extends SkyWrappers {

    @Then("I create a new shopping list")
    public void iCreateANewShoppingList() {
        clickByXpath(prop.getProperty("SL.AddOrEditSL.Xpath"));
        clickByAccessibility(prop.getProperty("SL.CreateSL.Accessibility"));
        enterByClass(prop.getProperty("SL.NameSL.Classname"),"MondayGrocery" );
        clickByAccessibility(prop.getProperty("SL.NameNext.Accessibility"));
        clickByXpath(prop.getProperty("SL.ToastMsgOK.Xpath"));
    }


    @When("Add item into newly created shopping list")
    public void AddItemIntoNewlyCreatedShoppingList() {
        enterByAccessibity(prop.getProperty("SL.Item.Accessibility"),"Milk" );
        clickByXpath(prop.getProperty("SL.ItemAdd.Xpath"));
    }

    @Then("Delete the shopping list")
    public void deleteTheShoppingList() {
        clickByAccessibility(prop.getProperty("SL.DeleteSL.Accessibility"));
        clickByXpath(prop.getProperty("SL.ConfirmYes.Xpath"));
    }
}
