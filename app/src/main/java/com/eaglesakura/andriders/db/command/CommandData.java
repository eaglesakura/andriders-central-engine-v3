package com.eaglesakura.andriders.db.command;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.command.SerializableIntent;
import com.eaglesakura.andriders.dao.command.DbCommand;
import com.eaglesakura.andriders.serialize.RawIntent;
import com.eaglesakura.android.util.ImageUtil;
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

    public CommandData(DbCommand raw) {
        mRaw = raw;
    }

    public int getCategory() {
        return mRaw.getCategory();
    }

    @NonNull
    public CommandKey getCommandKey() {
        return CommandKey.fromString(mRaw.getCommandKey());
    }

    public String getPackageName() {
        return mRaw.getPackageName();
    }

    /**
     * ACE制御用Intentを保存する
     */
    public void setInternalIntent(SerializableIntent intent) {
        try {
            mRaw.setIntentData(intent.serialize());
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

    /**
     * ACEが制御用に利用するIntentを取得する
     */
    @NonNull
    public Intent getInternalIntent() {
        try {
            RawIntent intent = SerializeUtil.deserializePublicFieldObject(RawIntent.class, mRaw.getCommandData());
            return SerializableIntent.newIntent(intent);
        } catch (SerializeException e) {
            throw new RuntimeException(e);
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

}
