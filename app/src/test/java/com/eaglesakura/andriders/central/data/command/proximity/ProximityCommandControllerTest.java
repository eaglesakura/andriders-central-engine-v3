package com.eaglesakura.andriders.central.data.command.proximity;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.model.command.CommandDataCollection;
import com.eaglesakura.andriders.service.command.ProximityData;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.rx.PendingCallbackQueue;
import com.eaglesakura.thread.IntHolder;
import com.eaglesakura.util.Util;

import org.junit.Test;

import java.util.ArrayList;

public class ProximityCommandControllerTest extends AppUnitTestCase {

    //    @Test
//    public void 毎秒フィードバックが送られてくる() throws Throwable {
//        IntHolder callbackSec = new IntHolder(-1);
//        IntHolder callbackNum = new IntHolder(0);
//        ProximityCommandController.ProximityListener proximityListener = new ProximityCommandController.ProximityListener() {
//            @Override
//            public void onRequestUserFeedback(ProximityCommandController self, int sec, @Nullable CommandData data) {
//                // 1秒ごとにフィードバックが送られる
//                assertEquals(callbackSec.value + 1, sec);
//                callbackSec.value = sec;
//                callbackNum.value++;
//            }
//
//            @Override
//            public void onProximityTimeOver(ProximityCommandController self, int sec) {
//
//            }
//        };
//
//        Clock clock = new Clock(System.currentTimeMillis());
//        PendingCallbackQueue callbackQueue = PendingCallbackQueue.newUnitTestController();
//        ProximityCommandController controller = new ProximityCommandController(getApplication(), clock);
//        controller.setProximityListener(proximityListener);
//
//        // フィードバックを開始する
//        {
//            controller.onStartCount(new CommandDataCollection(new ArrayList<>()));
//            Util.sleep(100);
//            assertEquals(callbackSec.value, 0);
//            assertEquals(callbackNum.value, 1);
//        }
//
//        // 1秒経過ごとにフィードバックされる
//        callbackNum.value = 0;
//        for (int i = 0; i < ProximityCommandController.MAX_FEEDBACK_SEC; ++i) {
//            clock.offset(1000 + 1);
//            controller.onUpdate(1.01);
//            Util.sleep(100);
//            assertEquals(callbackSec.value, i + 1);
//            assertEquals(callbackNum.value, i + 1);
//        }
//
//        // 限度を超えたらフィードバックされない
//        {
//            clock.offset(1000 + 1);
//            Util.sleep(100);
//            assertEquals(callbackSec.value, ProximityCommandController.MAX_FEEDBACK_SEC);
//            assertEquals(callbackNum.value, ProximityCommandController.MAX_FEEDBACK_SEC);
//        }
//    }
//
    @Test
    public void 近接コマンドのBootを行える() throws Throwable {
        // 1秒経過ごとにフィードバックされる
        for (int i = 0; i < 4; ++i) {
            PendingCallbackQueue callbackQueue = PendingCallbackQueue.newUnitTestController();
            Clock clock = new Clock(System.currentTimeMillis());

            IntHolder holder = new IntHolder(-1);

            ProximityCommandController.CommandBootListener listener = (self, data) -> {
                holder.value = (data != null ? 1 : 0);
            };


            ProximityCommandController controller = new ProximityCommandController(getApplication());
            controller.setBootListener(listener);
            controller.setCommands(new CommandDataCollection(new ArrayList<>()));

            // 近接開始
            {
                ProximityData data = new ProximityData(clock.nowDate(), true);
                controller.onUpdate(data);
            }
            clock.offset(1000 * i + 1);
            clock.offset(10);
            {
                ProximityData data = new ProximityData(clock.nowDate(), false);
                controller.onUpdate(data);
            }
            Util.sleep(100);

            // 指定秒でコールバックされる
            assertNotEquals(holder.value, -1);
        }
    }
}