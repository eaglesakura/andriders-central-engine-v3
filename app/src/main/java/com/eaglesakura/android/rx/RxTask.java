package com.eaglesakura.android.rx;

import rx.Observable;

/**
 *
 */
public class RxTask<T> {
    CancelSignal mCancelSignal;

    Observable.OnSubscribe<T> mSubscribe;

    Throwable mError;

    T mResult;

    /**
     * 戻り値を取得する
     */
    public T getResult() {
        return mResult;
    }

    /**
     * エラー内容を取得する
     */
    public Throwable getError() {
        return mError;
    }

    /**
     * エラーを持っていたら投げる
     */
    public void throwIfError() throws Throwable {
        if (mError != null) {
            throw mError;
        }
    }

    public boolean isCanceled() {
        if (mCancelSignal == null) {
            return false;
        }
        return mCancelSignal.isCanceled();
    }

    /**
     * 非同期処理を記述する
     */
    public interface Async<T> {
        T call(RxTask<T> task) throws Throwable;
    }

    /**
     * コールバックを記述する
     */
    public interface Action0<T> {
        void call(RxTask<T> task);
    }

    /**
     * 非同期処理後のコールバックを記述する
     */
    public interface Action1<T> {
        void call(T it, RxTask<T> task);
    }


    /**
     * 非同期処理後のコールバックを記述する
     */
    public interface ErrorAction<T> {
        void call(Throwable it, RxTask<T> task);
    }

    /**
     * キャンセルチェック用のコールバック
     * <p/>
     * cancel()メソッドを呼び出すか、このコールバックがisCanceled()==trueになった時点でキャンセル扱いとなる。
     */
    public interface CancelSignal {
        /**
         * キャンセルする場合はtrueを返す
         */
        boolean isCanceled();
    }

}
