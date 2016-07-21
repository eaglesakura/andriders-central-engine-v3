package com.eaglesakura.andriders;

import com.eaglesakura.andriders.db.AppStorageManager;
import com.eaglesakura.andriders.provider.StorageProvider;
import com.eaglesakura.andriders.provider.TestStorageProvider;
import com.eaglesakura.android.AndroidSupportTestCase;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.util.LogUtil;

import org.robolectric.annotation.Config;

import android.content.Context;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@Config(constants = BuildConfig.class, application = AceApplication.class, packageName = BuildConfig.DEFAULT_PACKAGE_NAME, sdk = 23)
public abstract class AppUnitTestCase extends AndroidSupportTestCase {
    /**
     * ユーザー体重のデフォルト値
     */
    public static final double USER_WEIGHT = 65;

    @Inject(StorageProvider.class)
    protected AppStorageManager mStorageManager;

    @Override
    public void onSetup() {
        super.onSetup();

        // ログを一部無効化する
        LogUtil.setLogEnable("App.GPS", false);
        LogUtil.setLogEnable("App.Ble.Data", false);
        LogUtil.setLogEnable("App.DB", false);

        TestStorageProvider.onSetup(this);

        // UnitTest用モジュールへ切り替える
        Garnet.override(StorageProvider.class, TestStorageProvider.class);

        // Injection
        Garnet.create(this).depend(Context.class, getContext()).inject();

        assertNotNull(mStorageManager);
        assertNotEquals(mStorageManager.getClass(), AppStorageManager.class);
    }
}
