package com.eaglesakura.andriders;

import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.andriders.provider.TestAppContextProvider;
import com.eaglesakura.andriders.provider.TestAppManagerProvider;
import com.eaglesakura.andriders.provider.TestAppStorageProvider;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.AndroidSupportTestCase;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.util.LogUtil;

import org.robolectric.annotation.Config;

@Config(constants = BuildConfig.class, packageName = BuildConfig.APPLICATION_ID, sdk = 23)
//@Config(constants = BuildConfig.class, application = AceApplication.class, packageName = BuildConfig.DEFAULT_PACKAGE_NAME, sdk = 23)
public abstract class AppUnitTestCase extends AndroidSupportTestCase {
    /**
     * ユーザー体重のデフォルト値
     */
    public static final float USER_WEIGHT = 65;

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
        Garnet.override(AppStorageProvider.class, TestAppStorageProvider.class);

        AppLog.test("UnitTest Database[%s]", getApplication().getFilesDir().getAbsolutePath());
    }

}
