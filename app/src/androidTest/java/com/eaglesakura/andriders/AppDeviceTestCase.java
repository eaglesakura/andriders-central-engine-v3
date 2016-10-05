package com.eaglesakura.andriders;

import com.eaglesakura.andriders.provider.AppControllerProvider;
import com.eaglesakura.andriders.provider.DeviceTestAppControllerProvider;
import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.android.garnet.Garnet;

public class AppDeviceTestCase extends DeviceTestCase<AceApplication> {
    @Override
    public void onSetup() {
        super.onSetup();
        Garnet.override(AppControllerProvider.class, DeviceTestAppControllerProvider.class);
        AppDeviceTestUtil.onSetup(this);
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        AppDeviceTestUtil.onShutdown(this);
    }
}
