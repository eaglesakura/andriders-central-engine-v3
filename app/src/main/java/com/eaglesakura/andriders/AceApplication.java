package com.eaglesakura.andriders;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.location.LocationServices;

import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.thread.async.AsyncTaskController;
import com.eaglesakura.util.LogUtil;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class AceApplication extends Application implements FrameworkCentral.FrameworkApplication {
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

        // 設定をロードする
        Settings.getInstance();
    }

    @Override
    public void onApplicationUpdated(int oldVersionCode, int newVersionCode, String oldVersionName, String newVersionName) {
        LogUtil.log("App Updated old(%d:%s) new(%d:%s)", oldVersionCode, oldVersionName, newVersionCode, newVersionName);
    }

    private static AsyncTaskController gTaskController = new AsyncTaskController(3);

    /**
     * グローバルで処理されるタスクコントローラを取得する
     */
    public static AsyncTaskController getTaskController() {
        return gTaskController;
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
