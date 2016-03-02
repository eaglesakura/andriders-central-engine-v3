package com.eaglesakura.android.rx;

import com.eaglesakura.android.thread.ui.UIHandler;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class SubscriptionController {
    private CompositeSubscription mSubscription = new CompositeSubscription();

    /**
     * 各Observeステートごとの保留タスク管理
     */
    private List<StateController> mStateControllers = new ArrayList<>();

    private LifecycleState mState;

    private ThreadController mThreadController = new ThreadController();

    public SubscriptionController() {
        mStateControllers.add(new StateController(ObserveTarget.Forground));
        mStateControllers.add(new StateController(ObserveTarget.Alive));
        mStateControllers.add(new StateController(ObserveTarget.FireAndForget));
    }

    public ThreadController getThreadController() {
        return mThreadController;
    }

    /**
     * 指定したコールバック受付が強制キャンセルならばtrue
     *
     * @param observeTarget 受付先
     */
    public boolean isCanceled(ObserveTarget observeTarget) {
        return mStateControllers.get(observeTarget.ordinal()).isCanceled();
    }

    /**
     * ライフサイクルをバインドする
     */
    public SubscriptionController bind(BehaviorSubject<LifecycleState> behavior) {
        behavior.asObservable().subscribe(next -> {
            mState = next;
            // 廃棄されたらスレッドと購読処理も廃棄する
            if (next == LifecycleState.OnDestroyed) {
                mThreadController.dispose();
                mSubscription.unsubscribe();
            }

            // 保留タスクがあれば流すように促す
            for (StateController ctrl : mStateControllers) {
                ctrl.onNext();
            }
        });
        return this;
    }

    SubscriptionController add(Subscription s) {
        mSubscription.add(s);
        return this;
    }

    /**
     * 実行クラスを渡し、処理を行わせる。
     *
     * 実行保留中であれば一旦キューに貯め、resumeのタイミングでキューを全て実行させる。
     */
    public void run(ObserveTarget target, Runnable callback) {
        mStateControllers.get(target.ordinal()).run(callback);
    }

    /**
     * 各ステートを制御する
     */
    class StateController {
        ObserveTarget mCallbackTarget;

        List<Runnable> mPendings = new ArrayList<>();

        public StateController(ObserveTarget callbackTarget) {
            mCallbackTarget = callbackTarget;
        }

        /**
         * 強制的にキャンセルさせるならばtrue
         */
        boolean isCanceled() {
            if (mCallbackTarget == ObserveTarget.FireAndForget) {
                // 打ちっぱなしならキャンセルはしなくて良い
                return false;
            } else {
                // それ以外なら購読フラグと連動する
                return mSubscription.isUnsubscribed();
            }
        }

        /**
         * 保留状態であればtrue
         */
        boolean isPending() {
            if (mState == null) {
                return true;
            }

            final int beginStateOrder;
            final int endStateOrder;

            switch (mCallbackTarget) {
                case Forground:
                    beginStateOrder = LifecycleState.OnResumed.ordinal();
                    endStateOrder = LifecycleState.OnPaused.ordinal();
                    break;
                case Alive:
                    beginStateOrder = LifecycleState.OnCreated.ordinal();
                    endStateOrder = LifecycleState.OnDestroyed.ordinal();
                    break;
                case FireAndForget:
                    beginStateOrder = LifecycleState.OnCreated.ordinal();
                    endStateOrder = 99999;
                    break;
                default:
                    // not impl
                    throw new IllegalStateException();
            }

            final int currentOrder = mState.ordinal();

            return currentOrder < beginStateOrder
                    || currentOrder > endStateOrder;
        }

        void onNext() {
            if (!isPending() && !mPendings.isEmpty()) {
                // 保留から解除されたら、保留されていたタスクを流す
                List<Runnable> executes = new ArrayList<>(mPendings);
                mPendings.clear();
                UIHandler.postUI(() -> {
                    if (mSubscription.isUnsubscribed()) {
                        // 未購読状態になっているので何もしない
                        return;
                    }

                    for (Runnable call : executes) {
                        call.run();
                    }
                });
            }
        }

        /**
         * コールバックを追加する
         */
        void run(Runnable callback) {
            if (isPending()) {
                mPendings.add(callback);
            } else {
                callback.run();
            }
        }
    }

}
