package com.eaglesakura.andriders.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionState;
import com.eaglesakura.andriders.util.AppLog;
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

    SessionNotification(@NonNull Callback callback) {
        mCallback = callback;
    }

    public static SessionNotification attach(CentralSession session, @NonNull Callback callback) {
        SessionNotification result = new SessionNotification(callback);

        session.registerStateBus(result);
        session.registerDataBus(result);

        return result;
    }

    /**
     * Sessionのステート変更通知をハンドリングする
     */
    @Subscribe
    private void onSessionStateChanged(SessionState.Bus state) {
        // 必要に応じてハンドリングを追加する
        AppLog.system("SessionState ID[%d] Changed[%s]", state.getSession().getSessionId(), state.getState());

        Service service = mCallback.getService(this);
        CentralSession session = state.getSession();
        if (state.getState() == SessionState.State.Initializing) {
            // 初期化中
            onStartSessionInitialize(service, session);
        } else if (state.getState() == SessionState.State.Running) {
            // 実行中に切り替わった
            onStartSession(service, session);
        } else if (state.getState() == SessionState.State.Stopping) {
            // セッション終了とする
            onStopSession(service, session);
        }
    }

    /**
     * セッションが開始された
     */
    @UiThread
    private void onStartSessionInitialize(Service service, CentralSession session) {
        mNotification = NotificationBuilder.from(service)
                .ticker(R.string.Word_App_AndridersCentralEngine)
                .title(R.string.Word_App_AndridersCentralEngine)
                .icon(R.mipmap.ic_launcher)
                .showForeground(NOTIFICATION_ID);

        // 表示内容を切り替える
        setContent(RemoteViewsBuilder.from(service, R.layout.display_notification_initialize)
                .build()
                .setOnClickListener(R.id.Item_Root, (self, viewId) -> {
                    mCallback.onClickNotification(SessionNotification.this);
                }));
    }

    /**
     * セッションの初期化が完了した
     */
    @UiThread
    private void onStartSession(Service service, CentralSession session) {
        // 表示内容を切り替える
        setContent(RemoteViewsBuilder.from(service, R.layout.display_notification_running)
                .build()
                .setOnClickListener(R.id.Item_Root, (self, viewId) -> {
                    mCallback.onClickNotification(SessionNotification.this);
                }));
    }

    /**
     * セッションが終了した
     */
    @UiThread
    private void onStopSession(Service service, CentralSession session) {
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
         * 管理対象のServiceを取得する
         */
        Service getService(SessionNotification self);

        /**
         * 通知をクリックされた
         */
        void onClickNotification(SessionNotification self);
    }
}
