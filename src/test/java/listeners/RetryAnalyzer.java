package listeners;

import com.automation.framework.core.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retry analyzer for handling flaky mobile tests.
 * Automatically retries failed tests up to a configurable number of times.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(RetryAnalyzer.class);
    
    private int retryCount = 0;
    private static final int DEFAULT_MAX_RETRY = 2;
    
    /**
     * Get max retry count from configuration or use default.
     */
    private int getMaxRetryCount() {
        return ConfigManager.getInt("retry.maxCount", DEFAULT_MAX_RETRY);
    }
    
    /**
     * Determines whether the test should be retried.
     * 
     * @param result The test result
     * @return true if the test should be retried, false otherwise
     */
    @Override
    public boolean retry(ITestResult result) {
        int maxRetry = getMaxRetryCount();
        
        if (retryCount < maxRetry) {
            retryCount++;
            logger.warn("⚠️ Test '{}' failed. Retrying... Attempt {}/{}", 
                    result.getName(), retryCount, maxRetry);
            return true;
        }
        
        logger.error("❌ Test '{}' failed after {} retry attempts", 
                result.getName(), maxRetry);
        return false;
    }
}
