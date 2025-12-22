package com.automation.framework.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * TestNG Listener that automatically applies RetryAnalyzer to all test methods.
 * Add this listener to testng.xml or via @Listeners annotation.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class RetryListener implements IAnnotationTransformer {
    private static final Logger logger = LoggerFactory.getLogger(RetryListener.class);
    
    @Override
    public void transform(ITestAnnotation annotation, Class testClass, 
                          Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
        logger.debug("Applied RetryAnalyzer to test: {}", 
                testMethod != null ? testMethod.getName() : "unknown");
    }
}
