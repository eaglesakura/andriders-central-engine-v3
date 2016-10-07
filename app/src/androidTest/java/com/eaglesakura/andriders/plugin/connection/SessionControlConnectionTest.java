package com.eaglesakura.andriders.plugin.connection;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.plugin.data.CentralSessionController;
import com.eaglesakura.andriders.provider.AppControllerProvider;
import com.eaglesakura.andriders.serialize.RawSessionInfo;
import com.eaglesakura.andriders.service.CentralSessionService;
import com.eaglesakura.android.garnet.Garnet;
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

        // システム関連をもとに戻して、DB接続先を正常にする
        Garnet.override(AppControllerProvider.class, AppControllerProvider.class);
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

        RawSessionInfo info;
        {
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

            info = centralSessionController.getSessionInfo();
            assertNotNull(info);
            // セッションIDは現在時刻範囲内でなければならない
            validate(info.sessionId).from(startTime).to(System.currentTimeMillis());

            // セッションを切断する
            assertTrue(connection.disconnect(() -> false));
            Util.sleep(1000 * 1);

            // セッションが行きて走行を継続していなければならない
            assertTrue(CentralSessionService.isRunning(getContext())); // Service起動後
        }

        // 再度接続して、ちゃんと起動が行えているから確認する
        {
            SessionControlConnection connection = new SessionControlConnection(getContext());
            assertTrue(connection.connect(() -> false));

            RawSessionInfo sessionInfo = connection.getCentralSessionController().getSessionInfo();
            assertNotNull(sessionInfo);
            assertEquals(sessionInfo, info);
            connection.disconnect(() -> false);
        }

        // 停止処理が行える
        {
            SessionControlConnection connection = new SessionControlConnection(getContext());
            connection.connect(() -> false);
            connection.getCentralSessionController().requestSessionStop();

            // セッション停止まで待つ
            // セッション開始まで待つ
            while (connection.getCentralSessionController().isSessionStarted()) {
                Util.sleep(1);
            }

            connection.disconnect(() -> false);
        }
    }

    @Test(expected = Exception.class)
    public void Serviceバインド前に処理をしようとすると例外が投げられる() throws Throwable {
        assertFalse(CentralSessionService.isRunning(getContext())); // Service起動前

        SessionControlConnection connection = new SessionControlConnection(getContext());
        connection.getCentralSessionController();

        fail();
    }
}
