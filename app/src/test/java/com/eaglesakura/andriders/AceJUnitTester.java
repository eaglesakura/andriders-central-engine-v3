package com.eaglesakura.andriders;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, application = AceApplication.class, packageName = BuildConfig.DEFAULT_PACKAGE_NAME)
public abstract class AceJUnitTester {
    Context mContext;

    @Before
    public void onSetup() {
        mContext = RuntimeEnvironment.application;
    }
}
