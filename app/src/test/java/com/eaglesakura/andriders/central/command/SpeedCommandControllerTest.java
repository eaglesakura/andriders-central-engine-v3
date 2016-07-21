package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.command.SerializableIntent;
import com.eaglesakura.andriders.dao.command.DbCommand;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.serialize.RawRecord;
import com.eaglesakura.thread.IntHolder;
import com.eaglesakura.util.Util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpeedCommandControllerTest extends AppUnitTestCase {

    CommandData newCommand(double speed, int type) throws Throwable {
        DbCommand db = new DbCommand();
        db.setCommandData(new SerializableIntent()
                .putExtra(CommandData.EXTRA_SPEED_KMH, speed)
                .putExtra(CommandData.EXTRA_SPEED_TYPE, type)
                .serialize()
        );
        db.setCommandKey(CommandKey.fromSpeed(System.currentTimeMillis()).getKey());
        return new CommandData(db);
    }

    RawRecord newRecord(double maxSpeed, double todayMaxSpeed) throws Throwable {
        RawRecord record = new RawRecord();
        record.maxSpeedKmh = (float) maxSpeed;
        record.maxSpeedKmhToday = (float) todayMaxSpeed;
        return record;
    }

    @Test
    public void 正しい基準速を得ることができる() throws Throwable {

        {
            MaxSpeedCommandController controller = new MaxSpeedCommandController(getApplication(),
                    newCommand(25.0f, CommandData.SPEEDCOMMAND_TYPE_MAX_FINISHED));
            controller.mRecord = newRecord(40.0f, 30.0f);
            assertEquals(controller.getTargetMaxSpeed(), 40.0f, 1.0f);
        }
        {
            MaxSpeedCommandController controller = new MaxSpeedCommandController(getApplication(),
                    newCommand(25.0f, CommandData.SPEEDCOMMAND_TYPE_TODAY_MAX_FINISHED));
            controller.mRecord = newRecord(40.0f, 30.0f);
            assertEquals(controller.getTargetMaxSpeed(), 30.0f, 1.0f);
        }
    }

    final double MAXSPEED = 40.0;
    final double MAXSPEED_TODAY = 30.0;

    @Test
    public void 最高速度を上回ったらコマンドを実行する() throws Throwable {
        IntHolder bootHolder = new IntHolder(0);

        MaxSpeedCommandController controller = new MaxSpeedCommandController(getApplication(),
                newCommand(25.0f, CommandData.SPEEDCOMMAND_TYPE_MAX_START));
        controller.mRecord = newRecord(MAXSPEED, MAXSPEED_TODAY);
        controller.setBootListener(((self, data) -> {
            bootHolder.value++;
        }));

        // 速度をカウントアップ
        float speed = 1.0f;
        while ((speed += 1.0f) < 50.0f) {
            // 速度を更新
            controller.onUpdateSpeed(speed);
            if (speed > MAXSPEED) {
                // ステートはチャレンジ
                assertEquals(controller.mState, MaxSpeedCommandController.STATE_CHALLENGE);
            } else {
                // ステートはLower
                assertEquals(controller.mState, MaxSpeedCommandController.STATE_LOWERSPEED);
            }
        }
        Util.sleep(100);
        assertEquals(bootHolder.value, 1);  // コールバックは一度だけ行われた

        // 速度をカウントダウン
        while ((speed -= 1.0f) > 0.0f) {
            controller.onUpdateSpeed(speed);
            if (speed > MAXSPEED) {
                // ステートはチャレンジ
                assertEquals(controller.mState, MaxSpeedCommandController.STATE_CHALLENGE);
            } else {
                // ステートはLower
                assertEquals(controller.mState, MaxSpeedCommandController.STATE_LOWERSPEED);
            }
        }
        Util.sleep(100);
        assertEquals(bootHolder.value, 1);  // 追加のコールバックは行われなかった
    }

    @Test
    public void 最高速度更新中にコマンドを実行する() throws Throwable {
        IntHolder bootHolder = new IntHolder(0);

        MaxSpeedCommandController controller = new MaxSpeedCommandController(getApplication(),
                newCommand(25.0f, CommandData.SPEEDCOMMAND_TYPE_MAX_UPDATED));
        controller.mRecord = newRecord(MAXSPEED, MAXSPEED_TODAY);
        controller.setBootListener(((self, data) -> {
            bootHolder.value++;
        }));

        // 速度をカウントアップ
        float speed = 1.0f;
        while ((speed += 1.0f) < 50.0f) {
            // 速度を更新
            controller.onUpdateSpeed(speed);
        }
        Util.sleep(100);
        assertEquals(bootHolder.value, 8);  // 更新の回数だけ呼び出される

        // 速度をカウントダウン
        bootHolder.value = 0;
        while ((speed -= 1.0f) > 40.0f) {
            controller.onUpdateSpeed(speed);
        }
        Util.sleep(100);
        assertEquals(bootHolder.value, 0);  // 追加の呼び出しはない
        assertEquals(controller.mState, MaxSpeedCommandController.STATE_CHALLENGE);

        // もう一度頑張る
        bootHolder.value = 0;
        while ((speed += 1.0f) < 50.0f) {
            controller.onUpdateSpeed(speed);
        }
        Util.sleep(100);
        assertEquals(bootHolder.value, 0);  // 前の速度まで頑張ってもアップデートされない


        // 限界まで頑張る
        bootHolder.value = 0;
        while ((speed += 1.0f) < 60.0f) {
            controller.onUpdateSpeed(speed);
        }
        Util.sleep(100);
        assertEquals(bootHolder.value, 9);  // 追加で呼びだされた


        // 最終減速でステートが戻る
        bootHolder.value = 0;
        while ((speed -= 1.0f) > 0.0f) {
            controller.onUpdateSpeed(speed);
        }
        Util.sleep(100);
        assertEquals(bootHolder.value, 0);  // 追加の呼び出しはない
        assertEquals(controller.mState, MaxSpeedCommandController.STATE_LOWERSPEED);
    }

    @Test
    public void 最高速度更新を終えたらコマンドを実行する() throws Throwable {
        IntHolder bootHolder = new IntHolder(0);

        MaxSpeedCommandController controller = new MaxSpeedCommandController(getApplication(),
                newCommand(25.0f, CommandData.SPEEDCOMMAND_TYPE_MAX_FINISHED));
        controller.mRecord = newRecord(MAXSPEED, MAXSPEED_TODAY);
        controller.setBootListener(((self, data) -> {
            bootHolder.value++;
        }));

        // 速度をカウントアップ
        float speed = 1.0f;
        while ((speed += 1.0f) < 50.0f) {
            // 速度を更新
            controller.onUpdateSpeed(speed);
        }
        Util.sleep(100);
        assertEquals(bootHolder.value, 0);  // まだ更新中であると思われる

        // 速度をカウントダウン
        while ((speed -= 1.0f) > 0.0f) {
            controller.onUpdateSpeed(speed);
        }
        Util.sleep(100);
        assertEquals(bootHolder.value, 1);  // 完了したので、一度だけコールバックされた
    }

    @Test
    public void 一定速度を上回ったらコマンドを実行する() throws Throwable {
        IntHolder bootHolder = new IntHolder(0);

        BasicSpeedCommandController controller =
                new BasicSpeedCommandController(getApplication(),
                        newCommand(25.0f, CommandData.SPEEDCOMMAND_TYPE_UPPER));

        // callback
        controller.setBootListener((self, data) -> {
            assertNotNull(data);
            bootHolder.value++;
        });

        float speed = 1.0f;
        while (speed < 30.0f) {
            controller.onUpdateSpeed(speed);        // 速度を更新する
            speed += 0.1f;
        }
        Util.sleep(100);
        assertEquals(bootHolder.value, 1);          // 一度だけ呼ばれている

        controller.onUpdateSpeed(0);
        Util.sleep(100);
        assertEquals(bootHolder.value, 1);
        controller.onUpdateSpeed(25.1f);    // 再度呼び出される速度
        Util.sleep(100);
        assertEquals(bootHolder.value, 2);  // 二度目の呼び出しが行われた
    }


    @Test
    public void 一定速度を下回ったらコマンドを実行する() throws Throwable {
        IntHolder bootHolder = new IntHolder(0);

        BasicSpeedCommandController controller =
                new BasicSpeedCommandController(getApplication(),
                        newCommand(25.0f, CommandData.SPEEDCOMMAND_TYPE_LOWER));

        // callback
        controller.setBootListener((self, data) -> {
            assertNotNull(data);
            bootHolder.value++;
        });

        float speed = 30.0f;
        while (speed > 0.0f) {
            controller.onUpdateSpeed(speed);        // 速度を更新する
            speed -= 0.1f;
        }
        Util.sleep(100);
        assertEquals(bootHolder.value, 1);          // 一度だけ呼ばれている

        controller.onUpdateSpeed(30.0f);
        Util.sleep(100);
        assertEquals(bootHolder.value, 1);
        controller.onUpdateSpeed(24.0f);    // 再度呼び出される速度
        Util.sleep(100);
        assertEquals(bootHolder.value, 2);  // 二度目の呼び出しが行われた
    }
}
