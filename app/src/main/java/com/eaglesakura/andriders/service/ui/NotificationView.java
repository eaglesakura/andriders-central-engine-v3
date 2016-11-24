package com.eaglesakura.andriders.service.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * 通知の描画を行うView
 */
public class NotificationView extends View {

//    /**
//     * 通知レンダリング
//     */
//    NotificationDisplayManager mNotificationManager;
//
//    /**
//     * 近接コマンドフィードバック
//     */
//    ProximityFeedbackManager mProximityFeedbackManager;

    public NotificationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    public void setNotificationManager(NotificationDisplayManager notificationManager) {
//        mNotificationManager = notificationManager;
//    }
//
//    public void setProximityFeedbackManager(ProximityFeedbackManager proximityFeedbackManager) {
//        mProximityFeedbackManager = proximityFeedbackManager;
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        if (isInEditMode()) {
//            return;
//        }
//
//        Graphics g = new Graphics(canvas);
//
//        if (mProximityFeedbackManager != null) {
//            mProximityFeedbackManager.rendering(g);
//        }
//
//        if (mNotificationManager != null) {
//            mNotificationManager.rendering(g);
//        }
//    }
}
