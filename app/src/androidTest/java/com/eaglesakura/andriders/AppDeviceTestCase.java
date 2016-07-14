package com.eaglesakura.andriders;

import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.android.framework.FrameworkCentral;

import android.content.Context;

public class AppDeviceTestCase extends DeviceTestCase<AceApplication> {
    @Override
    public AceApplication getApplication() {
        return (AceApplication) FrameworkCentral.getApplication();
    }

    @Override
    public Context getContext() {
        return FrameworkCentral.getApplication();
    }
}
