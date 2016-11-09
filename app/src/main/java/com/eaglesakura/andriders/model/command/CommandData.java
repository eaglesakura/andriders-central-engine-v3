package com.eaglesakura.andriders.model.command;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.command.SerializableIntent;
import com.eaglesakura.andriders.dao.central.DbCommand;
import com.eaglesakura.andriders.model.DaoModel;
import com.eaglesakura.andriders.serialize.RawIntent;
import com.eaglesakura.android.util.ImageUtil;
import com.eaglesakura.json.JSON;
import com.eaglesakura.util.StringUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.Comparator;

/**
 * ユーザーが定義したコマンド情報
 *
 * 対応アプリによって構築される
 */
public class CommandData extends DaoModel<DbCommand> {
    @NonNull
    Extra mExtra;

    /**
     * 近接コマンド
     */
    public static final int CATEGORY_PROXIMITY = 1;

    /**
     * 速度コマンド
     */
    public static final int CATEGORY_SPEED = 2;

    /**
     * 距離コマンド
     */
    public static final int CATEGORY_DISTANCE = 3;

    /**
     * タイマーコマンド
     */
    public static final int CATEGORY_TIMER = 4;

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
        super(raw);
        mExtra = JSON.decodeOrNull(mRaw.getExtraJson(), Extra.class);

        if (mExtra == null) {
            mExtra = new Extra();
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

    @Override
    @NonNull
    public DbCommand getRaw() {
        mRaw.setExtraJson(JSON.encodeOrNull(mExtra));
        return mRaw;
    }

    /**
     * ACE制御用Intentを保存する
     */
    private void setInternalIntent(SerializableIntent intent) {
        mRaw.setIntentJson(JSON.encodeOrNull(intent.getRawIntent()));
    }

    /**
     * コマンドアイコンを取得する
     */
    public Bitmap decodeIcon() {
        return ImageUtil.decode(mRaw.getIconPng());
    }

    @NonNull
    public Extra getInternalExtra() {
        return mExtra;
    }

    /**
     * ACEが制御用に利用するIntentを取得する
     */
    @NonNull
    private Intent getInternalIntent() {
        RawIntent intent = JSON.decodeOrNull(mRaw.getIntentJson(), RawIntent.class);
        return SerializableIntent.newIntent(intent);
    }

    /**
     * コマンドアプリが構築したIntentを取得する
     */
    @NonNull
    public RawIntent getIntent() {
        return JSON.decodeOrNull(mRaw.getIntentJson(), RawIntent.class);
    }

    /**
     * 付与情報
     *
     * JSONとして保存する
     */
    public static class Extra {
        /**
         * 共有 フラグ情報
         */
        public int flags;

        /**
         * 速度コマンドの基準速度
         */
        public Float speedKmh;

        /**
         * 速度コマンドの種別
         */
        public Integer speedType;

        /**
         * タイマーの種類
         */
        public Integer timerType;

        /**
         * タイマーの実行間隔（秒単位）
         */
        public Integer timerIntervalSec;

        /**
         * 距離基準
         */
        public Integer distanceType;

        /**
         * 距離
         */
        public Float distanceKm;
    }

    static final Comparator<CommandData> COMPARATOR_ASC = (l, r) -> {
        if (l.getCategory() != r.getCategory()) {
            return Integer.compare(l.getCategory(), r.getCategory());
        } else {
            return StringUtil.compareString(r.getKey().toString(), l.getKey().toString());
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandData that = (CommandData) o;

        return that.getKey().equals(that.getKey());

    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
}
