package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.command.SerializableIntent;
import com.eaglesakura.andriders.dao.command.DbCommand;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.thread.IntHolder;
import com.eaglesakura.util.Util;

import org.junit.Test;

import static org.junit.Assert.*;

public class SpeedCommandControllerTest extends DeviceTestCase {

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

    @Test
    public void 一定速度を上回ったらコマンドを実行する() throws Throwable {
        IntHolder bootHolder = new IntHolder(0);

        BasicSpeedCommandController controller =
                new BasicSpeedCommandController(getApplication(),
                        SubscriptionController.newUnitTestController(),
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
                        SubscriptionController.newUnitTestController(),
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