package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.dao.command.DbCommand;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.thread.IntHolder;

import org.junit.Test;

public class TimerCommandControllerTest extends AppUnitTestCase {

    CommandData newCommand(long intervalTimeMs, int type, int flags) throws Throwable {
        CommandData result = new CommandData(new DbCommand());
        CommandData.RawExtra extra = result.getInternalExtra();
        extra.timerType = type;
        extra.timerIntervalSec = (int) (intervalTimeMs / 1000);
        extra.flags = flags;
        return result;
    }


    @Test
    public void セッションを基準にタイマーが起動することを確認する() throws Throwable {

        Clock clock = new Clock(10000);
        TimerCommandController commandController =
                new TimerCommandController(getContext(), newCommand(1000, CommandData.TIMER_TYPE_SESSION, 0x00), clock);

        IntHolder holder = new IntHolder();
        commandController.setBootListener((self, data) -> {
            ++holder.value;
        });

        // 1秒未満進む
        // まだコールされないはず
        for (int i = 0; i < 99; ++i) {
            clock.offset(1000 / 100);
            commandController.onUpdate();
        }

        assertEquals(holder.value, 0);

        // フレーム経過で、コールされる
        for (int i = 0; i < 10; ++i) {
            clock.offset(1000 / 100);
            commandController.onUpdate();
        }

        // 一度だけコールされる
        assertEquals(holder.value, 1);

        // もう絶対にコールされない
        for (int i = 0; i < (100 * 60 * 24); ++i) {
            clock.offset(1000 / 100);
            commandController.onUpdate();
            assertEquals(holder.value, 1);
        }
    }

    @Test
    public void セッション時間に添って繰り返し実行されることを検証する() throws Throwable {

        Clock clock = new Clock(10000);
        TimerCommandController commandController =
                new TimerCommandController(getContext(), newCommand(1000, CommandData.TIMER_TYPE_SESSION, CommandData.TIMER_FLAG_REPEAT), clock);

        IntHolder holder = new IntHolder();
        commandController.setBootListener((self, data) -> {
            ++holder.value;
        });

        for (int i = 0; i < 10; ++i) {
            for (int k = 0; k < 100; ++k) {
                clock.offset(1000 / 100);
                commandController.onUpdate();
            }
            // 繰り返しの回数分コールされている
            assertEquals(holder.value, i + 1);
        }
    }

    @Test
    public void Clockの絶対時間を基準にタイマーが起動することを確認する() throws Throwable {

        Clock clock = new Clock(10500);
        TimerCommandController commandController =
                new TimerCommandController(getContext(), newCommand(1000, CommandData.TIMER_TYPE_REALTIME, 0x00), clock);

        IntHolder holder = new IntHolder();
        commandController.setBootListener((self, data) -> {
            ++holder.value;
        });

        // 1秒未満進む
        // 絶対時間が基準のため、コールされている
        for (int i = 0; i < 50; ++i) {
            clock.offset(1000 / 100);
            commandController.onUpdate();
        }

        assertEquals(holder.value, 1);

        // もう絶対にコールされない
        for (int i = 0; i < (100 * 60 * 24); ++i) {
            clock.offset(1000 / 100);
            commandController.onUpdate();
            assertEquals(holder.value, 1);
        }
    }
}