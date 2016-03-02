package com.eaglesakura.android.rx;

/**
 *
 */
public class RxTask<T> {

    /**
     * 外部から指定されたキャンセルチェック
     */
    CancelSignal mUserCancelSignal;

    /**
     * 購読対象からのキャンセルチェック
     */
    CancelSignal mSubscribeCancelSignal;

    Throwable mError;

    T mResult;

    State mState = State.Building;

    /**
     * コールバック対象を指定する
     *
     * デフォルトはFire And Forget
     */
    ObserveTarget mObserveTarget = ObserveTarget.FireAndForget;

    public enum State {
        /**
         * タスクを生成中
         */
        Building,

        /**
         * まだ実行されていない
         */
        Pending,

        /**
         * タスクが実行中
         */
        Running,

        /**
         * 完了
         */
        Finished,
    }

    /**
     * 現在のタスク状態を取得する
     */
    public State getState() {
        return mState;
    }

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

    /**
     * タスクがキャンセル状態であればtrue
     */
    public boolean isCanceled() {
        if (mUserCancelSignal != null && mUserCancelSignal.isCanceled()) {
            return true;
        }

        if (mSubscribeCancelSignal != null & mSubscribeCancelSignal.isCanceled()) {
            return true;
        }

        return false;
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
