package com.eaglesakura.andriders.service.central.notification;

import com.eaglesakura.andriders.computer.notification.NotificationManager;
import com.eaglesakura.android.graphics.Graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * 通知の描画を行うView
 */
public class NotificationView extends View {
    NotificationManager mNotificationManager;

    public NotificationView(Context context) {
        super(context);
    }

    public NotificationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotificationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setNotificationManager(NotificationManager notificationManager) {
        mNotificationManager = notificationManager;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mNotificationManager == null || isInEditMode()) {
            return;
        }

        Graphics g = new Graphics(canvas);
        mNotificationManager.rendering(g);
    }
}
