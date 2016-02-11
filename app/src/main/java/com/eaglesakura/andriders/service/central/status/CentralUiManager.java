package com.eaglesakura.andriders.service.central.status;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.service.central.CentralContext;
import com.eaglesakura.andriders.service.central.notification.NotificationView;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CentralUiManager {
    static final int NOTIFICATION_ID_FORGROUND_MENU = 0x3103;

    final Service mService;

    final CentralContext mCentralContext;

    Notification mUiNotification;

    /**
     * Root
     */
    ViewGroup mRootDisplay;

    /**
     * サイコン表示用のView
     */
    @Bind(R.id.Service_Central_Display_Root)
    ViewGroup mDisplayStub;

    /**
     * 通知用のView
     */
    @Bind(R.id.Service_Central_Notification)
    NotificationView mNotificationView;

    WindowManager mWindowManager;

    public CentralUiManager(Service service, CentralContext centralContext) {
        this.mService = service;
        this.mCentralContext = centralContext;
    }

    public ViewGroup getDisplayStub() {
        return mDisplayStub;
    }

    public NotificationView getNotificationView() {
        return mNotificationView;
    }

    private void initializeForground() {
        Notification.Builder builder = new Notification.Builder(mService);
        builder.setAutoCancel(true);
        builder.setTicker("ACE Service");
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle("ACEs");
        builder.setContentText("タップでサイコン設定を開きます");
        builder.setSmallIcon(R.drawable.ic_launcher);

        mUiNotification = builder.build();

        mService.startForeground(NOTIFICATION_ID_FORGROUND_MENU, mUiNotification);
    }

    private void initializeDisplayView() {
        mRootDisplay = (ViewGroup) View.inflate(mService, R.layout.service_cycle_display, null);
        ButterKnife.bind(this, mRootDisplay);
        if (mDisplayStub == null || mNotificationView == null) {
            throw new IllegalStateException();
        }

        mWindowManager = (WindowManager) mService.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                // レイアウトの幅 / 高さ設定
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                // レイアウトの挿入位置設定
                // TYPE_SYSTEM_OVERLAYはほぼ最上位に位置して、ロック画面よりも上に表示される。
                // ただし、タッチを拾うことはできない。
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                // ウィンドウ属性
                // TextureViewを利用するには、FLAG_HARDWARE_ACCELERATED が必至となる。
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        //
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON    // スクリーン表示Keep
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                ,
                // 透過属性を持たなければならないため、TRANSLUCENTを利用する
                PixelFormat.TRANSLUCENT);
        mWindowManager.addView(mRootDisplay, layoutParams);
    }

    public void connect() {
        initializeForground();
        initializeDisplayView();
    }

    public void disconnect() {
        mService.stopForeground(true);
        mWindowManager.removeView(mRootDisplay);
    }

}
