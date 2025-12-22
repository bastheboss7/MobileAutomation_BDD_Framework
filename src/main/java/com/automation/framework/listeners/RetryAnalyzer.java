package com.automation.framework.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retry Analyzer for flaky tests.
 * Automatically retries failed tests up to the configured max count.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY_COUNT = 2;
    private int retryCount = 0;
    
    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            logger.warn("Retrying test '{}' - Attempt {} of {}", 
                    result.getName(), retryCount, MAX_RETRY_COUNT);
            return true;
        }
        return false;
    }
    
    /**
     * Get current retry count.
     */
    public int getRetryCount() {
        return retryCount;
    }
}
