package pages;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import wrappers.LeafTapsWrappers;

public class Hooks extends LeafTapsWrappers{

	@Before
	public void launchApplication(Scenario sc) {
		invokeApp();
		startTestCase(sc.getName(), sc.getId());
	}
	
	@After
	public void executeAfterScenario(Scenario scenario) {
		if (scenario.isFailed()) {
			scenario.attach(captureScreen(),"image/png",scenario.getName());
		}
		quitApplication();
	}
}
