package com.eaglesakura.andriders.service.ui;

import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.android.framework.delegate.task.DataBus;

import android.support.annotation.Nullable;

/**
 * アニメーションの1フレームデータ
 */
public class AnimationFrame {
    /**
     * 現在のフレーム数
     */
    int mFrameCount;

    /**
     * 前のフレームからの差分時間
     */
    double mDeltaSec;

    CentralSession mSession;

    AnimationFrame(CentralSession session, int frameCount, double deltaSec) {
        mSession = session;
        mFrameCount = frameCount;
        mDeltaSec = deltaSec;
    }

    public AnimationFrame() {
        mFrameCount = 0;
        mDeltaSec = 0.01;
    }

    public CentralSession getSession() {
        return mSession;
    }

    public int getFrameCount() {
        return mFrameCount;
    }

    public double getDeltaSec() {
        return mDeltaSec;
    }

    public static class Bus extends DataBus<AnimationFrame> {
        public Bus(@Nullable AnimationFrame data) {
            super(data);
        }

        public CentralSession getSession() {
            return getData().getSession();
        }

        public int getFrameCount() {
            return getData().getFrameCount();
        }

        public double getDeltaSec() {
            return getData().getDeltaSec();
        }

        public void onUpdate(CentralSession session, double deltaSec) {
            // 更新通知を出す
            AnimationFrame oldData = getData();
            if (oldData == null) {
                modified(new AnimationFrame(session, 0, deltaSec));
            } else {
                modified(new AnimationFrame(session, oldData.mFrameCount + 1, deltaSec));
            }
        }
    }
}
