package com.eaglesakura.andriders.plugin;

import com.eaglesakura.andriders.command.SerializableIntent;
import com.eaglesakura.andriders.dao.command.DbCommand;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.db.command.CommandDatabase;
import com.eaglesakura.andriders.db.command.CommandSetupData;
import com.eaglesakura.serialize.error.SerializeException;

import android.content.Context;
import android.media.MediaDataSource;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * コマンド管理用Manager
 */
public class CommandManager {
    @NonNull
    final Context mContext;

    CommandDatabase mDatabase;

    public CommandManager(@NonNull Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CommandDatabase(mContext);
    }

    CommandDatabase open() {
        return mDatabase.openWritable(CommandDatabase.class);
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
            if (internalIntent != null) {
                dbCommand.setIntentData(internalIntent.serialize());
            }

            db.update(dbCommand);
            return new CommandData(dbCommand);
        } catch (SerializeException e) {
            throw new RuntimeException(e);
        }
    }
}
