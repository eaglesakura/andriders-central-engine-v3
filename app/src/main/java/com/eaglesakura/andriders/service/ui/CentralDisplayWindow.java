package com.eaglesakura.andriders.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionState;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.squareup.otto.Subscribe;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Session情報レンダリング用のWindowを管理する
 *
 * 管理対象:
 * * 通知カードレンダリングエリア
 * * 近接コマンドレンダリングエリア
 * * サイコン情報レンダリングエリア
 */
public class CentralDisplayWindow {
    /**
     * 通知表示用ディスプレイ
     */
    private ViewGroup mNotificationDisplay;

    /**
     * サイコンのKey-Value表示ディスプレイ
     */
    private ViewGroup mDataDisplay;

    @NonNull
    final Context mContext;

    @NonNull
    final WindowManager mWindowManager;

    CentralDisplayWindow(@NonNull Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public static CentralDisplayWindow attach(@NonNull Context context, ServiceLifecycleDelegate lifecycleDelegate, AnimationFrame.Bus animationFrameBus, @NonNull CentralSession session) {
        CentralDisplayWindow result = new CentralDisplayWindow(context);
        session.getDataBus().bind(lifecycleDelegate, result);
        animationFrameBus.bind(lifecycleDelegate, result);
        return result;
    }

    /**
     * Sessionのステート変更通知をハンドリングする
     */
    @Subscribe
    private void onSessionStateChanged(SessionState.Bus state) {
        AppLog.system("SessionState ID[%d] Changed[%s]", state.getSession().getSessionId(), state.getState());
        if (state.getState() == SessionState.State.Running) {
            // ウィンドウを登録する
            initDisplayView();
            initNotificationDisplay();
        } else if (state.getState() == SessionState.State.Stopping) {
            // ウィンドウを削除する
            mWindowManager.removeView(mDataDisplay);
            mWindowManager.removeView(mNotificationDisplay);
        }
    }

    private void initDisplayView() {
        mDataDisplay = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.central_display, null, false);
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
        mNotificationDisplay = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.central_notification, null);
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

    @Subscribe
    private void onAnimationFrame(AnimationFrame.Bus frame) {
        AppLog.system("onAnimationFrame frame[%d] delta[%.3f sec]", frame.getFrameCount(), frame.getDeltaSec());
        if (mNotificationDisplay != null) {
            mNotificationDisplay.invalidate();
        }
    }
}
