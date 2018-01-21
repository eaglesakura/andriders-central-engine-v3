package com.eaglesakura.andriders.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionState;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.sloth.app.lifecycle.Lifecycle;
import com.eaglesakura.sloth.view.SupportNotification;
import com.eaglesakura.sloth.view.SupportRemoteViews;
import com.eaglesakura.sloth.view.builder.NotificationBuilder;
import com.eaglesakura.sloth.view.builder.RemoteViewsBuilder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

/**
 * セッション表示用の通知を管理する
 */
public class CentralStatusBar {

    private static final int NOTIFICATION_ID = 0x1200;

    /**
     * 通知ID
     */
    private SupportNotification mNotification;

    /**
     * 通知用のView
     */
    private SupportRemoteViews mNotificationViews;

    /**
     * 制御コールバック
     */
    @NonNull
    private final Callback mCallback;

    @NonNull
    private final Service mService;

    @NonNull
    private final CentralSession mSession;

    public CentralStatusBar(Service service, Lifecycle lifecycle, CentralSession session, @NonNull Callback callback) {
        mService = service;
        mCallback = callback;
        mSession = session;
        session.getStateStream().observe(lifecycle, this::observeSessionState);
    }

    /**
     * Sessionのステート変更通知をハンドリングする
     */
    private void observeSessionState(SessionState state) {
        AppLog.system("SessionState ID[%d] Changed[%s]", mSession.getSessionId(), state.getState());

        if (state.getState() == SessionState.State.Initializing) {
            // 初期化中
            onStartSessionInitialize(mService);
        } else if (state.getState() == SessionState.State.Running) {
            // 実行中に切り替わった
            onStartSession(mService);
        } else if (state.getState() == SessionState.State.Stopping) {
            // セッション終了とする
            onStopSession(mService);
        }
    }

    /**
     * セッションが開始された
     */
    @UiThread
    private void onStartSessionInitialize(Service service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NotificationBuilder.CHANNEL_DEFAULT, "default", NotificationManager.IMPORTANCE_HIGH);
            channel.setLightColor(Color.RED);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
        mNotification = NotificationBuilder.from(service)
                .ticker(R.string.Word_Common_AndridersCentralEngine)
                .title(R.string.Word_Common_AndridersCentralEngine)
                .icon(R.mipmap.ic_launcher)
                .showForeground(NOTIFICATION_ID);

        // 表示内容を切り替える
        setContent(RemoteViewsBuilder.from(service, R.layout.display_notification_initialize)
                .build()
                .setOnClickListener(R.id.Item_Root, (self, viewId) -> mCallback.onClickNotification(CentralStatusBar.this)));
    }

    /**
     * セッションの初期化が完了した
     */
    @UiThread
    private void onStartSession(Service service) {
        // 表示内容を切り替える
        setContent(RemoteViewsBuilder.from(service, R.layout.display_notification_running)
                .build()
                .setOnClickListener(R.id.Item_Root, (self, viewId) -> mCallback.onClickNotification(CentralStatusBar.this))
                .setOnClickListener(R.id.Button_ViewToggle, (self, viewId) -> mCallback.onClickToggleDisplay(CentralStatusBar.this))
        );
    }

    /**
     * セッションが終了した
     */
    @UiThread
    private void onStopSession(Service service) {
        if (mNotification != null) {
            service.stopForeground(true);
            mNotification.cancel();
            mNotification = null;
        }

        if (mNotificationViews != null) {
            mNotificationViews.dispose();
            mNotificationViews = null;
        }
    }

    /**
     * コンテンツを更新する
     */
    @UiThread
    public void setContent(SupportRemoteViews views) {
        if (mNotificationViews != null) {
            mNotificationViews.dispose();
        }
        mNotificationViews = views;

        if (views != null) {
            mNotification.content(views.getRemoteViews()).appy();
        }
    }

    public interface Callback {
        /**
         * 通知をクリックされた
         */
        void onClickNotification(CentralStatusBar self);

        /**
         * 表示・非表示のトグルが実行された
         */
        void onClickToggleDisplay(CentralStatusBar self);
    }
}
