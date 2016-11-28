package com.eaglesakura.andriders.system.context.config;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.android.firebase.auth.FirebaseAuthorizeManager;
import com.eaglesakura.android.firebase.config.FirebaseConfigManager;

import org.junit.Test;

public class AppConfigManagerTest extends AppDeviceTestCase {

    @Test(timeout = 1000 * 30)
    public void Configの同期が行える() throws Throwable {
        FirebaseAuthorizeManager.getInstance().await(() -> false);
        AppConfigManager manager = new AppConfigManager(getContext());
        int fetch = manager.fetch(() -> false);
        // Fetchが成功してなければならない
        assertTrue((fetch & FirebaseConfigManager.FETCH_STATUS_HAS_VALUES) != 0);

        assertNotNull(manager.get());
        assertNotNull(manager.get().profile);
        validate(manager.get().profile.wheel).notEmpty().allNotNull().each(item -> {
            assertNotEmpty(item.title);
            validate(item.length).from(1000);
        });
    }
}