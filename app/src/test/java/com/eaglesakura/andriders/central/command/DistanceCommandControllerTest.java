package com.eaglesakura.andriders.central.command;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.dao.command.DbCommand;
import com.eaglesakura.andriders.db.command.CommandData;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.thread.IntHolder;

import org.junit.Test;

import java.util.Random;

public class DistanceCommandControllerTest extends AppUnitTestCase {

    CommandData newCommand(float intervalDistance, int type, int flags) throws Throwable {
        CommandData result = new CommandData(new DbCommand());
        CommandData.RawExtra extra = result.getInternalExtra();
        extra.distanceKm = intervalDistance;
        extra.distanceType = type;
        extra.flags = flags;
        return result;
    }

    @Test
    public void 一度だけの実行が正しく行われる() throws Throwable {
        DistanceCommandController controller =
                new DistanceCommandController(getContext(), newCommand(1.0f, CommandData.DISTANCE_TYPE_SESSION, 0x00));

        IntHolder holder = new IntHolder();
        controller.setBootListener((self, data) -> {
            ++holder.value;
        });


        RawCentralData dummyData = new RawCentralData(Random.class);
        dummyData.session.distanceKm = 0.0f;

        controller.onUpdate();
        while (dummyData.session.distanceKm <= 10.5f) {
            dummyData.session.distanceKm += 0.001f;
            controller.mDataHandler.onReceived(dummyData);
            controller.onUpdate();
        }

        assertEquals(holder.value, 1);
    }

    @Test
    public void 繰り返しの実行が正しく行われる() throws Throwable {
        DistanceCommandController controller =
                new DistanceCommandController(getContext(), newCommand(1.0f, CommandData.DISTANCE_TYPE_SESSION, CommandData.DISTANCE_FLAG_REPEAT));

        IntHolder holder = new IntHolder();
        controller.setBootListener((self, data) -> {
            ++holder.value;
        });


        RawCentralData dummyData = new RawCentralData(Random.class);
        dummyData.session.distanceKm = 0.0f;

        controller.onUpdate();
        while (dummyData.session.distanceKm <= 10.5f) {
            dummyData.session.distanceKm += 0.001f;
            controller.mDataHandler.onReceived(dummyData);
            controller.onUpdate();
        }

        assertEquals(holder.value, 10);
    }

    @Test
    public void 当日合計距離が正しくハンドリングされる() throws Throwable {
        DistanceCommandController controller =
                new DistanceCommandController(getContext(), newCommand(1.0f, CommandData.DISTANCE_TYPE_TODAY, 0x00));

        IntHolder holder = new IntHolder();
        controller.setBootListener((self, data) -> {
            ++holder.value;
        });


        RawCentralData dummyData = new RawCentralData(Random.class);
        dummyData.today.distanceKm = 100.0f;

        controller.onUpdate();
        while (dummyData.today.distanceKm <= 100.5f) {
            dummyData.today.distanceKm += 0.001f;
            controller.mDataHandler.onReceived(dummyData);
            controller.onUpdate();
        }

        // 0.5km走っただけではまだコールされない
        assertEquals(holder.value, 0);


        // 101km時点で、最初のコールが行われる
        while (dummyData.today.distanceKm <= 101.5f) {
            dummyData.today.distanceKm += 0.001f;
            controller.mDataHandler.onReceived(dummyData);
            controller.onUpdate();
        }

        // 0.5km走っただけではまだコールされない
        assertEquals(holder.value, 1);
    }

    @Test
    public void 正しい基準で距離がピックアップされる() throws Throwable {

        RawCentralData dummyData = new RawCentralData(Random.class);
        dummyData.session.activeDistanceKm = 1.0f;
        dummyData.session.distanceKm = 10.0f;

        dummyData.today.activeDistanceKm = 5.0f;
        dummyData.today.distanceKm = 50.0f;

        // セッション合計
        {
            DistanceCommandController controller =
                    new DistanceCommandController(getContext(), newCommand(10.0f, CommandData.DISTANCE_TYPE_SESSION, 0x00));
            // 指定した距離が設定されている
            validate(controller.mNextTriggerDistance).eq(10.0f);

            // ダミーデータを受信させる
            controller.mDataHandler.onReceived(dummyData);
            validate(controller.mCurrentDistance).eq(dummyData.session.distanceKm);
        }

        // セッションアクティブ
        {
            DistanceCommandController controller =
                    new DistanceCommandController(getContext(), newCommand(10.0f, CommandData.DISTANCE_TYPE_SESSION, CommandData.DISTANCE_FLAG_ACTIVE_ONLY));
            // 指定した距離が設定されている
            validate(controller.mNextTriggerDistance).eq(10.0f);

            // ダミーデータを受信させる
            controller.mDataHandler.onReceived(dummyData);
            validate(controller.mCurrentDistance).eq(dummyData.session.activeDistanceKm);
        }

        // 今日合計
        {
            DistanceCommandController controller =
                    new DistanceCommandController(getContext(), newCommand(10.0f, CommandData.DISTANCE_TYPE_TODAY, 0x00));
            // 指定した距離が設定されている
            validate(controller.mNextTriggerDistance).eq(10.0f);

            // ダミーデータを受信させる
            controller.mDataHandler.onReceived(dummyData);
            validate(controller.mCurrentDistance).eq(dummyData.today.distanceKm);
        }

        // 今日アクティブ
        {
            DistanceCommandController controller =
                    new DistanceCommandController(getContext(), newCommand(10.0f, CommandData.DISTANCE_TYPE_TODAY, CommandData.DISTANCE_FLAG_ACTIVE_ONLY));
            // 指定した距離が設定されている
            validate(controller.mNextTriggerDistance).eq(10.0f);

            // ダミーデータを受信させる
            controller.mDataHandler.onReceived(dummyData);
            validate(controller.mCurrentDistance).eq(dummyData.today.activeDistanceKm);
        }
    }

}