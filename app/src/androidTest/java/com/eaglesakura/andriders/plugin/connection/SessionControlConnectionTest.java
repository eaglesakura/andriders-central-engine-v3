package com.eaglesakura.andriders.plugin.connection;

import com.eaglesakura.andriders.AppDeviceTestCase;
import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.andriders.serialize.NotificationProtocol;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSessionInfo;
import com.eaglesakura.andriders.service.CentralSessionService;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.thread.IntHolder;
import com.eaglesakura.util.Util;

import org.junit.Test;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * セッション制御のテスト
 */
public class SessionControlConnectionTest extends AppDeviceTestCase {

    List<RawCentralData> mReceivedDataList = new ArrayList<>();

    List<NotificationProtocol.RawNotification> mReceivedNotifications = new ArrayList<>();

    CentralDataReceiver mCentralDataReceiver;

    @Override
    public void onSetup() {
        super.onSetup();

        // システム関連をもとに戻して、DB接続先を正常にする
        Garnet.override(AppStorageProvider.class, AppStorageProvider.class);
        // Serviceを強制切断
        getApplication().stopService(new Intent(getContext(), CentralSessionService.class));

        mCentralDataReceiver = new CentralDataReceiver(getContext()) {
            @Override
            public void onReceived(@NonNull RawCentralData central) {
                mReceivedDataList.add(central);
                super.onReceived(central);
            }

            @Override
            public void onReceived(@NonNull NotificationProtocol.RawNotification notification) {
                mReceivedNotifications.add(notification);
                super.onReceived(notification);
            }
        };
        mCentralDataReceiver.connect();
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        // Serviceを強制切断
        getApplication().stopService(new Intent(getContext(), CentralSessionService.class));

        mCentralDataReceiver.disconnect();
    }

    @Test
    public void WiFiをオフにできる() throws Throwable {
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        assertTrue(wifiManager.setWifiEnabled(false));
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
            IntHolder sessionStartCount = new IntHolder();
            final long startTime = System.currentTimeMillis();

            SessionControlConnection connection = new SessionControlConnection(getContext());
            assertTrue(connection.connect(() -> false));
            connection.registerSessionStateChangeListener(new SessionControlConnection.OnSessionStateChangeListener() {
                @Override
                public void onSessionStarted(SessionControlConnection connection, RawSessionInfo info) {
                    sessionStartCount.add(1);
                }

                @Override
                public void onSessionStopped(SessionControlConnection connection, RawSessionInfo info) {

                }
            });

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

            // コールバックが送られている
            Util.sleep(500);
            assertEquals(sessionStartCount.value, 1);

            // セッションを切断する
            assertTrue(connection.disconnect(() -> false));
            Util.sleep(1000 * 1);

            // セッションが行きて走行を継続していなければならない
            assertTrue(CentralSessionService.isRunning(getContext())); // Service起動後
        }

        // 通知等をチェックする
        Util.sleep(1000 * 10);

        // Wi-Fiが連動している
        assertEquals(
                Garnet.instance(AppContextProvider.class, AppSettings.class).getCentralSettings().isWifiDisable(),
                !((WifiManager) getContext().getSystemService(Context.WIFI_SERVICE)).isWifiEnabled()
        );

        // Broadcastが発生している
        validate(mReceivedDataList).sizeFrom(1).sizeTo(20).allNotNull();
        validate(mReceivedNotifications).sizeFrom(1).sizeTo(20).allNotNull();

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
