package com.eaglesakura.andriders.computer;

import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;

import android.content.Context;

/**
 *
 */
public abstract class CycleComputerManager {

    protected final Context mContext;

    protected final Settings mSettings = Settings.getInstance();

    protected final SubscriptionController mSubscription;

    public CycleComputerManager(Context context, SubscriptionController subscription) {
        mContext = context;
        mSubscription = subscription;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * パイプラインで処理を行わせる
     */
    public <T> RxTask<T> execute(RxTask.Async<T> action) {
        return new RxTaskBuilder<T>(mSubscription)
                .async(action)
                .subscribeOn(SubscribeTarget.Pipeline)
                .observeOn(ObserveTarget.FireAndForget)
                .start();
    }

    /**
     * 定時更新を行わせる
     *
     * このメソッドは外部からパイプラインに流し込まれるため、必ずパイプラインスレッドから呼び出される。
     * そのため、この内部でパイプラインに処理を流す必要はない。
     */
    public abstract void updateInPipeline(double deltaTimeSec);
}

