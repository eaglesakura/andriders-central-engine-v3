package com.eaglesakura.andriders.model.command;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.command.CommandSetting;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Intent;
import android.support.annotation.Nullable;

public class CommandSetupData {

    CommandKey mKey;

    byte[] mIconFile;

    String mPackageName;

    byte[] mUserIntent;

    private CommandSetupData() {

    }

    public CommandKey getKey() {
        return mKey;
    }

    public byte[] getIconFile() {
        return mIconFile;
    }

    public String getPackageName() {
        return mPackageName;
    }

    /**
     * ACE-SDKを経由してやり取りするデータは容量を軽減するためSerializedする。
     * ただし、互換性の問題からJSONのほう良いため、DBの内部表現にはJSONを用いる
     */
    public byte[] getUserIntent() {
        return mUserIntent;
    }

    /**
     * コマンドセットアップの結果を受け取る
     *
     * 正常にデータを取得できなければnullを返却する
     */
    @Nullable
    public static CommandSetupData getFromResult(Intent data) {
        if (data == null) {
            return null;
        }

        CommandSetupData result = new CommandSetupData();
        result.mKey = data.getParcelableExtra(CommandSetting.EXTRA_COMMAND_KEY);
        result.mIconFile = data.getByteArrayExtra(CommandSetting.EXTRA_ICON);
        result.mPackageName = data.getStringExtra(CommandSetting.EXTRA_PACKAGE_NAME);
        result.mUserIntent = data.getByteArrayExtra(CommandSetting.EXTRA_SERIALIZED_INTENT);

        if (result.mKey == null || CollectionUtil.isEmpty(result.mIconFile) || StringUtil.isEmpty(result.mPackageName)) {
            return null;
        }

        return result;
    }
}
