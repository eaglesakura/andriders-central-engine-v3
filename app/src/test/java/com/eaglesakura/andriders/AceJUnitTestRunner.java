package com.eaglesakura.andriders;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;

public class AceJUnitTestRunner extends RobolectricGradleTestRunner {
    public AceJUnitTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }
}
