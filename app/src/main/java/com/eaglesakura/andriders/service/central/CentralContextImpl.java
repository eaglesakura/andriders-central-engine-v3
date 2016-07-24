package com.eaglesakura.andriders.service.central;

import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;

/**
 * メソッドの実装を分離する
 */
class CentralContextImpl {

    /**
     * ログを更新させる
     */
    static RxTask<CentralContext> commitLogDatabase(CentralContext context) {
        return new RxTaskBuilder<CentralContext>(context.getSubscription())
                .observeOn(ObserveTarget.FireAndForget)
                .subscribeOn(SubscribeTarget.GlobalPipeline)
                .async(task -> {
                    AppLog.db("CentralCommit Start");
                    context.mCentralData.commit();
                    return context;
                })
                .completed((result, task) -> {
                    AppLog.db("CentralCommit Completed");
                })
                .failed((error, task) -> {
                    AppLog.report(error);
                })
                .start();
    }
}
