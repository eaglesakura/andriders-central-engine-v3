package com.eaglesakura.android.rx;

import com.eaglesakura.android.thread.ui.UIHandler;

import android.support.annotation.UiThread;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class SubscriptionWrapper {
    private List<Runnable> mRunnables = new ArrayList<>();
    private boolean mPending = true;
    private CompositeSubscription mSubscription;

    public SubscriptionWrapper(CompositeSubscription subscription) {
        mSubscription = subscription;
    }

    public CompositeSubscription getSubscription() {
        return mSubscription;
    }

    public boolean isUnsubscribed() {
        return mSubscription.isUnsubscribed();
    }

    public void clear() {
        mSubscription.clear();
    }

    public void unsubscribe() {
        mSubscription.unsubscribe();
    }

    public void remove(Subscription s) {
        mSubscription.remove(s);
    }

    public boolean hasSubscriptions() {
        return mSubscription.hasSubscriptions();
    }

    public void add(Subscription s) {
        mSubscription.add(s);
    }

    public boolean hasPendingTasks() {
        return !mRunnables.isEmpty();
    }

    @UiThread
    public void onPause() {
        mPending = true;
    }

    @UiThread
    public void onResume() {
        mPending = false;

        if (mRunnables.isEmpty()) {
            return;
        }

        // キューを実行し、キャッシュクリアする
        final List<Runnable> copy = new ArrayList<>(mRunnables);
        UIHandler.postUI(() -> {
            // 実行の必要がなければ何もしない。
            if (mSubscription.isUnsubscribed()) {
                return;
            }

            for (Runnable item : copy) {
                item.run();
            }
        });
        mRunnables.clear();
    }

    /**
     * 実行クラスを渡し、処理を行わせる。
     *
     * 実行保留中であれば一旦キューに貯め、resumeのタイミングでキューを全て実行させる。
     */
    public void run(Runnable callback) {
        if (mPending) {
            mRunnables.add(callback);
        } else {
            callback.run();
        }
    }
}
