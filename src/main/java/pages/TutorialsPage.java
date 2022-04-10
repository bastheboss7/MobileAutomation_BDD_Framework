package pages;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import wrappers.LeafTapsWrappers;

public class TutorialsPage extends LeafTapsWrappers {

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
