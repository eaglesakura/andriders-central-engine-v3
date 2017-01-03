package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.android.device.external.Storage;
import com.eaglesakura.android.garnet.Garnet;

import org.junit.Test;

import android.net.Uri;

import java.io.File;


/**
 * 存在するデータを利用してテストを行なう
 * 事前インストール必須
 */
public class CentralLogManagerTest2 extends AppDeviceTestCase {

    @Override
    public void onSetup() {
        super.onSetup();
        Garnet.override(AppStorageProvider.class, AppStorageProvider.class);
    }

    @Test
    public void AACR2015のデータをエクスポートできる() throws Throwable {
        long SESSION_ID = 1432414288000L;

        File output = new File(Storage.getExternalDataStorage(getContext()).getPath(), "test/aacr2015.zip");
        CentralLogManager logManager = Garnet.instance(AppManagerProvider.class, CentralLogManager.class);

        // 指定した日付をバックアップする
        {
            output.getParentFile().mkdirs();
            output.delete();
            assertFalse(output.isFile());

            logManager.exportDailySessions(SESSION_ID, Uri.fromFile(output), () -> false);
            assertTrue(output.isFile());
        }
    }
}