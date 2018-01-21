package com.eaglesakura.andriders.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionState;
import com.eaglesakura.andriders.data.display.DisplayBindManager;
import com.eaglesakura.andriders.data.display.DisplayLayoutManager;
import com.eaglesakura.andriders.data.notification.CentralNotificationManager;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.model.display.DisplayLayoutCollection;
import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.plugin.internal.CentralServiceCommand;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.provider.SessionManagerProvider;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.thread.HandlerLoopController;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.PackageUtil;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.cerberus.ResultCollection;
import com.eaglesakura.sloth.app.lifecycle.Lifecycle;
import com.eaglesakura.util.Timer;
import com.eaglesakura.util.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
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
     * 通知用View
     */
    private CentralNotificationView mNotificationView;

    /**
     * サイコンのKey-Value表示ディスプレイ
     */
    private ViewGroup mDataDisplay;

    @NonNull
    private final Context mContext;

    @NonNull
    private final WindowManager mWindowManager;

    @Inject(SessionManagerProvider.class)
    private CentralNotificationManager mCentralNotificationManager;

    @Inject(SessionManagerProvider.class)
    private DisplayBindManager mCentralDisplayBindManager;

    @Inject(AppManagerProvider.class)
    private DisplayLayoutManager mDisplayLayoutManager;

    /**
     * 現在のレイアウト状態
     */
    @Nullable
    private DisplayLayoutCollection mCurrentDisplayLayout;

    @NonNull
    private final Lifecycle mLifecycle;

    /**
     * 最後にチェックした際のアプリケーションID
     */
    private String mLastTopApplication;

    @NonNull
    private final SessionInfo mSessionInfo;

    @Nullable
    private HandlerLoopController mLoopController;

    private static final int WINDOW_LAYER_TYPE;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android O以降は専用のレイヤーに載せる
            WINDOW_LAYER_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            WINDOW_LAYER_TYPE = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
    }

    public CentralDisplayWindow(@NonNull Context context, @NonNull Lifecycle lifecycle, CentralSession centralSession) {
        mContext = context;
        mLifecycle = lifecycle;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mSessionInfo = centralSession.getSessionInfo();

        centralSession.getStateStream().observe(lifecycle, this::observeSessionState);
        Garnet.create(this)
                .depend(CentralSession.class, centralSession)
                .inject();
    }

    /**
     * 通知レンダリング用のマネージャを取得する
     */
    public CentralNotificationManager getCentralNotificationManager() {
        return mCentralNotificationManager;
    }

    /**
     * ディスプレイとの関連付けマネージャを取得する
     */
    public DisplayBindManager getCentralDisplayBindManager() {
        return mCentralDisplayBindManager;
    }

    /**
     * 通知用ウィンドウを取得する
     */
    public CentralNotificationView getNotificationView() {
        return mNotificationView;
    }

    /**
     * 表示をトグルする
     * トグル対象はサイコンのみで、通知はレンダリングする
     */
    @UiThread
    public void toggleVisible() {
        if (mDataDisplay == null) {
            return;
        }
        if (mDataDisplay.getVisibility() == View.VISIBLE) {
            mDataDisplay.setVisibility(View.INVISIBLE);
        } else {
            mDataDisplay.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Sessionのステート変更通知をハンドリングする
     */
    @UiThread
    private void observeSessionState(SessionState state) {
        AppLog.system("SessionState ID[%d] Changed[%s]", mSessionInfo.getSessionId(), state.getState());
        if (state.getState() == SessionState.State.Running) {
            // ウィンドウを登録する
            initDisplayView();
            initNotificationDisplay();
            refreshDeviceContext();

            // BroadcastReceiver登録
            {
                IntentFilter filter = new IntentFilter();
                filter.addAction(CentralServiceCommand.ACTION_NOTIFICATION_REQUEST);
                mContext.registerReceiver(mCentralDisplayEventReceiver, filter);
            }

            // 毎フレームの描画処理を行う
            mLoopController = new HandlerLoopController(UIHandler.getInstance(), this::onAnimationFrame);
            mLoopController.setFrameRate(30.0);
            mLoopController.connect();
        } else if (state.getState() == SessionState.State.Stopping) {
            // ウィンドウを削除する
            mWindowManager.removeView(mDataDisplay);
            mWindowManager.removeView(mNotificationDisplay);

            mNotificationDisplay = null;
            mNotificationView = null;
            mDataDisplay = null;

            // Receiverを削除
            mContext.unregisterReceiver(mCentralDisplayEventReceiver);

            // レンダリング停止
            if (mLoopController != null) {
                mLoopController.disconnect();
                mLoopController = null;
            }
        }
    }

    /**
     * サイコン用のViewを構築する
     */
    private void initDisplayView() {
        mDataDisplay = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.central_display, null, false);
        ((ViewGroup) mDataDisplay.findViewById(R.id.Container_DisplayRoot)).addView(DisplayLayoutManager.newStubLayout(mContext));

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                // レイアウトの幅 / 高さ設定
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                // レイアウトの挿入位置設定
                // TYPE_SYSTEM_OVERLAYはほぼ最上位に位置して、ロック画面よりも上に表示される。
                // ただし、タッチを拾うことはできない。
                WINDOW_LAYER_TYPE,
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

    /**
     * フィードバック用のViewを構築する
     */
    private void initNotificationDisplay() {
        mNotificationDisplay = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.central_notification, null);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                // レイアウトの幅 / 高さ設定
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                // レイアウトの挿入位置設定
                // TYPE_SYSTEM_OVERLAYはほぼ最上位に位置して、ロック画面よりも上に表示される。
                // ただし、タッチを拾うことはできない。
                WINDOW_LAYER_TYPE,
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

        mNotificationView = ViewUtil.findViewByMatcher(mNotificationDisplay, it -> it instanceof CentralNotificationView);
        mNotificationView.setNotificationManager(mCentralNotificationManager);
    }

    /**
     * 前回のディスプレイ更新時間からの時間
     */
    private double mDisplayDeltaSec;

    /**
     * 前回のデバイス状態チェックからの時間
     */
    private double mDeviceCheckDeltaSec;

    @UiThread
    private void onAnimationFrame(double deltaSec) {
//        AppLog.system("onAnimationFrame delta[%.3f sec]", deltaSec);
        // 通知を更新
        mCentralNotificationManager.onUpdate(deltaSec);

        if (mNotificationView != null) {
            mNotificationView.invalidate();
        }

        mDisplayDeltaSec += deltaSec;
        mDeviceCheckDeltaSec += deltaSec;

        if (mDisplayDeltaSec > 1.0 && mCurrentDisplayLayout != null) {
            for (DisplayLayout layout : mCurrentDisplayLayout.getSource()) {
                mCentralDisplayBindManager.bind(layout, mDataDisplay);
            }
            mDisplayDeltaSec = 0;
        }

        if (mDeviceCheckDeltaSec > 0.5) {
            // データをリフレッシュ
            refreshDeviceContext();
            mDeviceCheckDeltaSec = 0;
        }
    }

    /**
     * デバイス状態を更新する
     */
    @UiThread
    private void refreshDeviceContext() {
        Timer timer = new Timer();
        mLifecycle.async(ExecuteTarget.LocalQueue, CallbackTime.Alive, (BackgroundTask<ResultCollection> task) -> {
            String currentAppPackage = PackageUtil.getTopApplicationPackage(mContext);
            DisplayLayoutCollection collection = mCurrentDisplayLayout;
            if (!currentAppPackage.equals(mLastTopApplication)) {
                // レイアウト構成をリロードする
                collection = mDisplayLayoutManager.listOrDefault(currentAppPackage);
            }
            return new ResultCollection()
                    .put("list", collection)
                    .put("package", currentAppPackage);
        }).completed((result, task) -> {
            mCurrentDisplayLayout = result.get("list");

            String packageName = result.get("package");
            if (mLastTopApplication == null || !mLastTopApplication.equals(packageName)) {
                AppLog.system("Loaded package[%s] Layout[%d] LoadTime[%d ms]", packageName, mCurrentDisplayLayout.size(), timer.end());
            }
            mLastTopApplication = packageName;
        }).failed((error, task) -> {
            AppLog.report(error);
        }).start();
    }

    final BroadcastReceiver mCentralDisplayEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CentralServiceCommand.ACTION_NOTIFICATION_REQUEST.equals(action)) {
                byte[] data = intent.getByteArrayExtra(CentralServiceCommand.EXTRA_NOTIFICATION_DATA);
                if (data == null) {
                    return;
                }
                Util.safeIfPresent(getCentralNotificationManager(), it -> it.queue(new NotificationData(mContext, data)));
            }
        }
    };


}
