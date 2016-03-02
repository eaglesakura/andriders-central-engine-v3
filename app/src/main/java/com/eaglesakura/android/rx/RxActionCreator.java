package com.eaglesakura.android.rx;

import com.eaglesakura.android.thread.async.AsyncTaskResult;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 *
 */
public class RxActionCreator<T> {
    final SubscriptionWrapper mSubscription;

    final Scheduler mScheduler;

    Observable<T> mObservable;

    /**
     * 完了時処理を記述する
     */
    RxTask.Action1<T> mCompletedCallback;

    /**
     * エラー時の処理を記述する
     */
    RxTask.ErrorAction<T> mErrorCallback;

    /**
     * 最終的に必ず呼び出される処理
     */
    RxTask.Action0 mFinalizeCallback;

    /**
     * キャンセルチェック
     */
    AsyncTaskResult.CancelSignal mCancelSignal;

    /**
     * Task
     */
    RxTask<T> mTask = new RxTask<>();

    public RxActionCreator(SubscriptionWrapper subscription, Scheduler scheduler) {
        mSubscription = subscription;
        mScheduler = scheduler;
    }

    public RxActionCreator<T> async(final RxTask.Async<T> subscribe) {
        mTask.mSubscribe = it -> {
            try {
                it.onNext(subscribe.call(mTask));
                it.onCompleted();
            } catch (Throwable e) {
                it.onError(e);
            }
        };
        mObservable = Observable.create(mTask.mSubscribe)
                .subscribeOn(mScheduler)
                .observeOn(AndroidSchedulers.mainThread());
        return this;
    }

    /**
     * Observableを直接更新する
     */
    public RxActionCreator<T> update(final Action1<Observable<T>> callback) {
        callback.call(mObservable);
        return this;
    }

    /**
     * 戻り値からの処理を記述する
     */
    public RxActionCreator<T> completed(RxTask.Action1<T> callback) {
        mCompletedCallback = callback;
        return this;
    }

    /**
     * エラーハンドリングを記述する
     */
    public RxActionCreator<T> error(RxTask.ErrorAction<T> callback) {
        mErrorCallback = callback;
        return this;
    }

    /**
     * 終了時の処理を記述する
     */
    public RxActionCreator<T> finalized(RxTask.Action0<T> callback) {
        mFinalizeCallback = callback;
        return this;
    }

    /**
     * 処理完了待ちを行い、結果を受け取る
     */
    public T await() throws Throwable {
        return start().toBlocking().first();
    }

    /**
     * セットアップを完了し、処理を開始する
     */
    public Observable<T> start() {
        final Subscription subscribe = mObservable.subscribe(
                // next = completeed
                next -> {
                    mTask.mResult = next;

                    if (mCompletedCallback != null) {
                        mSubscription.run(() -> {
                            mCompletedCallback.call(next, mTask);
                        });
                    }

                    if (mFinalizeCallback != null) {
                        mSubscription.run(() -> {
                            mFinalizeCallback.call(mTask);
                        });
                    }
                },
                // error
                error -> {
                    mTask.mError = error;

                    if (mErrorCallback != null) {
                        mSubscription.run(() -> {
                            mErrorCallback.call(error, mTask);
                        });
                    }

                    if (mFinalizeCallback != null) {
                        mSubscription.run(() -> {
                            mFinalizeCallback.call(mTask);
                        });
                    }
                }
        );

        // 購読対象に追加
        mSubscription.getSubscription().add(subscribe);

        return mObservable;
    }

//    public static <T> RxActionCreator<T> create(SubscriptionWrapper subscription, Executor executor, RxTask.Async<T> subscribe) {
//        RxActionCreator<T> result = new RxActionCreator<>();
//        result.init(subscription, Schedulers.from(executor), subscribe);
//        return result;
//    }
}
