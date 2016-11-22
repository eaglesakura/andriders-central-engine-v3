package com.eaglesakura.andriders.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionState;
import com.eaglesakura.material.widget.NotificationBuilder;
import com.eaglesakura.material.widget.RemoteViewsBuilder;
import com.eaglesakura.material.widget.support.SupportNotification;
import com.eaglesakura.material.widget.support.SupportRemoteViews;
import com.squareup.otto.Subscribe;

import android.app.Service;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

/**
 * セッション表示用の通知を管理する
 */
public class SessionNotification {

    static final int NOTIFICATION_ID = 0x1200;

    @NonNull
    final Service mService;

    /**
     * 通知ID
     */
    SupportNotification mNotification;

    /**
     * 通知用のView
     */
    SupportRemoteViews mNotificationViews;

    /**
     * 制御コールバック
     */
    @NonNull
    final Callback mCallback;

    public SessionNotification(@NonNull Service service, Callback callback) {
        mService = service;
        mCallback = callback;
    }

    /**
     * セッションが開始された
     */
    @UiThread
    public void onStartSession(CentralSession session) {
        session.registerStateBus(this);

        mNotification = NotificationBuilder.from(mService)
                .ticker(R.string.Word_App_AndridersCentralEngine)
                .title(R.string.Word_App_AndridersCentralEngine)
                .icon(R.mipmap.ic_launcher)
                .showForeground(NOTIFICATION_ID);

        setContent(RemoteViewsBuilder.from(mService, R.layout.display_notification_initialize)
                .build()
                .setOnClickListener(R.id.Item_Root, (self, viewId) -> {
                    mCallback.onClickNotification(SessionNotification.this);
                }));
    }

    /**
     * コンテンツを更新する
     */
    public void setContent(SupportRemoteViews views) {
        if (mNotificationViews != null) {
            mNotificationViews.dispose();
        }
        mNotificationViews = views;

        if (views != null) {
            mNotification.content(views.getRemoteViews()).appy();
        }
    }

    /**
     * セッションが終了した
     */
    @UiThread
    public void onStopSession(CentralSession session) {
        if (mNotification != null) {
            mService.stopForeground(true);
            mNotification.cancel();
            mNotification = null;
        }
        if (mNotificationViews != null) {
            mNotificationViews.dispose();
            mNotificationViews = null;
        }
    }

    @Subscribe
    private void onSessionStateChanged(SessionState.Bus state) {
        if (state.getState() == SessionState.State.Running) {
            // Runningに切り替わったので、通知を変更する
        }
    }

    public interface Callback {
        /**
         * 通知をクリックされた
         */
        void onClickNotification(SessionNotification self);
    }
}
