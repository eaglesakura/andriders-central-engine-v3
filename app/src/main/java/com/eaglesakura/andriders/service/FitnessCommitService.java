package com.eaglesakura.andriders.service;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.ui.navigation.log.LogSummaryBinding;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.gms.client.PlayServiceConnection;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.support.SupportCancelCallbackBuilder;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Google Fitへのデータアップロードを行う
 */
public class FitnessCommitService extends IntentService {
    static final int NOTIFICATION_ID = 0x1212;

    @Inject(AppManagerProvider.class)
    CentralLogManager mCentralLogManager;

    /**
     * サンプリング対象のセッションID
     * これを含んだセッションをすべてアップロードする
     */
    static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";

    Notification mNotification;

    NotificationManager mNotificationManager;

    public FitnessCommitService() {
        super("GoogleFitUpload");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Garnet.inject(this);

        // notificationを表示する
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        builder.setTicker(getString(R.string.Env_AppName));
        builder.setSmallIcon(R.mipmap.ic_google_fit);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentText(getString(R.string.Message_Log_UploadToGoogleFit));

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_google_fit));
        mNotification = builder.build();

        startForeground(NOTIFICATION_ID, mNotification);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // タイムアウト付きでキャンセルチェック
            CancelCallback cancelCallback =
                    SupportCancelCallbackBuilder.from(() -> false)
                            .orTimeout(1000 * 60 * 30, TimeUnit.MILLISECONDS).build();

            long sessionId = intent.getLongExtra(EXTRA_SESSION_ID, 0);
            UIHandler.await(() -> {
                updateNotification(sessionId);
                return 0;
            });

            try (PlayServiceConnection connection = PlayServiceConnection.newInstance(AppUtil.newFullPermissionClient(this), cancelCallback)) {
                mCentralLogManager.eachDailySessionPoints(sessionId, centralData -> {
                }, cancelCallback);
            }
        } catch (Exception e) {
            AppLog.report(e);
        } finally {

        }
    }

    /**
     * 通知を更新する
     */
    void updateNotification(long sessionId) {
        mNotification.tickerText = getString(R.string.Message_Log_UploadToGoogleFit)
                + " " + LogSummaryBinding.DEFAULT_DAY_FORMATTER.format(new Date(sessionId));
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

}
