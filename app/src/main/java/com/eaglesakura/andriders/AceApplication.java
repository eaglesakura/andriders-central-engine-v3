package com.eaglesakura.andriders;

import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.FrameworkCentral;

import android.app.Application;
import android.support.annotation.NonNull;

public class AceApplication extends Application implements FrameworkCentral.FrameworkApplication {
    Object mCentral;

    @Override
    public void onCreate() {
        super.onCreate();

        AppLog.inject(this);

        // ACE環境の初期化を行う
        AcesEnvironment.initialize(this);

        // Central & DeploygateRemote
        FrameworkCentral.onApplicationCreate(this);
        FrameworkCentral.requestDeploygateInstall();
    }

    @Override
    public void onApplicationUpdated(int oldVersionCode, int newVersionCode, String oldVersionName, String newVersionName) {
        AppLog.system("App Updated old(%d:%s) new(%d:%s)", oldVersionCode, oldVersionName, newVersionCode, newVersionName);
    }

    @Override
    public void onRequestSaveCentral(@NonNull Object central) {
        mCentral = central;
    }
}
