package com.eaglesakura.andriders.service.ui;

import com.eaglesakura.andriders.data.notification.CentralNotificationManager;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.service.command.ProximityFeedbackManager;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.graphics.Graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * 通知の描画を行うView
 */
public class CentralNotificationView extends View {

    /**
     * 通知レンダリング
     */
    @Inject(AppManagerProvider.class)
    CentralNotificationManager mNotificationManager;

    /**
     * 近接コマンドフィードバック
     */
    ProximityFeedbackManager mProximityFeedbackManager;

    public CentralNotificationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNotificationManager(CentralNotificationManager notificationManager) {
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
//        g.setColorARGB(0xFF000000 | RandomUtil.randInt32());
//        g.fillRect(0, 0, 512, 512);

        if (mProximityFeedbackManager != null) {
            mProximityFeedbackManager.rendering(g);
        }

        if (mNotificationManager != null) {
            mNotificationManager.rendering(g);
        }
    }
}
