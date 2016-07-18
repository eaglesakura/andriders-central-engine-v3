package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.AceApplication;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.thread.IntHolder;
import com.eaglesakura.util.Util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ProximityCommandControllerTest extends DeviceTestCase<AceApplication> {

    @Test
    public void 毎秒フィードバックが送られてくる() throws Throwable {
        IntHolder callbackSec = new IntHolder(-1);
        IntHolder callbackNum = new IntHolder(0);
        ProximityCommandController.ProximityListener proximityListener = (self, sec, data) -> {
            // 1秒ごとにフィードバックが送られる
            assertEquals(callbackSec.value + 1, sec);
            callbackSec.value = sec;
            callbackNum.value++;
        };

        Clock clock = new Clock(System.currentTimeMillis());
        SubscriptionController subscriptionController = SubscriptionController.newUnitTestController();
        ProximityCommandController controller = new ProximityCommandController(getApplication(), clock, subscriptionController);
        controller.setProximityListener(proximityListener);

        // フィードバックを開始する
        {
            controller.onStartCount();
            Util.sleep(100);
            assertEquals(callbackSec.value, 0);
            assertEquals(callbackNum.value, 1);
        }

        // 1秒経過ごとにフィードバックされる
        callbackNum.value = 0;
        for (int i = 0; i < ProximityCommandController.MAX_FEEDBACK_SEC; ++i) {
            clock.offset(1000 + 1);
            controller.onUpdate();
            Util.sleep(100);
            assertEquals(callbackSec.value, i + 1);
            assertEquals(callbackNum.value, i + 1);
        }

        // 限度を超えたらフィードバックされない
        {
            clock.offset(1000 + 1);
            Util.sleep(100);
            assertEquals(callbackSec.value, ProximityCommandController.MAX_FEEDBACK_SEC);
            assertEquals(callbackNum.value, ProximityCommandController.MAX_FEEDBACK_SEC);
        }
    }

    @Test
    public void 近接コマンドのBootを行える() throws Throwable {
        // 1秒経過ごとにフィードバックされる
        for (int i = 0; i < ProximityCommandController.MAX_FEEDBACK_SEC; ++i) {
            SubscriptionController subscriptionController = SubscriptionController.newUnitTestController();
            Clock clock = new Clock(System.currentTimeMillis());

            IntHolder holder = new IntHolder(-1);

            ProximityCommandController.CommandBootListener listener = (self, data) -> {
                holder.value = (data != null ? 1 : 0);
            };


            ProximityCommandController controller = new ProximityCommandController(getApplication(), clock, subscriptionController);
            controller.setBootListener(listener);
            controller.onStartCount();

            clock.offset(1000 * i + 1);
            controller.onUpdate();
            clock.offset(10);
            controller.onEndCount();
            Util.sleep(100);

            // 指定秒でコールバックされる
            assertNotEquals(holder.value, -1);
        }
    }
}