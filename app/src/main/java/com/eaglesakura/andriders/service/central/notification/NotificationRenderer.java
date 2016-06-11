package com.eaglesakura.andriders.service.central.notification;

import com.eaglesakura.andriders.service.central.CentralContext;
import com.eaglesakura.android.thread.loop.HandlerLoopController;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.app.Service;

/**
 *
 */
public class NotificationRenderer {
    final Service mBaseService;

    /**
     * 通知フレームレート
     */
    final int NOTIFICATION_FRAME_RATE = 30;

    /**
     * 通知が残っている間描画する秒数
     */
    final int NOTIFICATION_RENDERING_SEC = 5;

    /**
     * データ
     */
    CentralContext mCentralContext;

    /**
     * 通知表示用のView
     */
    NotificationView mNotificationView;

    /**
     * ループを管理する
     */
    HandlerLoopController mLoopController;

    public NotificationRenderer(Service service, CentralContext centralContext) {
        mBaseService = service;
        mCentralContext = centralContext;
    }

    public void connect() {
        mLoopController = new HandlerLoopController(UIHandler.getInstance()) {
            @Override
            protected void onUpdate() {
                if (mNotificationView != null) {
                    mNotificationView.invalidate();
                }
            }
        };
        mLoopController.connect();
    }

    public void dispose() {
        mLoopController.disconnect();
        mLoopController = null;
    }
}
