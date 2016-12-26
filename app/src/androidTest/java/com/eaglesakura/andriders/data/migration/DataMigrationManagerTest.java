package com.eaglesakura.andriders.data.migration;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.android.garnet.Garnet;

import org.junit.Test;

public class DataMigrationManagerTest extends AppDeviceTestCase {

    @Test
    public void 自動マイグレーションが行える() throws Throwable {
        DataMigrationManager manager = Garnet.instance(AppManagerProvider.class, DataMigrationManager.class);
        manager.migration();
        assertEquals(manager.mAppSettings.getUpdateCheckProps().getInitializeReleased(), DataMigrationManager.RELEASE_ACE3);
    }
}