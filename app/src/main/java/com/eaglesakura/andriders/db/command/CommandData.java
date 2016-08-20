package com.eaglesakura.andriders.db.command;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.command.SerializableIntent;
import com.eaglesakura.andriders.dao.command.DbCommand;
import com.eaglesakura.andriders.serialize.RawIntent;
import com.eaglesakura.android.util.ImageUtil;
import com.eaglesakura.serialize.Serialize;
import com.eaglesakura.serialize.error.SerializeException;
import com.eaglesakura.util.SerializeUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 * ユーザーが定義したコマンド情報
 *
 * 対応アプリによって構築される
 */
public class CommandData {
    @NonNull
    final DbCommand mRaw;

    @NonNull
    RawExtra mExtra;

    /**
     * 基準となる速度 / double
     */
    public static final String EXTRA_SPEED_KMH = "EXTRA_SPEED_KMH";

    /**
     * 基準速度に対する扱い
     */
    public static final String EXTRA_SPEED_TYPE = "EXTRA_SPEED_TYPE";

    /**
     * 規定まで加速したら
     */
    public static final int SPEED_TYPE_UPPER = 0;

    /**
     * 規定まで減速したら
     */
    public static final int SPEED_TYPE_LOWER = 1;

    /**
     * 最高速度の更新開始
     */
    public static final int SPEED_TYPE_MAX_START = 2;

    /**
     * 最高速度の更新中
     */
    public static final int SPEED_TYPE_MAX_UPDATED = 3;

    /**
     * 最高速度の更新終了
     */
    public static final int SPEED_TYPE_MAX_FINISHED = 4;

    /**
     * 今日の最高速度更新開始
     */
    public static final int SPEED_TYPE_TODAY_MAX_START = 5;

    /**
     * 今日の最高速度更新中
     */
    public static final int SPEED_TYPE_TODAY_MAX_UPDATED = 6;

    /**
     * 今日の最高速度更新完了
     */
    public static final int SPEED_TYPE_TODAY_MAX_FINISHED = 7;

    /**
     * セッションの時間ごとにタイマー起動
     */
    public static final int TIMER_TYPE_SESSION = 0;

    /**
     * 現実時間ごとにタイマー起動
     */
    public static final int TIMER_TYPE_REALTIME = 1;

    /**
     * タイマーコマンドで繰り返し実行を行う
     */
    public static final int TIMER_FLAG_REPEAT = 0x1 << 0;

    /**
     * セッションの走行距離ごと
     */
    public static final int DISTANCE_TYPE_SESSION = 0;

    /**
     * 今日の走行距離ごと
     */
    public static final int DISTANCE_TYPE_TODAY = 1;

    /**
     * 繰り返し実行する
     */
    public static final int DISTANCE_FLAG_REPEAT = 0x1 << 0;

    /**
     * アクティブ走行時間を基準にする
     */
    public static final int DISTANCE_FLAG_ACTIVE_ONLY = 0x1 << 1;

    public CommandData(DbCommand raw) {
        mRaw = raw;

        if (mRaw.getCommandData() == null) {
            mExtra = new RawExtra();
        } else {
            try {
                mExtra = SerializeUtil.deserializePublicFieldObject(RawExtra.class, mRaw.getCommandData());
            } catch (Throwable e) {
                mExtra = new RawExtra();
            }
        }
    }

    public int getCategory() {
        return mRaw.getCategory();
    }

    @NonNull
    public CommandKey getKey() {
        return CommandKey.fromString(mRaw.getCommandKey());
    }

    public String getPackageName() {
        return mRaw.getPackageName();
    }

    @NonNull
    public DbCommand getRaw() {
        try {
            mRaw.setCommandData(SerializeUtil.serializePublicFieldObject(mRaw, false));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mRaw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandData that = (CommandData) o;

        return getKey().equals(that.getKey());

    }

    @Override
    public int hashCode() {
        return mRaw.hashCode();
    }

    /**
     * ACE制御用Intentを保存する
     */
    private void setInternalIntent(SerializableIntent intent) {
        try {
            mRaw.setCommandData(intent.serialize());
        } catch (SerializeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * コマンドアイコンを取得する
     */
    public Bitmap decodeIcon() {
        return ImageUtil.decode(mRaw.getIconPng());
    }

    @NonNull
    public RawExtra getInternalExtra() {
        return mExtra;
    }

    /**
     * s
     * ACEが制御用に利用するIntentを取得する
     */
    @NonNull
    private Intent getInternalIntent() {
        try {
            RawIntent intent = SerializeUtil.deserializePublicFieldObject(RawIntent.class, mRaw.getCommandData());
            return SerializableIntent.newIntent(intent);
        } catch (SerializeException e) {
            return new Intent();
        }
    }

    /**
     * コマンドアプリが構築したIntentを取得する
     */
    @NonNull
    public RawIntent getIntent() {
        try {
            return SerializeUtil.deserializePublicFieldObject(RawIntent.class, mRaw.getIntentData());
        } catch (SerializeException e) {
            throw new RuntimeException(e);
        }
    }

    public static class RawExtra {
        /**
         * 共有 フラグ情報
         */
        @Serialize(id = 1)
        public int flags;

        /**
         * 速度コマンドの基準速度
         */
        @Serialize(id = 11)
        public float speedKmh = 25.0f;

        /**
         * 速度コマンドの種別
         */
        @Serialize(id = 12)
        public int speedType = SPEED_TYPE_UPPER;

        /**
         * タイマーの種類
         */
        @Serialize(id = 21)
        public int timerType = TIMER_TYPE_SESSION;

        /**
         * タイマーの実行間隔（秒単位）
         */
        @Serialize(id = 22)
        public int timerIntervalSec = 30;

        /**
         * 距離基準
         */
        @Serialize(id = 31)
        public int distanceType = DISTANCE_TYPE_SESSION;

        /**
         * 距離
         */
        @Serialize(id = 32)
        public float distanceKm = 5.0f;
    }
}
