package com.eaglesakura.andriders.central.service;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.util.RandomUtil;
import com.eaglesakura.util.Timer;
import com.eaglesakura.util.Util;

import org.junit.Test;

/**
 * セッション実行テスト
 */
public class CentralSessionTest extends AppDeviceTestCase {

    @Test(timeout = 1000 * 10)
    public void セッションが生成できる() throws Throwable {
        SessionInfo sessionInfo = new SessionInfo.Builder(getContext(), new Clock(System.currentTimeMillis()))
                .debugable(true)
                .build();

        assertNotNull(sessionInfo);
        assertEquals(sessionInfo.getSessionClock().now(), sessionInfo.getSessionId());  // 始まりの時刻は同じである
        assertNotNull(sessionInfo.getCentralServiceSettings());
        assertNotNull(sessionInfo.getContext());
        assertNotNull(sessionInfo.getUserProfiles());

        CentralSession centralSession = CentralSession.newInstance(sessionInfo);
        assertNotNull(centralSession);
        assertNotNull(centralSession.mPluginDataManager);
        assertNotNull(centralSession.mSessionInfo);
        assertEquals(centralSession.mSessionInfo, sessionInfo);
    }

    @Test(timeout = 1000 * 30)
    public void セッションのライフサイクルを通過できる() throws Throwable {
        Timer timer = new Timer();
        CentralSession centralSession = CentralSession.newInstance(new SessionInfo.Builder(getContext(), new Clock(System.currentTimeMillis())).debugable(true).build());

        try {
            timer.start();
            awaitUiThread(() -> centralSession.initialize(new CentralSession.InitializeOption()));
            while (centralSession.mCentralDataManager == null) {
                Util.sleep(1);
            }
        } finally {
            validate(timer.end()).to(1000 * 10); // 10秒以内に処理出来ている
        }

        // セッション内時間で8時間を経過させる
        for (int i = 0; i < (60 * 60 * 8); ++i) {
            // 1秒程度のオフセットを生成する
            double offsetTimeSec = (RandomUtil.randFloat() * 0.2) + 0.9;

            try {
                timer.start();
                awaitUiThread(() -> centralSession.onUpdate(offsetTimeSec));
            } finally {
                validate(timer.end()).to(1000);
            }
        }

        try {
            timer.start();
            awaitUiThread(() -> centralSession.dispose());
            while (centralSession.mCentralDataManager != null) {
                Util.sleep(1);
            }
        } finally {
            validate(timer.end()).to(1000 * 10); // 10秒以内に処理出来ている
        }
    }
}