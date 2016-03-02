package com.eaglesakura.android.rx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * RxAndroidの実行スレッド制御を行う
 */
class ThreadController {

    List<ThreadItem> mThreads = new ArrayList<>();


    /**
     * プロセス共有シリアル
     */
    private final ThreadItem gGlobalPipeline = new ThreadItem(SubscribeTarget.GlobalPipeline);

    /**
     * プロセス共有Parallels
     */
    private final ThreadItem gGlobalParallels = new ThreadItem(SubscribeTarget.GlobalParallels);

    /**
     * プロセス共有ネットワーク
     */
    private final ThreadItem gNetworks = new ThreadItem(SubscribeTarget.Network);

    public ThreadController() {
        mThreads.add(new ThreadItem(SubscribeTarget.Pipeline));
        mThreads.add(new ThreadItem(SubscribeTarget.Parallels));
        mThreads.add(gGlobalPipeline);
        mThreads.add(gGlobalParallels);
        mThreads.add(gNetworks);
    }

    /**
     * 処理対象のスケジューラを取得する
     *
     * MEMO : スケジューラの実際のnew処理はこの呼出まで遅延される
     */
    public Scheduler getScheduler(SubscribeTarget target) {
        return mThreads.get(target.ordinal()).getScheduler();
    }

    /**
     * 全てのスケジューラを開放する
     */
    public void dispose() {
        mThreads.get(SubscribeTarget.Pipeline.ordinal()).dispose();
        mThreads.get(SubscribeTarget.Parallels.ordinal()).dispose();
    }

    class ThreadItem {
        ThreadPoolExecutor mExecutor;
        Scheduler mScheduler;
        SubscribeTarget mTarget;

        public ThreadItem(SubscribeTarget target) {
            this.mTarget = target;
        }

        public Scheduler getScheduler() {
            synchronized (this) {
                if (mScheduler == null) {
                    mExecutor = new ThreadPoolExecutor(1, mTarget.getThreadPoolNum(), mTarget.getKeepAliveMs(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
                    mScheduler = Schedulers.from(mExecutor);
                }
                return mScheduler;
            }
        }

        public void dispose() {
            synchronized (this) {
                if (mExecutor != null) {
                    mExecutor.shutdown();
                }
            }
        }
    }
}
