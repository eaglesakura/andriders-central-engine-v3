package com.eaglesakura.andriders.system.context;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.gen.prop.CentralServiceSettings;
import com.eaglesakura.andriders.gen.prop.DebugSettings;
import com.eaglesakura.andriders.gen.prop.UpdateCheckProps;
import com.eaglesakura.andriders.gen.prop.UserProfiles;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.garnet.Singleton;
import com.eaglesakura.android.property.TextDatabasePropertyStore;
import com.eaglesakura.android.property.TextPropertyStore;

import android.content.Context;
import android.support.annotation.RawRes;

import java.util.Map;

/**
 * 全設定を管理するためのクラス
 */
@Singleton
public class AppSettings {

    final Context mAppContext;

    DebugSettings mDebugSettings;

    CentralServiceSettings mCentralSettings;

    UserProfiles mUserProfiles;

    UpdateCheckProps mUpdateCheckProps;

    /**
     * アプリ動作に関連する設定を保持する
     */
    TextPropertyStore mAppPropertyStore;

    /**
     * Centralで使用するデータを保持する
     */
    TextPropertyStore mCentralPropertyStore;

    public AppSettings(Context context) {
        mAppContext = context.getApplicationContext();

        mAppPropertyStore = newDatabasePropertyStore(context, R.raw.app_properties);
        mCentralPropertyStore = newDatabasePropertyStore(context, R.raw.central_properties);

        mDebugSettings = new DebugSettings(mAppPropertyStore);
        mUpdateCheckProps = new UpdateCheckProps(mAppPropertyStore);

        mCentralSettings = new CentralServiceSettings(mCentralPropertyStore);
        mUserProfiles = new UserProfiles(mCentralPropertyStore);
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
     * デバッグが有効化されていたらtrue
     */
    public boolean isDebuggable() {
        return getDebugSettings().isDebugEnable();
    }

    /**
     * 全てのデータを最新版に更新する
     */
    public void commit() {
        mAppPropertyStore.commit();
        mCentralPropertyStore.commit();
    }

    /**
     * Central動作用のPropertyをダンプする
     */
    public Map<String, String> dumpCentralSettings() {
        return mCentralPropertyStore.asMap();
    }

    /**
     * Load Database Store
     */
    static TextDatabasePropertyStore newDatabasePropertyStore(Context context, @RawRes int propResId) {
        TextDatabasePropertyStore store = new TextDatabasePropertyStore(context, "settings.db");
        store.loadProperties(AppSupportUtil.loadPropertySource(context, propResId));
        return store;
    }
}
