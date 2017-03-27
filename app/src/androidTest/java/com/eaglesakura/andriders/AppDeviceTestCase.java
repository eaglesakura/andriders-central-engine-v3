package com.eaglesakura.andriders;

import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.andriders.provider.DeviceTestAppStorageProvider;
import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.lambda.Action0;

public abstract class AppDeviceTestCase extends DeviceTestCase<AceApplication> {
    @Override
    public void onSetup() {
        super.onSetup();
        Garnet.clearSingletonCache();
        Garnet.override(AppStorageProvider.class, DeviceTestAppStorageProvider.class);
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
