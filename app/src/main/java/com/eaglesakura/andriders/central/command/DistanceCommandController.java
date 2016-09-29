package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.central.CentralDataHandler;
import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSessionData;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * 距離コマンドの制御を行う
 */
public class DistanceCommandController extends CommandController {
    final CommandData mCommandData;

    /**
     * 次に実行すべき距離
     */
    float mNextTriggerDistance;

    /**
     * 現在の走行距離
     */
    float mCurrentDistance;

    /**
     * 距離情報を初期化済み
     */
    boolean mInitialized;

    public DistanceCommandController(@NonNull Context context, @NonNull CommandData commandData) {
        super(context);
        mCommandData = commandData;

        updateNextTriggerDistance();
    }

    public void bind(CentralDataReceiver receiver) {
        receiver.addHandler(mDataHandler);
    }

    /**
     * 次に反応すべき時刻を更新する
     */
    protected void updateNextTriggerDistance() {
        CommandData.Extra extra = mCommandData.getInternalExtra();

        if (mNextTriggerDistance == 0) {
            // 初回リセット
            mNextTriggerDistance = extra.distanceKm;
        } else {
            if ((extra.flags & CommandData.DISTANCE_FLAG_REPEAT) != 0) {
                // リピートの場合、二度目以降はインターバルを加算するだけでいい
                mNextTriggerDistance += extra.distanceKm;
            } else {
                // リピートしない場合、適当な大きい数を加算することで対応する
                mNextTriggerDistance += 40000;  // 地球一周レベルの距離ならば実質的に反応できない
            }
        }
    }

    /**
     * トリガーを実行する
     */
    protected void onTriggerTime() {
        // コマンドの実行リクエストを送る
        requestCommandBoot(mCommandData);
    }

    @Override
    public void onUpdate() {
        if (!mInitialized) {
            return;
        }

        if (mCurrentDistance >= mNextTriggerDistance) {
            // トリガーを実行
            onTriggerTime();

            // トリガー時刻を更新する
            updateNextTriggerDistance();
        }
    }

    CentralDataHandler mDataHandler = new CentralDataHandler() {
        @Override
        public void onReceived(RawCentralData newData) {
            CommandData.Extra internalExtra = mCommandData.getInternalExtra();

            RawSessionData target;
            if (internalExtra.distanceType == CommandData.DISTANCE_TYPE_SESSION) {
                target = newData.session;
            } else {
                target = newData.today;
            }

            if ((internalExtra.flags & CommandData.DISTANCE_FLAG_ACTIVE_ONLY) != 0) {
                // アクティブ距離をチェック
                mCurrentDistance = target.activeDistanceKm;
            } else {
                // 合計距離をチェック
                mCurrentDistance = target.distanceKm;
            }

            if (!mInitialized) {
                // 例えば、既に100km走っている場合、すぐにトリガーが実行されてしまう。
                // それを避けるため、初回受信時には反応距離を加算して次回実行距離を正しく扱う
                mNextTriggerDistance += mCurrentDistance;
                mInitialized = true;
            }
        }
    };
}
