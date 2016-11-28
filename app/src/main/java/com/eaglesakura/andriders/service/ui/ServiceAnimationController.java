package com.eaglesakura.andriders.service.ui;

import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.central.service.SessionState;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.android.thread.loop.HandlerLoopController;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.squareup.otto.Subscribe;

import android.support.annotation.NonNull;

/**
 * Serviceの通知やレンダリングを制御するためのメインループを管理する
 */
public class ServiceAnimationController {
    @NonNull
    private final Callback mCallback;

    private ServiceAnimationController(@NonNull Callback callback) {
        mCallback = callback;
    }

    public static ServiceAnimationController attach(ServiceLifecycleDelegate lifecycleDelegate, @NonNull CentralSession session, @NonNull Callback callback) {
        ServiceAnimationController result = new ServiceAnimationController(callback);
        session.getStateBus().bind(lifecycleDelegate, result);
        return result;
    }


    /**
     * Sessionのステート変更通知をハンドリングする
     */
    @Subscribe
    private void onSessionStateChanged(SessionState.Bus state) {
        AppLog.system("SessionState ID[%d] Changed[%s]", state.getSession().getSessionId(), state.getState());

        if (state.getState() == SessionState.State.Running) {
            // 実行中に入った
            CentralSession session = state.getSession();

            // ループ管理を行う
            mLoopController = new HandlerLoopController(UIHandler.getInstance()) {
                @Override
                protected void onUpdate() {
                    mCallback.onUpdate(ServiceAnimationController.this, session, mLoopController.getDeltaTime());
                }
            };
            mLoopController.setFrameRate(30.0); // デフォルトでは30fps, 将来的にはフレームレート変更に対応する
            mLoopController.connect();
        } else if (state.getState() == SessionState.State.Stopping) {
            // 停止中になったらアニメーション停止
            if (mLoopController != null) {
                mLoopController.disconnect();
                mLoopController = null;
            }
        }
    }

    private HandlerLoopController mLoopController;

    public interface Callback {
        /**
         * 毎フレームアップデートの通知を行う
         *
         * @param session  対象セッション
         * @param deltaSec 前回の更新からの時間
         */
        void onUpdate(ServiceAnimationController self, CentralSession session, double deltaSec);
    }
}
