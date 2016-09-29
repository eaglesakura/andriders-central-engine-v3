package com.eaglesakura.andriders;

import com.eaglesakura.andriders.system.AppStorageController;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.provider.AppControllerProvider;
import com.eaglesakura.andriders.provider.TestAppContextProvider;
import com.eaglesakura.andriders.provider.TestAppManagerProvider;
import com.eaglesakura.andriders.provider.TestProviderUtil;
import com.eaglesakura.android.AndroidSupportTestCase;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.util.LogUtil;

import org.robolectric.annotation.Config;

@Config(constants = BuildConfig.class, application = AceApplication.class, packageName = BuildConfig.DEFAULT_PACKAGE_NAME, sdk = 23)
public abstract class AppUnitTestCase extends AndroidSupportTestCase {
    /**
     * ユーザー体重のデフォルト値
     */
    public static final float USER_WEIGHT = 65;

    private AppStorageController mStorageManager;

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
        Garnet.override(AppControllerProvider.class, TestAppManagerProvider.class);
    }

    public synchronized AppStorageController getStorageManager() {
        if (mStorageManager == null) {
            mStorageManager = TestProviderUtil.provideAppStorageManager();
        }

        assertNotNull(mStorageManager);
        assertNotEquals(mStorageManager.getClass(), AppStorageController.class);
        return mStorageManager;
    }
}
