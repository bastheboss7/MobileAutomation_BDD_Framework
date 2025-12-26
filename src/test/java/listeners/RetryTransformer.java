package listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Annotation transformer to automatically apply RetryAnalyzer to all tests.
 * Eliminates the need to add retryAnalyzer attribute to each @Test annotation.
 * 
 * @author Baskar
 * @version 2.0.0
 */
public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
            Constructor testConstructor, Method testMethod) {
        // Apply RetryAnalyzer to all test methods
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
