package com.eaglesakura.andriders.computer;

import com.eaglesakura.android.thread.async.AsyncTaskController;

import android.content.Context;

/**
 *
 */
public abstract class CycleComputerManager {
    /**
     * 処理用パイプライン
     */
    private static final AsyncTaskController gPipeline = new AsyncTaskController(1, 1000 * 5);

    protected final AsyncTaskController mPipeline;

    protected final Context mContext;

    protected CycleComputerManager(Context context) {
        mContext = context.getApplicationContext();
        mPipeline = gPipeline;
    }

    public Context getContext() {
        return mContext;
    }

    public AsyncTaskController getPipeline() {
        return mPipeline;
    }

    public static AsyncTaskController getGlobalPipeline() {
        return gPipeline;
    }

    /**
     * 定時更新を行わせる
     *
     * このメソッドは外部からパイプラインに流し込まれるため、必ずパイプラインスレッドから呼び出される。
     * そのため、この内部でパイプラインに処理を流す必要はない。
     */
    public abstract void updateInPipeline(double deltaTimeSec);
}

