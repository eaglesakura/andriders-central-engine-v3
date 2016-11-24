package com.eaglesakura.andriders.data.importer;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.central.data.CentralDataManagerTest;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.data.gpx.GpxParser;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.thread.IntHolder;
import com.eaglesakura.util.DateUtil;
import com.eaglesakura.util.LogUtil;

import org.junit.Test;

import java.io.File;
import java.util.Date;
import java.util.TimeZone;

public class GpxImporterTest extends AppUnitTestCase {

    @Override
    public void onSetup() {
        super.onSetup();
        LogUtil.setLogEnable("App.DB", false);
    }

    @Test
    public void GPXのサンプルファイルからパースを行える_AACR2015() throws Throwable {
        GpxImporter build = new GpxImporter.Builder(getContext())
                .parser(GpxParser.DateOption.AddTimeZone)
                .file(new File("../sdk/src/test/assets/gpx/sample-aacr2015.gpx").getAbsoluteFile())
                .build();

        IntHolder gpxSegmentNum = new IntHolder();

        Holder<Date> startDate = new Holder<>();
        Holder<Date> endDate = new Holder<>();

        build.install(new SessionImporter.Listener() {
            int mGpxPointNum;

            @Override
            public void onSessionStart(SessionImporter self, CentralDataManager dataManager) throws AppException {
                Date date = new Date(dataManager.getSessionId());
                AppLog.test("Session index[%d] Start Date[%s]", gpxSegmentNum.value, date);

                gpxSegmentNum.add(1);
                mGpxPointNum = 0;   // セッション単位で初期化
                if (startDate.get() == null) {
                    startDate.set(date);
                }
            }

            @Override
            public void onPointInsert(SessionImporter self, CentralDataManager dataManager, RawCentralData latest) throws AppException {
                ++mGpxPointNum;
            }

            @Override
            public void onSessionFinished(SessionImporter self, CentralDataManager dataManager) throws AppException {
                // 10点以上のポイントを持っている
                validate(mGpxPointNum).from(10);

                // 最終地点情報
                endDate.set(new Date(dataManager.getLatestCentralData().centralStatus.date));
                AppLog.test("  - Session End Date[%s]", endDate.get());
            }
        }, () -> false);

        // 1以上のセッションが読み込まれている
        validate(gpxSegmentNum.value).from(1);
        assertEquals(DateUtil.getYear(startDate.get(), TimeZone.getDefault()), 2015);
        assertEquals(DateUtil.getMonth(startDate.get(), TimeZone.getDefault()), 5);
        assertEquals(DateUtil.getDay(startDate.get(), TimeZone.getDefault()), 24);
        assertEquals(DateUtil.getHour(startDate.get(), TimeZone.getDefault()), 5);

        assertEquals(DateUtil.getYear(endDate.get(), TimeZone.getDefault()), 2015);
        assertEquals(DateUtil.getMonth(endDate.get(), TimeZone.getDefault()), 5);
        assertEquals(DateUtil.getDay(endDate.get(), TimeZone.getDefault()), 24);
        assertEquals(DateUtil.getHour(endDate.get(), TimeZone.getDefault()), 16);

    }

    @Test
    public void GPXのサンプルデータを書き込める_AACR2015() throws Throwable {
        GpxImporter build = new GpxImporter.Builder(getContext())
                .parser(GpxParser.DateOption.AddTimeZone)
                .file(new File("../sdk/src/test/assets/gpx/sample-aacr2015.gpx").getAbsoluteFile())
                .build();

        SessionImportCommitter committer = new SessionImportCommitter(getContext()) {
            @Override
            public void onPointInsert(SessionImporter self, CentralDataManager dataManager, RawCentralData latest) throws AppException {
                // CentralをValidate
                try {
                    CentralDataManagerTest.assertCentralData(dataManager, latest);
                } catch (Error e) {
                    throw e;
                } catch (Throwable e) {
                    e.printStackTrace();
                    fail();
                }

                // 心拍を適当に設定する
                dataManager.setHeartrate((int) (160.0 + 30.0 * Math.random()));

                // 半分の時間自走する
                long diffTimeMs = dataManager.getSessionInfo().getSessionClock().absDiff(dataManager.getSessionId());
                if ((diffTimeMs / (1000 * 60)) % 2 == 0) {
                    dataManager.setSpeedAndCadence((float) (70.0 + (30 * Math.random())), 0, -1, -1);
                }

                super.onPointInsert(self, dataManager, latest);
            }
        };
        try (SessionLogDatabase db = committer.openDatabase()) {
            db.runInTx(() -> {
                build.install(committer, () -> false);
                return 0;
            });
        }

        CentralLogManager logManager = Garnet.instance(AppManagerProvider.class, CentralLogManager.class);
        assertNotNull(logManager);

        LogStatistics allStatistics = logManager.loadAllStatistics();
        assertNotNull(allStatistics);

        validate(allStatistics.getMaxSpeedKmh()).delta(10.0).eq(60.0);   // AACR最高速度
        validate(allStatistics.getSumDistanceKm()).delta(10.0).eq(160.0);   // AACR走行距離
        validate(allStatistics.getCalories()).from(2000); // 160bpm以上で動き続けるため、2000kcal以上は最低限動いている
        validate(allStatistics.getExercise()).from(20.0);   // 20EX以上経過している
    }
}
