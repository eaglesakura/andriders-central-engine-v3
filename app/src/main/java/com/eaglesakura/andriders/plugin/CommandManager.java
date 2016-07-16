package com.eaglesakura.andriders.plugin;

import com.eaglesakura.andriders.command.SerializableIntent;
import com.eaglesakura.andriders.dao.command.DbCommand;
import com.eaglesakura.andriders.db.command.CommandCollection;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.db.command.CommandDatabase;
import com.eaglesakura.andriders.db.command.CommandSetupData;
import com.eaglesakura.serialize.error.SerializeException;

import android.content.Context;
import android.media.MediaDataSource;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

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
     * カテゴリを指定して列挙する
     */
    public CommandCollection loadFromCategory(int category) {
        try (CommandDatabase db = open()) {
            return new CommandCollection(db.list(category));
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
