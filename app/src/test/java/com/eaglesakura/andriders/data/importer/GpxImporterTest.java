package com.eaglesakura.andriders.data.importer;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.central.data.CentralDataManager;
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

        SessionImportCommitter committer = new SessionImportCommitter(getContext());
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
        validate(allStatistics.getCalories()).from(2000.0); // 2000kcal以上消費している
        validate(allStatistics.getExercise()).from(20.0);   // 20EX以上経過している
    }


//    @Test
//    public void AACR2015のテストデータをインストールする() throws Exception {
////        try {
////            assertEquals(db.loadMaxSpeedKmh(), 61.0, 1.0);  // AACR最高速度
////            assertEquals(
////                    db.loadMaxSpeedKmh(importer.getImportStartDate().getTime(), importer.getImportEndDate().getTime()),
////                    db.loadMaxSpeedKmh(),
////                    0.01);  // AACR最高速度
////
////            SessionTotalCollection collection = db.loadTotal(SessionTotalCollection.Order.Asc);
////            assertNotNull(collection);
////            assertEquals(collection.getTotals().size(), 1);
////            assertEquals(collection.getSumDistanceKm(), 160, 10);
////            assertEquals(collection.getMaxSpeedKmh(), 61.0, 1.0);
////            assertEquals(collection.getLongestDateDistanceKm(), 160.0, 10);
////            assertTrue(collection.getRangeCalorie(importer.getImportStartDate().getTime(), importer.getImportEndDate().getTime()) > 2000);
////            assertTrue(collection.getRangeExercise(importer.getImportStartDate().getTime(), importer.getImportEndDate().getTime()) > 20);
////        } catch (Exception e) {
////
////        }
//    }
}
