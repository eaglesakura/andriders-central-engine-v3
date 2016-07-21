package com.eaglesakura.andriders.service.central.notification;

import com.eaglesakura.andriders.display.notification.NotificationDisplayManager;
import com.eaglesakura.andriders.display.notification.ProximityFeedbackManager;
import com.eaglesakura.android.graphics.Graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * 通知の描画を行うView
 */
public class NotificationView extends View {

    /**
     * 通知レンダリング
     */
    NotificationDisplayManager mNotificationManager;

    /**
     * 近接コマンドフィードバック
     */
    ProximityFeedbackManager mProximityFeedbackManager;

    public NotificationView(Context context) {
        super(context);
    }

    public NotificationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotificationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setNotificationManager(NotificationDisplayManager notificationManager) {
        mNotificationManager = notificationManager;
    }

    public void setProximityFeedbackManager(ProximityFeedbackManager proximityFeedbackManager) {
        mProximityFeedbackManager = proximityFeedbackManager;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        }

        Graphics g = new Graphics(canvas);

        if (mProximityFeedbackManager != null) {
            mProximityFeedbackManager.rendering(g);
        }

        if (mNotificationManager != null) {
            mNotificationManager.rendering(g);
        }
    }
}
