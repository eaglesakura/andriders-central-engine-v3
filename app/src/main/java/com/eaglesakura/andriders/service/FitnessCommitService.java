package com.eaglesakura.andriders.service;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.google.GoogleFitUploader;
import com.eaglesakura.andriders.ui.navigation.log.LogSummaryBinding;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;
import com.eaglesakura.sloth.view.SupportRemoteViews;
import com.eaglesakura.sloth.view.builder.RemoteViewsBuilder;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Google Fitへのデータアップロードを行う
 */
public class FitnessCommitService extends IntentService {
    static final int NOTIFICATION_ID = 0x1212;

    /**
     * サンプリング対象のセッションID
     * これを含んだセッションをすべてアップロードする
     */
    static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";

    Notification mNotification;

    NotificationManager mNotificationManager;

    PowerManager.WakeLock mCpuLock;
    private SupportRemoteViews mNotificationView;

    public FitnessCommitService() {
        super("GoogleFitUpload");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Garnet.inject(this);

        mNotificationView = RemoteViewsBuilder.from(this, R.layout.upload_notification).build();

        // notificationを表示する
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        builder.setTicker(getString(R.string.Env_AppName));
        builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_google_fit));
        builder.setSmallIcon(R.mipmap.ic_google_fit);
        builder.setWhen(System.currentTimeMillis());
        builder.setContent(mNotificationView.getRemoteViews());
        mNotification = builder.build();
        startForeground(NOTIFICATION_ID, mNotification);
        mCpuLock = ContextUtil.cpuWakeLock(this, this);
    }

    @Override
    public void onDestroy() {
        mCpuLock.release();
        mNotificationView.dispose();
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // タイムアウト付きでキャンセルチェック
            // 1時間以内に更新する
            CancelCallback cancelCallback =
                    SupportCancelCallbackBuilder.from(() -> false)
                            .orTimeout(1000 * 60 * 60, TimeUnit.MILLISECONDS).build();

            long sessionId = intent.getLongExtra(EXTRA_SESSION_ID, 0);
            GoogleFitUploader uploader = GoogleFitUploader.Builder.from(this)
                    .session(sessionId)
                    .build();

            uploader.uploadDaily(new GoogleFitUploader.UploadCallback() {
                @Override
                public void onUploadStart(GoogleFitUploader self, SessionHeader session) {
                    AppLog.system("GoogleFit upload start [%d]", session.getSessionId());
                    UIHandler.await(() -> {
                        Date startDate = new Date(session.getSessionId());
                        Date endDate = session.getEndDate();

                        String message =
                                getString(R.string.Message_Fit_Notification,
                                        LogSummaryBinding.DEFAULT_DAY_FORMATTER.format(startDate),
                                        LogSummaryBinding.DEFAULT_TIME_FORMATTER.format(startDate),
                                        LogSummaryBinding.DEFAULT_TIME_FORMATTER.format(endDate)
                                );
                        updateNotification(message);
                        return 0;
                    });
                }

                @Override
                public void onUploadCompleted(GoogleFitUploader self, SessionHeader session) {
                    AppLog.system("GoogleFit upload completed [%d]", session.getSessionId());
                }
            }, cancelCallback);

            String uploadedDay = LogSummaryBinding.DEFAULT_DAY_FORMATTER.format(new Date(sessionId));

            // アップロード成功メッセージ
            UIHandler.postUI(() -> addNotification(getString(R.string.Message_Fit_Completed, uploadedDay)));
        } catch (Exception e) {
            AppLog.report(e);
            // アップロード失敗メッセージ
            UIHandler.postUI(() -> addNotification(getString(R.string.Message_Fit_UploadFailed)));
        }
    }

    /**
     * アップロード失敗時のメッセージを流す
     */
    void addNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        builder.setTicker(getString(R.string.Env_AppName));
        builder.setSmallIcon(R.mipmap.ic_google_fit);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentText(message);
        // 通知を表示する
        mNotificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    /**
     * 通知を更新する
     */
    void updateNotification(String message) {
        AppLog.system("New Message[%s]", message);
        mNotificationView.getRemoteViews().setTextViewText(R.id.Item_Message, message);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    public static class Builder {
        Intent mIntent;

        long mSessionId;

        public static Builder from(Context context) {
            Builder builder = new Builder();
            builder.mIntent = new Intent(context, FitnessCommitService.class);
            return builder;
        }

        public Builder session(SessionHeader session) {
            mSessionId = session.getSessionId();
            return this;
        }

        public Builder session(long sessionId) {
            mSessionId = sessionId;
            return this;
        }

        public Intent build() {
            mIntent.putExtra(EXTRA_SESSION_ID, mSessionId);
            return mIntent;
        }
    }
}
