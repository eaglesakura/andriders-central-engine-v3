package com.eaglesakura.andriders.plugin;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.dao.central.DbCommand;
import com.eaglesakura.andriders.data.db.CentralSettingDatabase;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.command.CommandDataCollection;
import com.eaglesakura.andriders.model.command.CommandSetupData;
import com.eaglesakura.andriders.serialize.RawIntent;
import com.eaglesakura.andriders.system.manager.CentralSettingManager;
import com.eaglesakura.json.JSON;
import com.eaglesakura.serialize.PublicFieldDeserializer;
import com.eaglesakura.serialize.error.SerializeException;
import com.eaglesakura.util.SerializeUtil;

import org.greenrobot.greendao.annotation.NotNull;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * コマンド管理用Manager
 */
public class CommandDataManager extends CentralSettingManager {
    public CommandDataManager(@NotNull Context context) {
        super(context);
    }

    /**
     * カテゴリを指定して列挙する
     *
     * @see CommandData#CATEGORY_TIMER
     * @see CommandData#CATEGORY_DISTANCE
     * @see CommandData#CATEGORY_PROXIMITY
     * @see CommandData#CATEGORY_SPEED
     */
    public CommandDataCollection loadFromCategory(int category) {
        try (CentralSettingDatabase db = open()) {
            return new CommandDataCollection(db.listCommands(category));
        }
    }

    /**
     * すべての設定済みコマンドを列挙する
     */
    public CommandDataCollection loadAll() {
        try (CentralSettingDatabase db = open()) {
            return new CommandDataCollection(db.listCommands());
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
        try (CentralSettingDatabase db = open()) {
            db.remove(key);
        }
    }

    public void save(CommandData data) {
        try (CentralSettingDatabase db = open()) {
            db.update(data.getRaw());
        }
    }

    /**
     * コマンドを保存する
     */
    public CommandData save(CommandSetupData data, int category, @Nullable CommandData.Extra extra) {
        try (CentralSettingDatabase db = open()) {
            DbCommand dbCommand = new DbCommand();
            dbCommand.setCommandKey(data.getKey().toString());
            dbCommand.setCategory(category);
            dbCommand.setIconPng(data.getIconFile());
            dbCommand.setPackageName(data.getPackageName());

            if (data.getUserIntent() != null) {
                RawIntent rawIntent = PublicFieldDeserializer.deserializeFrom(RawIntent.class, data.getUserIntent());
                dbCommand.setIntentJson(JSON.encodeOrNull(rawIntent));
            }

            if (extra != null) {
                dbCommand.setExtraJson(JSON.encodeOrNull(extra));
            }

            db.update(dbCommand);
            return new CommandData(dbCommand);
        } catch (SerializeException e) {
            throw new RuntimeException(e);
        }
    }
}
