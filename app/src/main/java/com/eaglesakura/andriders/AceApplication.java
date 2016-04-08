package com.eaglesakura.andriders;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.location.LocationServices;

import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.util.LogUtil;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

public class AceApplication extends Application implements FrameworkCentral.FrameworkApplication {
    Object mCentral;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtil.setLogger(new LogUtil.AndroidLogger(Log.class).setStackInfo(BuildConfig.DEBUG));

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

    public static GoogleApiClient.Builder newFullPermissionClientBuilder() {
        return new GoogleApiClient.Builder(FrameworkCentral.getApplication())
                // Google Fit
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.BLE_API)
                .addApi(Fitness.SESSIONS_API)
                .addScope(Fitness.SCOPE_ACTIVITY_READ_WRITE)
                .addScope(Fitness.SCOPE_BODY_READ_WRITE)
                .addScope(Fitness.SCOPE_LOCATION_READ_WRITE)
                // GPS
                .addApi(LocationServices.API)
                ;
    }
}
