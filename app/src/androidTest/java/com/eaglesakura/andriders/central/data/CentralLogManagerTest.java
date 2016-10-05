package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.android.garnet.Garnet;

import org.junit.Test;


public class CentralLogManagerTest extends AppDeviceTestCase {

    @Test
    public void 初回ロードは必ずnullとなる() throws Throwable {
        CentralLogManager logManager = Garnet.instance(AppManagerProvider.class, CentralLogManager.class);
        assertNull(logManager.loadAllStatistics());
        assertNull(logManager.loadTodayStatistics(System.currentTimeMillis()));
    }
}