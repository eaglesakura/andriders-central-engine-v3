package com.eaglesakura.andriders.plugin;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.central.data.command.speed.SpeedCommandControllerTest;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.android.garnet.Garnet;

import org.junit.Test;

public class CommandDataManagerTest extends AppUnitTestCase {

    @Test
    public void コマンドの保存が行える() throws Throwable {

        // 適当なデータを保存する
        {
            CommandDataManager manager = Garnet.instance(AppManagerProvider.class, CommandDataManager.class);
            // 初期ではロードしても0件である
            validate(manager.loadFromCategory(CommandData.CATEGORY_SPEED).list()).sizeIs(0);

            CommandData cmd = SpeedCommandControllerTest.newCommand(123, CommandData.SPEED_TYPE_LOWER);
            cmd.getRaw().setCommandKey(CommandKey.fromSpeed(System.currentTimeMillis()).toString());
            cmd.getRaw().setPackageName(getContext().getPackageName());
            cmd.getRaw().setCategory(CommandData.CATEGORY_SPEED);
            cmd.getRaw().setIconPng("temp".getBytes());
            manager.save(cmd);
        }

        // データが読める
        {
            CommandDataManager manager = Garnet.instance(AppManagerProvider.class, CommandDataManager.class);
            // スピードコマンド1件、その他は0件が正しい
            validate(manager.loadFromCategory(CommandData.CATEGORY_SPEED).list()).sizeIs(1).checkAt(0, cmd -> {
                assertEquals(cmd.getCategory(), CommandData.CATEGORY_SPEED);
                validate((float) cmd.getInternalExtra().speedKmh).eq(123);
            });
            validate(manager.loadFromCategory(CommandData.CATEGORY_PROXIMITY).list()).sizeIs(0);
        }
    }
}