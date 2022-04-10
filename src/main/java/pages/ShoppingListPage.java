package pages;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import wrappers.LeafTapsWrappers;

public class ShoppingListPage extends LeafTapsWrappers {

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
