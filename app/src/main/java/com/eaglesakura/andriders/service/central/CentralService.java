package com.eaglesakura.andriders.service.central;

import com.eaglesakura.andriders.service.base.AppBaseService;
import com.eaglesakura.andriders.service.central.display.DisplayRenderer;
import com.eaglesakura.andriders.service.central.notification.NotificationRenderer;
import com.eaglesakura.andriders.service.central.status.CentralUiManager;
import com.eaglesakura.android.thread.loop.HandlerLoopController;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.ContextUtil;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class CentralService extends AppBaseService {

    /**
     * UI表示管理
     *
     * ステータスバーとViewを含む
     */
    CentralUiManager mCentralUiManager;

    /**
     * 現在のデータ内容
     */
    CentralContext mCentralContext;

    /**
     * サイコン本体のレンダリング
     */
    DisplayRenderer mDisplayRenderer;


    /**
     * 通知レンダリング
     */
    NotificationRenderer mNotificationRenderer;


    /**
     * ループ管理
     */
    HandlerLoopController mLoopController;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mCentralContext = new CentralContext(this);  // データ初期化
        initializeUiManagers();     // ディスプレイ表示
        initializeUpdateLoop();     // 更新ループを開始する

        // 拡張機能その他
        mCentralContext.onServiceInitializeCompleted();
    }

    /**
     * UI依存系Managerを初期化する
     */
    private void initializeUiManagers() {
        // ステータスバー表示
        {
            mCentralUiManager = new CentralUiManager(this, mCentralContext);
            mCentralUiManager.connect();
        }

        // サイコン表示
        {
            mDisplayRenderer = new DisplayRenderer(this, mCentralContext);
            mDisplayRenderer.setDisplayStub(mCentralUiManager.getDisplayStub());
            mDisplayRenderer.connect();
        }

        // 通知表示
        {
            mNotificationRenderer = new NotificationRenderer(this, mCentralContext);
            mNotificationRenderer.connect();
        }
    }

    /**
     * 更新ループを初期化する
     */
    private void initializeUpdateLoop() {
        mLoopController = new HandlerLoopController(UIHandler.getInstance()) {
            @Override
            protected void onUpdate() {
                final double deltaTimeSec = mLoopController.getDeltaTime();
                mCentralContext.onUpdated(deltaTimeSec);
            }
        };
        mLoopController.setFrameRate(5);
        mLoopController.connect();
    }

    /**
     * 更新ループを削除する
     */
    private void destroyUpdateLoop() {
        // セッション終了
        mLoopController.disconnect();
        mLoopController.dispose();
        mLoopController = null;
    }

    /**
     * UIを削除する
     */
    private void destroyUiManagers() {
        mNotificationRenderer.dispose();
        mNotificationRenderer = null;

        mDisplayRenderer.dispose();
        mDisplayRenderer = null;

        mCentralUiManager.disconnect();
        mCentralUiManager = null;
    }

    @Override
    public void onDestroy() {
        destroyUpdateLoop();
        destroyUiManagers();

        mCentralContext.dispose();

        super.onDestroy();

    }


    public static void start(Context context) {
        context = context.getApplicationContext();
        Intent intent = new Intent(context, CentralService.class);
        context.startService(intent);
    }

    public static void stop(Context context) {
        context = context.getApplicationContext();
        Intent intent = new Intent(context, CentralService.class);
        context.stopService(intent);
    }

    public static boolean isRunning(Context context) {
        return ContextUtil.isServiceRunning(context, CentralService.class);
    }
}
