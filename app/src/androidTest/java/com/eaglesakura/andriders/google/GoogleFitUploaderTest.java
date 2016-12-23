package com.eaglesakura.andriders.google;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.thread.IntHolder;

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

        IntHolder startCalls = new IntHolder();
        IntHolder endCalls = new IntHolder();
        int sessions = uploader.uploadDaily(new GoogleFitUploader.UploadCallback() {
            @Override
            public void onUploadStart(GoogleFitUploader self, SessionHeader session) {
                AppLog.test("UploadStart id[%d]", session.getSessionId());
                startCalls.add(1);
            }

            @Override
            public void onUploadCompleted(GoogleFitUploader self, SessionHeader session) {
                AppLog.test("UploadCompleted id[%d]", session.getSessionId());
                endCalls.add(1);
            }
        }, () -> false);

        assertEquals(sessions, 8);  // AACR2015は8セッションで構築されている
        assertEquals(startCalls.value, sessions);
        assertEquals(endCalls.value, sessions);
    }
}