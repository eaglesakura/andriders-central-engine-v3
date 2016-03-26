package com.eaglesakura.andriders;

import com.eaglesakura.android.AndroidSupportTestCase;

import org.robolectric.annotation.Config;

@Config(constants = BuildConfig.class, application = AceApplication.class, packageName = BuildConfig.DEFAULT_PACKAGE_NAME)
public abstract class AppUnitTestCase extends AndroidSupportTestCase {
}
