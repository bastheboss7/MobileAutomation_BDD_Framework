package pages;

import io.cucumber.java.en.And;
import wrappers.SkyWrappers;

public class MenuPage extends SkyWrappers {

    @And("Logout from the application")
    public void logoutFromTheApplication() {
        clickByAccessibility(prop.getProperty("Menu.MainMenu.Accessibility"));
        clickByAccessibility(prop.getProperty("Menu.LogOut,Accessbility"));
        clickByXpath(prop.getProperty("Menu.LogOutYes.Xpath"));
    }
}
