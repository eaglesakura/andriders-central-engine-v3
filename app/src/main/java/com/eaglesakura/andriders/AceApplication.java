package com.eaglesakura.andriders;

import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.sloth.Sloth;

import android.app.Application;

public class AceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppLog.inject(this);

        // ACE環境の初期化を行う
        AceEnvironment.initialize(this);

        // Central & DeploygateRemote
        Sloth.init(this);
//        FrameworkCentral.requestDeploygateInstall();
    }
}
