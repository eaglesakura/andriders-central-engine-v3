package com.eaglesakura.andriders.db;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.gen.prop.CentralServiceSettings;
import com.eaglesakura.andriders.gen.prop.DebugSettings;
import com.eaglesakura.andriders.gen.prop.UpdateCheckProps;
import com.eaglesakura.andriders.gen.prop.UserProfiles;
import com.eaglesakura.android.device.external.StorageInfo;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.garnet.Singleton;
import com.eaglesakura.android.property.PropertyStore;
import com.eaglesakura.android.property.TextDatabasePropertyStore;
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

    DebugSettings mDebugSettings;

    CentralServiceSettings mCentralSettings;

    UserProfiles mUserProfiles;

    Context mAppContext;

    UpdateCheckProps mUpdateCheckProps;

    PropertyStore mPropertyStore;

    public AppSettings(Context context) {
        mAppContext = context.getApplicationContext();

        mPropertyStore = newDatabasePropertyStore(context);
        mDebugSettings = new DebugSettings(mPropertyStore);
        mCentralSettings = new CentralServiceSettings(mPropertyStore);
        mUserProfiles = new UserProfiles(mPropertyStore);
        mUpdateCheckProps = new UpdateCheckProps(mPropertyStore);
    }

    public UpdateCheckProps getUpdateCheckProps() {
        return mUpdateCheckProps;
    }

    public UserProfiles getUserProfiles() {
        return mUserProfiles;
    }

    public CentralServiceSettings getCentralSettings() {
        return mCentralSettings;
    }

    public DebugSettings getDebugSettings() {
        return mDebugSettings;
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
        return getDebugSettings().isDebugEnable();
    }

    /**
     * 全てのデータを最新版に更新する
     */
    public void commit() {
        mPropertyStore.commit();
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

    /**
     * Load Database Store
     */
    public static PropertyStore newDatabasePropertyStore(Context context) {
        TextDatabasePropertyStore store = new TextDatabasePropertyStore(context, "settings.db");
        store.loadProperties(AppSupportUtil.loadPropertySource(context, R.raw.app_properties));
        return store;
    }
}
