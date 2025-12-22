package pages;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import wrappers.SkyWrappers;

public class Hooks extends SkyWrappers{

	@Before
	public void launchApplication(Scenario sc) {
		// Cross-platform app launch
		if (isIOS()) {
			invokeIOSApp();
		} else {
			invokeApp();
		}
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
