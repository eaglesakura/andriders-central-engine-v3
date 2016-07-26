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
    public static final int SPEEDCOMMAND_TYPE_UPPER = 0;

    /**
     * 規定まで減速したら
     */
    public static final int SPEEDCOMMAND_TYPE_LOWER = 1;

    /**
     * 最高速度の更新開始
     */
    public static final int SPEEDCOMMAND_TYPE_MAX_START = 2;

    /**
     * 最高速度の更新中
     */
    public static final int SPEEDCOMMAND_TYPE_MAX_UPDATED = 3;

    /**
     * 最高速度の更新終了
     */
    public static final int SPEEDCOMMAND_TYPE_MAX_FINISHED = 4;

    /**
     * 今日の最高速度更新開始
     */
    public static final int SPEEDCOMMAND_TYPE_TODAY_MAX_START = 5;

    /**
     * 今日の最高速度更新中
     */
    public static final int SPEEDCOMMAND_TYPE_TODAY_MAX_UPDATED = 6;

    /**
     * 今日の最高速度更新完了
     */
    public static final int SPEEDCOMMAND_TYPE_TODAY_MAX_FINISHED = 7;

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
         * 速度コマンドの基準速度
         */
        @Serialize(id = 11)
        public float speedKmh = 25.0f;

        /**
         * 速度コマンドの種別
         */
        @Serialize(id = 12)
        public int speedType = SPEEDCOMMAND_TYPE_UPPER;
    }
}
