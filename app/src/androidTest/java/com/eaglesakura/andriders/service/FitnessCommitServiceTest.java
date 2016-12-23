package com.eaglesakura.andriders.service;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.util.ContextUtil;

import org.junit.Test;

import android.content.Intent;

public class FitnessCommitServiceTest extends AppDeviceTestCase {

    @Override
    public void onSetup() {
        super.onSetup();

        // ストレージを通常に戻す
        Garnet.override(AppStorageProvider.class, AppStorageProvider.class);
    }

    @Test(timeout = (1000 * 60 * 30))
    public void アップロードタスクを実行する() throws Throwable {
        // AACR2015をアップロード
        {
            Intent intent = FitnessCommitService.Builder.from(getContext())
                    .session(1432414288000L)
                    .build();
            getContext().startService(intent);
        }

        sleep(1000);

        // Serviceが死ぬまで待つ
        while (ContextUtil.isServiceRunning(getContext(), FitnessCommitService.class)) {
            sleep(100);
        }
    }
}