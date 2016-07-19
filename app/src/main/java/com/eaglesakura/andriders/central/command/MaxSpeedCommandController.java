package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.android.rx.SubscriptionController;

import android.content.Context;
import android.support.annotation.NonNull;

public class MaxSpeedCommandController extends SpeedCommandController {

    /**
     * 最高速度を下回っている
     */
    static final int STATE_LOWERSPEED = 1;

    /**
     * 最高速度に挑戦中である
     */
    static final int STATE_CHALLENGE = 2;


    /**
     * 現在の観察ステート
     */
    int mState = STATE_LOWERSPEED;

    /**
     * チャレンジ開始時点での最高速度
     */
    double mCacheSpeedKmh;

    /**
     * チャレンジ中の最高速度
     */
    double mChallengeMaxSpeed;

    public MaxSpeedCommandController(@NonNull Context context, CommandData commandData) {
        super(context, commandData);
    }

    /**
     * 対象となる最高速度を取得する
     */
    double getTargetMaxSpeed() {
        if (mRecord == null) {
            return 9999.9;
        }

        // チャレンジ前はそのままの速度を返す
        switch (mCommandType) {
            case CommandData.SPEEDCOMMAND_TYPE_MAX_START:
            case CommandData.SPEEDCOMMAND_TYPE_MAX_UPDATED:
            case CommandData.SPEEDCOMMAND_TYPE_MAX_FINISHED:
                return mRecord.maxSpeedKmh;
            default:
                return mRecord.maxSpeedKmhToday;
        }
    }

    /**
     * 基準となる速度を取得する
     */
    double getThreshouldMaxSpeedKmh() {
        return Math.max(getTargetMaxSpeed(), 25.0f);
    }

    void onMaxSpeedStarted() {
        switch (mCommandType) {
            case CommandData.SPEEDCOMMAND_TYPE_MAX_START:
            case CommandData.SPEEDCOMMAND_TYPE_TODAY_MAX_START:
                requestCommandBoot(mCommandData);
                break;
        }
    }

    void onMaxSpeedUpdated() {
        switch (mCommandType) {
            case CommandData.SPEEDCOMMAND_TYPE_MAX_UPDATED:
            case CommandData.SPEEDCOMMAND_TYPE_TODAY_MAX_UPDATED:
                requestCommandBoot(mCommandData);
                break;
        }
    }

    void onMaxSpeedFinished() {
        switch (mCommandType) {
            case CommandData.SPEEDCOMMAND_TYPE_MAX_FINISHED:
            case CommandData.SPEEDCOMMAND_TYPE_TODAY_MAX_FINISHED:
                requestCommandBoot(mCommandData);
                break;
        }
    }

    @Override
    void onUpdateSpeed(float currentSpeedKmh) {
        super.onUpdateSpeed(currentSpeedKmh);

        if (mState == STATE_LOWERSPEED) {
            // 最高速待機中
            if (currentSpeedKmh > getThreshouldMaxSpeedKmh()) {
                // 基準速度を超えた
                mCacheSpeedKmh = currentSpeedKmh;
                mChallengeMaxSpeed = currentSpeedKmh;
                mState = STATE_CHALLENGE;
                // 最高速度を更新中に変更
                onMaxSpeedStarted();
            }
        } else {
            // 最高速チャレンジ中
            if (currentSpeedKmh > mChallengeMaxSpeed) {
                // 最高速度が限界を超えた
                mChallengeMaxSpeed = currentSpeedKmh;
                onMaxSpeedUpdated();
            } else if (currentSpeedKmh < mCacheSpeedKmh) {
                // 古い最高速度を下回ったら、更新終了
                onMaxSpeedFinished();
                mState = STATE_LOWERSPEED;
            }
        }
    }

    @Override
    public void onUpdate() {

    }
}
