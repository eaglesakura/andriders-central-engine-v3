package com.eaglesakura.andriders;

import com.eaglesakura.andriders.db.AppStorageManager;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.provider.TestAppContextProvider;
import com.eaglesakura.andriders.provider.TestAppManagerProvider;
import com.eaglesakura.andriders.provider.TestProviderUtil;
import com.eaglesakura.android.AndroidSupportTestCase;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.util.LogUtil;

import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@Config(constants = BuildConfig.class, application = AceApplication.class, packageName = BuildConfig.DEFAULT_PACKAGE_NAME, sdk = 23)
public abstract class AppUnitTestCase extends AndroidSupportTestCase {
    /**
     * ユーザー体重のデフォルト値
     */
    public static final double USER_WEIGHT = 65;

    private AppStorageManager mStorageManager;

    @Override
    public void onSetup() {
        super.onSetup();

        // ログを一部無効化する
        LogUtil.setLogEnable("App.GPS", false);
        LogUtil.setLogEnable("App.Ble.Data", false);
        LogUtil.setLogEnable("App.DB", false);

        // UnitTest用モジュールへ切り替える
        TestAppManagerProvider.onSetup(this);
        Garnet.override(AppContextProvider.class, TestAppContextProvider.class);
        Garnet.override(AppManagerProvider.class, TestAppManagerProvider.class);
    }

    public synchronized AppStorageManager getStorageManager() {
        if (mStorageManager == null) {
            mStorageManager = TestProviderUtil.provideAppStorageManager();
        }

        assertNotNull(mStorageManager);
        assertNotEquals(mStorageManager.getClass(), AppStorageManager.class);
        return mStorageManager;
    }
}
