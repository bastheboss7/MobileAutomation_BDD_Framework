package listeners;

import com.automation.framework.reports.ExtentReportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;

/**
 * TestNG listener to manage Extent Reports lifecycle.
 * Ensures reports are flushed after all tests complete.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class ExtentReportListener implements IExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(ExtentReportListener.class);
    
    @Override
    public void onExecutionStart() {
        logger.info("Test execution started - Initializing Extent Reports");
        ExtentReportManager.initReports();
    }
    
    @Override
    public void onExecutionFinish() {
        logger.info("Test execution finished - Flushing Extent Reports");
        ExtentReportManager.flushReports();
        logger.info("📊 Report generated at: {}", ExtentReportManager.getReportPath());
    }
}
