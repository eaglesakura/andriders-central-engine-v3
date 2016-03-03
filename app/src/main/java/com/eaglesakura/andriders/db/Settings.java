package com.eaglesakura.andriders.db;

import com.eaglesakura.andriders.v2.db.CentralServiceSettings;
import com.eaglesakura.andriders.v2.db.DebugSettings;
import com.eaglesakura.andriders.v2.db.DefaultCommandSettings;
import com.eaglesakura.andriders.v2.db.UpdateCheckProps;
import com.eaglesakura.andriders.v2.db.UserProfiles;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.util.LogUtil;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 全設定を管理するためのクラス
 */
public class Settings {

    final DebugSettings debugSettings;

    final CentralServiceSettings centralSettings;

    final UserProfiles userProfiles;

    final Context appContext;

    final UpdateCheckProps updateCheckProps;

    final DefaultCommandSettings defaultCommandSettings;

    private Settings() {
        this.appContext = FrameworkCentral.getApplication();

        debugSettings = new DebugSettings(appContext);
        centralSettings = new CentralServiceSettings(appContext);
        userProfiles = new UserProfiles(appContext);
        updateCheckProps = new UpdateCheckProps(appContext);
        defaultCommandSettings = new DefaultCommandSettings(appContext);
    }

    public DefaultCommandSettings getDefaultCommandSettings() {
        return defaultCommandSettings;
    }

    public UpdateCheckProps getUpdateCheckProps() {
        return updateCheckProps;
    }

    public UserProfiles getUserProfiles() {
        return userProfiles;
    }

    public CentralServiceSettings getCentralSettings() {
        return centralSettings;
    }

    public DebugSettings getDebugSettings() {
        return debugSettings;
    }

    /**
     * デバッグが有効化されていたらtrue
     */
    public static boolean isDebugable() {
        return getInstance().getDebugSettings().getDebugEnable();
    }

    /**
     * リロードを行う
     */
    public void load() {
        debugSettings.load();
        centralSettings.load();
        userProfiles.load();
        updateCheckProps.load();
        defaultCommandSettings.load();
    }

    /**
     * 全てのデータを最新版に更新する
     */
    public void commitAndLoad() {
        debugSettings.commitAndLoad();
        centralSettings.commitAndLoad();
        userProfiles.commitAndLoad();
        updateCheckProps.commitAndLoad();
        defaultCommandSettings.commitAndLoad();
    }

//    /**
//     * 非同期で最新版に更新する
//     */
//    public void commitAndLoadAsync(SubscriptionController subscriptionController, RxTask.Action1<Settings> completed) {
//        new RxTaskBuilder<Settings>(subscriptionController)
//                .async(it -> {
//                    commitAndLoad();
//                    return Settings.this;
//                })
//                .observeOn(ObserveTarget.FireAndForget)
//                .subscribeOn(SubscribeTarget.GlobalPipeline)
//                .completed(completed)
//                .start();
//    }

    /**
     * データインストール用のパスを取得する
     */
    public File getInstallDataPath() {
        File path = new File(Environment.getExternalStorageDirectory(), "andriders/" + appContext.getPackageName());
        if (!path.isDirectory()) {
            // ディレクトリが生成されていなかったら、nomediaも生成する
            path.mkdirs();
            try {
                FileOutputStream os = new FileOutputStream(new File(path, ".nomedia"));
                os.write("nomedia".getBytes());
                os.close();
            } catch (Exception e) {
                LogUtil.log(e);
            }
        }
        return path;
    }

    private static Settings instance;

    public synchronized static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }

        return instance;
    }
}
