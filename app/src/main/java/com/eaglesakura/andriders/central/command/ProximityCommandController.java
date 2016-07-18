package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.db.command.CommandDataCollection;
import com.eaglesakura.andriders.db.command.CommandDatabase;
import com.eaglesakura.andriders.plugin.CommandDataManager;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

public class ProximityCommandController extends CommandController {

    /**
     * ロードされたコマンド一覧
     */
    @NonNull
    CommandDataCollection mCommands;

    @NonNull
    final ClockTimer mTimer;

    /**
     * 最後にバイブレーションを鳴らしたカウント秒
     *
     * 開始, 1, 2, 3, 4秒でそれぞれバイブを振動させる
     */
    int mLastFeedbackSec = 0;

    static final int MAX_FEEDBACK_SEC = 4;

    ProximityListener mProximityListener;

    public ProximityCommandController(@NonNull Context context, @NonNull Clock clock, @NonNull SubscriptionController subscriptionController) {
        super(context, subscriptionController);
        mTimer = new ClockTimer(clock);
    }

    public void setProximityListener(ProximityListener proximityListener) {
        mProximityListener = proximityListener;
    }

    @NonNull
    CommandData getCurrentCommand() {
        CommandDataCollection collection = mCommands;
        if (collection == null) {
            return null;
        }

        return collection.getOrNull(CommandKey.fromProximity((int) mTimer.endSec()));
    }

    /**
     * 手をかざし始めた
     */
    @WorkerThread
    public void onStartCount() {
        mLastFeedbackSec = -1;
        mTimer.start();
        new RxTaskBuilder<CommandDataCollection>(mSubscriptionController)
                .subscribeOn(SubscribeTarget.Parallels)
                .observeOn(ObserveTarget.Alive)
                .async(task -> {
                    return new CommandDataManager(mContext).loadFromCategory(CommandDatabase.CATEGORY_PROXIMITY);
                })
                .completed((result, task) -> {
                    mCommands = result;
                })
                .failed((error, task) -> {
                    AppLog.report(error);
                }).start();
        onUpdate();
    }

    @WorkerThread
    @Override
    public void onUpdate() {
        final int end = (int) (mTimer.endSec());
        if (end == mLastFeedbackSec || end > MAX_FEEDBACK_SEC) {
            return;
        }
        mLastFeedbackSec = end;

        ProximityListener listener = mProximityListener;
        if (listener == null) {
            return;
        }

        mSubscriptionController.run(ObserveTarget.Alive, () -> {
            CommandData data = getCurrentCommand();
            listener.onRequestUserFeedback(this, end, data);
        });
    }


    /**
     * カウントを停止する
     */
    @WorkerThread
    public void onEndCount() {
        requestCommandBoot(getCurrentCommand());
    }

    public interface ProximityListener {
        /**
         * 振動等のユーザーフィードバックを行わせる
         */
        @UiThread
        void onRequestUserFeedback(ProximityCommandController self, int sec, @Nullable CommandData data);
    }
}
