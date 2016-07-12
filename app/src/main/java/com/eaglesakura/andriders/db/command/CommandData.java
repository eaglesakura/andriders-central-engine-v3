package com.eaglesakura.andriders.db.command;

import com.android.annotations.NonNull;
import com.eaglesakura.andriders.dao.command.DbCommand;

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
}
