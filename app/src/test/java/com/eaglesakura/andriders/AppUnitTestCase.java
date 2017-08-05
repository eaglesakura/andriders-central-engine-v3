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

import org.junit.runner.RunWith;

import org.robolectric.annotation.Config;

@RunWith(AppUnitTestRunner.class)
@Config(constants = BuildConfig.class, packageName = BuildConfig.APPLICATION_ID, sdk = 23)
public abstract class AppUnitTestCase extends AndroidSupportTestCase {
    /**
     * ユーザー体重のデフォルト値
     */
    public static final float USER_WEIGHT = 65;

    @Override
    public void onSetup() {
        super.onSetup();


        // UnitTest用モジュールへ切り替える
        Garnet.clearOverrideMapping();
        Garnet.clearSingletonCache();
        
        TestAppManagerProvider.onSetup(this);
        Garnet.override(AppContextProvider.class, TestAppContextProvider.class);
        Garnet.override(AppManagerProvider.class, TestAppManagerProvider.class);
        Garnet.override(AppStorageProvider.class, TestAppStorageProvider.class);

        AppLog.test("UnitTest Database[%s]", getApplication().getFilesDir().getAbsolutePath());
    }

}
