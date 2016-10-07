package com.eaglesakura.andriders;

import com.eaglesakura.andriders.provider.AppControllerProvider;
import com.eaglesakura.andriders.provider.DeviceTestAppControllerProvider;
import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.lambda.Action0;

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

    public void awaitUiThread(Action0 uiThreadAction) throws Throwable {
        UIHandler.postWithWait(() -> {
            try {
                uiThreadAction.action();
            } catch (Throwable e) {
                e.printStackTrace();
                fail();
            }
        }, () -> false);
    }

}
