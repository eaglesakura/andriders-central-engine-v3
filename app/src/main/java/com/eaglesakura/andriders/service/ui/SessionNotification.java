package com.eaglesakura.andriders.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.material.widget.NotificationBuilder;
import com.eaglesakura.material.widget.RemoteViewsBuilder;
import com.eaglesakura.material.widget.support.SupportNotification;
import com.eaglesakura.material.widget.support.SupportRemoteViews;

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
    public void onStartSession(SessionInfo session) {

        mNotification = NotificationBuilder.from(mService)
                .ticker(R.string.Word_App_AndridersCentralEngine)
                .title(R.string.Word_App_AndridersCentralEngine)
                .icon(R.mipmap.ic_launcher)
                .showForeground(NOTIFICATION_ID);

        setContent(RemoteViewsBuilder.from(mService, R.layout.service_session_notification_initialize)
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
    public void onStopSession() {
        if (mNotification != null) {
            mNotification.cancel();
            mNotification = null;
        }
        if (mNotificationViews != null) {
            mNotificationViews.dispose();
            mNotificationViews = null;
        }
    }

    public interface Callback {
        /**
         * 通知をクリックされた
         */
        void onClickNotification(SessionNotification self);
    }
}
