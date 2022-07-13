package pages;

import io.cucumber.java.en.And;
import wrappers.SkyWrappers;

public class TutorialsPage extends SkyWrappers {

    @And("Skip tutorials")
    public void SkipTutorials() {
//        Provide permission to access location
        clickById(prop.getProperty("Permissions.Location.Id"));
//        Skipping tutorials begin here
        clickByAccessibility(prop.getProperty("TutorialPage.GetStarted.Accessibility"));
        clickByAccessibility(prop.getProperty("TutorialPage.Skip.Accessibility"));
        clickByAccessibility(prop.getProperty("TutorialPage.SkipTutorial.Accessibility"));
        clickByXpath(prop.getProperty("TutorialPage.Confirm.Xpath"));
    }
}
