package com.eaglesakura.andriders.service.central;

import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.BackgroundTaskBuilder;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;

/**
 * メソッドの実装を分離する
 */
class CentralContextImpl {

    /**
     * ログを更新させる
     */
    static BackgroundTask<CentralContext> commitLogDatabase(CentralContext context) {
        return new BackgroundTaskBuilder<CentralContext>(context.getCallbackQueue())
                .callbackOn(CallbackTime.FireAndForget)
                .executeOn(ExecuteTarget.GlobalQueue)
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
