package pages;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import wrappers.LeafTapsWrappers;

public class MenuPage extends LeafTapsWrappers {

    @And("Logout from the application")
    public void logoutFromTheApplication() {
        clickByAccessibility(prop.getProperty("Menu.MainMenu.Accessibility"));
        clickByAccessibility(prop.getProperty("Menu.LogOut,Accessbility"));
        clickByXpath(prop.getProperty("Menu.LogOutYes.Xpath"));
    }
}
