package com.eaglesakura.andriders.plugin.connection;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.plugin.data.CentralSessionController;
import com.eaglesakura.andriders.serialize.RawSessionInfo;
import com.eaglesakura.andriders.service.CentralSessionService;
import com.eaglesakura.util.Util;

import org.junit.Test;

import android.content.Intent;

/**
 * セッション制御のテスト
 */
public class SessionControlConnectionTest extends AppDeviceTestCase {

    @Override
    public void onSetup() {
        super.onSetup();
        // Serviceを強制切断
        getApplication().stopService(new Intent(getContext(), CentralSessionService.class));
    }

    @Test(timeout = 1000 * 5)
    public void Serviceにバインド出来る() throws Throwable {
        assertFalse(CentralSessionService.isRunning(getContext())); // Service起動前

        SessionControlConnection connection = new SessionControlConnection(getContext());
        assertTrue(connection.connect(() -> false));

        assertTrue(CentralSessionService.isRunning(getContext())); // Service起動後

        // セッション情報を取得し、nullが返却される
        // まだセッションが開始されていないので、nullであるはず。
        CentralSessionController centralSessionController = connection.getCentralSessionController();
        assertNotNull(centralSessionController);
        assertNull(centralSessionController.getSessionInfo());

        assertTrue(connection.disconnect(() -> false));
    }

    @Test(timeout = 1000 * 30)
    public void セッションを開始できる() throws Throwable {
        final long startTime = System.currentTimeMillis();

        SessionControlConnection connection = new SessionControlConnection(getContext());
        assertTrue(connection.connect(() -> false));

        CentralSessionController centralSessionController = connection.getCentralSessionController();
        assertFalse(centralSessionController.isSessionStarted());
        centralSessionController.requestSessionStart();

        // セッション開始まで待つ
        while (!centralSessionController.isSessionStarted()) {
            Util.sleep(1);
        }

        RawSessionInfo sessionInfo = centralSessionController.getSessionInfo();
        assertNotNull(sessionInfo);
        // セッションIDは現在時刻範囲内でなければならない
        validate(sessionInfo.sessionId).from(startTime).to(System.currentTimeMillis());

        // セッションを切断する
        assertTrue(connection.disconnect(() -> false));
        Util.sleep(1000 * 1);

        // セッションが行きて走行を継続していなければならない
        assertTrue(CentralSessionService.isRunning(getContext())); // Service起動後
    }

    @Test(expected = Exception.class)
    public void Serviceバインド前に処理をしようとすると例外が投げられる() throws Throwable {
        assertFalse(CentralSessionService.isRunning(getContext())); // Service起動前

        SessionControlConnection connection = new SessionControlConnection(getContext());
        connection.getCentralSessionController();

        fail();
    }
}
