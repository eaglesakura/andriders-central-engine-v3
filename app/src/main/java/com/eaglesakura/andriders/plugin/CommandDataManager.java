package com.eaglesakura.andriders.plugin;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.command.SerializableIntent;
import com.eaglesakura.andriders.dao.command.DbCommand;
import com.eaglesakura.andriders.db.command.CommandDataCollection;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.db.command.CommandDatabase;
import com.eaglesakura.andriders.db.command.CommandSetupData;
import com.eaglesakura.serialize.error.SerializeException;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * コマンド管理用Manager
 */
public class CommandDataManager {
    @NonNull
    final Context mContext;

    CommandDatabase mDatabase;

    public CommandDataManager(@NonNull Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CommandDatabase(mContext);
    }

    CommandDatabase open() {
        return mDatabase.openWritable(CommandDatabase.class);
    }

    /**
     * カテゴリを指定して列挙する
     */
    public CommandDataCollection loadFromCategory(int category) {
        try (CommandDatabase db = open()) {
            return new CommandDataCollection(db.list(category));
        }
    }

    public void remove(@NonNull CommandData data) {
        if (data == null) {
            return;
        }
        remove(data.getKey());
    }

    public void remove(@NonNull CommandKey key) {
        if (key == null) {
            return;
        }
        try (CommandDatabase db = open()) {
            db.remove(key);
        }
    }

    /**
     * コマンドを保存する
     */
    public CommandData save(CommandSetupData data, int category, @Nullable SerializableIntent internalIntent) {
        try (CommandDatabase db = open()) {
            DbCommand dbCommand = new DbCommand();
            dbCommand.setCommandKey(data.getKey().getKey());
            dbCommand.setCategory(category);
            dbCommand.setCommandData(data.getUserIntent());
            dbCommand.setIconPng(data.getIconFile());
            dbCommand.setPackageName(data.getPackageName());
            dbCommand.setIntentData(data.getUserIntent());
            if (internalIntent != null) {
                dbCommand.setCommandData(internalIntent.serialize());
            }

            db.update(dbCommand);
            return new CommandData(dbCommand);
        } catch (SerializeException e) {
            throw new RuntimeException(e);
        }
    }
}
