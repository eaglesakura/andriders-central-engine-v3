package com.eaglesakura.andriders.db;

import com.eaglesakura.andriders.v2.db.CentralServiceSettings;
import com.eaglesakura.andriders.v2.db.DebugSettings;
import com.eaglesakura.andriders.v2.db.DefaultCommandSettings;
import com.eaglesakura.andriders.v2.db.UpdateCheckProps;
import com.eaglesakura.andriders.v2.db.UserProfiles;
import com.eaglesakura.android.device.external.StorageInfo;
import com.eaglesakura.android.garnet.Singleton;
import com.eaglesakura.util.LogUtil;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 全設定を管理するためのクラス
 */
@Singleton
public class AppSettings {

    final DebugSettings debugSettings;

    final CentralServiceSettings centralSettings;

    final UserProfiles userProfiles;

    final Context mAppContext;

    final UpdateCheckProps updateCheckProps;

    final DefaultCommandSettings defaultCommandSettings;

    public AppSettings(Context context) {
        mAppContext = context.getApplicationContext();
        debugSettings = new DebugSettings(mAppContext);
        centralSettings = new CentralServiceSettings(mAppContext);
        userProfiles = new UserProfiles(mAppContext);
        updateCheckProps = new UpdateCheckProps(mAppContext);
        defaultCommandSettings = new DefaultCommandSettings(mAppContext);
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
     * 外部ストレージに保存するDBファイルパスを取得する
     */
    public File getExternalDatabasePath(String name) {
        File external = StorageInfo.getExternalStorageRoot(mAppContext);
        if (!name.endsWith(".db")) {
            name += ".db";
        }
        return new File(external, "db/" + name);
    }

    /**
     * デバッグが有効化されていたらtrue
     */
    public boolean isDebuggable() {
        return getDebugSettings().getDebugEnable();
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

    /**
     * データインストール用のパスを取得する
     */
    public File getInstallDataPath() {
        File path = new File(Environment.getExternalStorageDirectory(), "andriders/" + mAppContext.getPackageName());
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

}
