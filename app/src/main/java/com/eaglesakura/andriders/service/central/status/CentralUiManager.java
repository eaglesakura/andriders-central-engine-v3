package com.eaglesakura.andriders.service.central.status;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.service.central.CentralContext;
import com.eaglesakura.andriders.service.central.notification.NotificationView;
import com.eaglesakura.android.util.ContextUtil;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class CentralUiManager {
    static final int NOTIFICATION_ID_FORGROUND_MENU = 0x3103;

    final Service mService;

    final CentralContext mCentralContext;

    Notification mUiNotification;

    /**
     * サイコンのKey-Value表示ディスプレイ
     */
    ViewGroup mDataDisplay;

    /**
     * 通知表示用ディスプレイ
     */
    ViewGroup mNotificationDisplay;

    WindowManager mWindowManager;

    public CentralUiManager(Service service, CentralContext centralContext) {
        this.mService = service;
        this.mCentralContext = centralContext;
    }

    public ViewGroup getDisplayStub() {
        return (ViewGroup) mDataDisplay.findViewById(R.id.Service_Central_Display_Root);
    }

    public NotificationView getNotificationView() {
        return (NotificationView) mNotificationDisplay.findViewById(R.id.Service_Central_Notification);
    }

    private void initializeForground() {
        Notification.Builder builder = new Notification.Builder(mService);
        builder.setAutoCancel(true);
        builder.setTicker("ACE Service");
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle("ACEs");
        builder.setContentText("タップでサイコン設定を開きます");
        builder.setSmallIcon(R.mipmap.ic_launcher);

        mUiNotification = builder.build();

        mService.startForeground(NOTIFICATION_ID_FORGROUND_MENU, mUiNotification);
    }

    private void initDisplayView() {
        mDataDisplay = (ViewGroup) View.inflate(mService, R.layout.central_display, null);

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

        mWindowManager.addView(mDataDisplay, layoutParams);
    }

    private void initNotificationDisplay() {
        mNotificationDisplay = (ViewGroup) ContextUtil.getInflater(mService).inflate(R.layout.central_notification, null);

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

        mWindowManager.addView(mNotificationDisplay, layoutParams);
    }

    public void connect() {
        initializeForground();
        initDisplayView();
        initNotificationDisplay();
    }

    public void disconnect() {
        mService.stopForeground(true);
        mWindowManager.removeView(mDataDisplay);
        mWindowManager.removeView(mNotificationDisplay);
    }

}
