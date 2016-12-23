package com.eaglesakura.andriders.google;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.android.garnet.Garnet;

import org.junit.Test;

public class GoogleFitUploaderTest extends AppDeviceTestCase {
    @Override
    public void onSetup() {
        super.onSetup();

        // ストレージを通常に戻す
        Garnet.override(AppStorageProvider.class, AppStorageProvider.class);
    }

    @Test
    public void AACR2015のデータがアップロードできる() throws Throwable {
        GoogleFitUploader uploader = GoogleFitUploader.Builder.from(getContext())
                .session(1432414288000L)
                .build();
        assertNotNull(uploader);

        int sessions = uploader.uploadDaily(() -> false);
        assertEquals(sessions, 8);  // AACR2015は8セッションで構築されている
    }
}