package com.automation.tests.runners;

import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * TestNG Cucumber Runner for executing BDD tests.
 * Uses the new package structure with separated Page Objects and Steps.
 * 
 * @author Baskar
 * @version 2.0.0
 */
@CucumberOptions(
        tags = "@iOS",
        features = "src/test/resources/features",
        glue = {"com.automation.tests.steps", "com.automation.tests.hooks"},
        plugin = {
                "summary",
                "pretty",
                "html:target/reports/cucumber-report/cucumber-pretty/",
                "json:target/reports/cucumber-report/CucumberTestReport.json",
                "timeline:target/reports/cucumber-report/timeline"
        },
        monochrome = true
)
public class TestRunner {
    
    private TestNGCucumberRunner testNGCucumberRunner;
    
    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        testNGCucumberRunner = new TestNGCucumberRunner(this.getClass());
    }
    
    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios", dataProvider = "scenarios")
    public void scenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        testNGCucumberRunner.runScenario(pickleWrapper.getPickle());
    }
    
    @DataProvider(name = "scenarios", parallel = false)
    public Object[][] scenarios() {
        return testNGCucumberRunner.provideScenarios();
    }
    
    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        testNGCucumberRunner.finish();
    }
}
