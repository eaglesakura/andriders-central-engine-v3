package com.eaglesakura.andriders.data.importer;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.central.data.CentralDataManagerTest;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.log.SessionHeaderCollection;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.data.backup.CentralBackupImporter;
import com.eaglesakura.andriders.data.backup.serialize.BackupInformation;
import com.eaglesakura.andriders.data.backup.serialize.SessionBackup;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.data.gpx.GpxParser;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.gen.prop.CentralServiceSettings;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.property.PropertyStore;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.thread.IntHolder;
import com.eaglesakura.thread.LongHolder;
import com.eaglesakura.util.DateUtil;
import com.eaglesakura.util.LogUtil;

import org.junit.Test;

import android.net.Uri;
import android.support.annotation.NonNull;

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
                .parser(GpxParser.DateOption.None)
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
                .parser(GpxParser.DateOption.None)
                .file(new File("../sdk/src/test/assets/gpx/sample-aacr2015.gpx").getAbsoluteFile())
                .build();

        LongHolder firstSession = new LongHolder();

        SessionImportCommitter committer = new SessionImportCommitter(getContext()) {
            @Override
            public void onSessionStart(SessionImporter self, CentralDataManager dataManager) throws AppException {
                super.onSessionStart(self, dataManager);
                if (firstSession.value == 0) {
                    firstSession.value = dataManager.getSessionId();
                }
            }

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

        // 今日とトータルを取得する
        // 1日分のデータしか無いので、両者は合致するはずである
        LogStatistics[] testStatisticses = {
                logManager.loadAllStatistics(() -> false),
                logManager.loadDailyStatistics(DateUtil.getTime(TimeZone.getDefault(), 2015, 5, 24).getTime(), () -> false)
        };

        // バックアップを取る
        {
            File exportFile = new File(getContext().getFilesDir(), "export.zip");
            AppLog.test("Export Path[%s]", exportFile.getAbsolutePath());
            Uri export = Uri.fromFile(exportFile);

            // ログをエクスポートできる
            logManager.exportDailySessions(firstSession.value, export, () -> false);

            // エクスポートしたログを取得できる
            CentralBackupImporter centralBackupImporter = new CentralBackupImporter(getContext());

            IntHolder sessionCount = new IntHolder();

            centralBackupImporter.parse(new CentralBackupImporter.Callback() {
                @Override
                public void onLoadInformation(@NonNull CentralBackupImporter self, @NonNull BackupInformation info, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
                    assertNotNull(info);
                    assertNotEmpty(info.appVersionName);
                }

                @Override
                public void onLoadSession(@NonNull CentralBackupImporter self, @NonNull SessionBackup session, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
                    validate(session.points).notEmpty().allNotNull().each(pt -> {
                        assertNotNull(pt.centralStatus);
                    });

                    sessionCount.add(1);
                }
            }, export, () -> false);
            assertNotEquals(sessionCount.value, 0);
        }

        for (LogStatistics log : testStatisticses) {
            assertNotNull(log);
            validate(log.getMaxSpeedKmh()).delta(10.0).eq(60.0);   // AACR最高速度
            validate(log.getSumDistanceKm()).delta(10.0).eq(160.0);   // AACR走行距離
            validate(log.getLongestDateDistanceKm()).eq(log.getSumDistanceKm()); // AACR走行距離が、そのまま最長到達距離となるはずである
            validate(log.getMaxDateAltitudeMeter()).eq(log.getSumAltitudeMeter()); // 最大獲得標高は同じである
            validate(log.getDateCount()).eq(1); // セッションは1日だけのはずである
            validate(log.getCalories()).from(2000); // 160bpm以上で動き続けるため、2000kcal以上は最低限動いている
            validate(log.getExercise()).from(20.0);   // 20EX以上経過している
        }

        // ログヘッダを生成する
        {
            SessionHeaderCollection sessionHeaderCollection = logManager.listAllHeaders(() -> false);
            assertNotNull(sessionHeaderCollection);
            validate(sessionHeaderCollection.list()).notEmpty().each((index, header) -> {
                AppLog.test("Session id[%d] date[%d]", header.getSessionId(), header.getDateId());

                LogStatistics statistics = logManager.loadSessionStatistics(header, () -> false);
                assertNotNull(statistics);
                AppLog.test("  - Start[%s] End[%s] Speed[%.1f km/h] Distance[%.1f km] Alt[%d m]",
                        statistics.getStartDate(), statistics.getEndDate(),
                        statistics.getMaxSpeedKmh(),
                        statistics.getSumDistanceKm(), (int) statistics.getSumAltitudeMeter()
                );

                // すべてのポイントが列挙できることを確認
                int points = logManager.eachDailySessionPoints(header.getSessionId(), point -> {
                    assertNotNull(point);
                    assertNotNull(point.centralStatus);
                    assertNotNull(point.record);
                    assertNotNull(point.session);
                    assertNotNull(point.specs);
                }, () -> false);
                validate(points).from(10);
                AppLog.test("Each Points num[%d]", points);

                // 全てのCentral Pointがロードできることを確認
                DataCollection<RawCentralData> centralDataCollection = logManager.listSessionPoints(header.getSessionId(), () -> false);
                assertNotNull(centralDataCollection);
                validate(centralDataCollection.list()).notEmpty().allNotNull().sizeFrom(10).sizeTo(points);

                // 削除ができることを確認
                logManager.delete(header);
                // 削除が完了していることを確認
                assertNull(logManager.listAllHeaders(() -> false).find(it -> it.getSessionId() == header.getSessionId()));
            });
        }
    }

    @Test
    public void GPXのサンプルデータを書き込める_AACR2016() throws Throwable {
        GpxImporter build = new GpxImporter.Builder(getContext())
                .parser(GpxParser.DateOption.None)
                .file(new File("../sdk/src/test/assets/gpx/sample-aacr2016.gpx").getAbsoluteFile())
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

        // 今日とトータルを取得する
        // 1日分のデータしか無いので、両者は合致するはずである
        LogStatistics[] testStatisticses = {
                logManager.loadAllStatistics(() -> false),
                logManager.loadDailyStatistics(DateUtil.getTime(TimeZone.getDefault(), 2016, 5, 22).getTime(), () -> false)
        };

        for (LogStatistics log : testStatisticses) {
            assertNotNull(log);
            validate(log.getMaxSpeedKmh()).delta(10.0).eq(70.0);   // AACR最高速度
            validate(log.getSumDistanceKm()).delta(10.0).eq(160.0);   // AACR走行距離
            validate(log.getLongestDateDistanceKm()).eq(log.getSumDistanceKm()); // AACR走行距離が、そのまま最長到達距離となるはずである
            validate(log.getMaxDateAltitudeMeter()).eq(log.getSumAltitudeMeter()); // 最大獲得標高は同じである
            validate(log.getDateCount()).eq(1); // セッションは1日だけのはずである
            validate(log.getCalories()).from(2000); // 160bpm以上で動き続けるため、2000kcal以上は最低限動いている
            validate(log.getExercise()).from(20.0);   // 20EX以上経過している
        }

        // ログヘッダを生成する
        {
            SessionHeaderCollection sessionHeaderCollection = logManager.listAllHeaders(() -> false);
            assertNotNull(sessionHeaderCollection);
            validate(sessionHeaderCollection.list()).notEmpty().each((index, header) -> {
                AppLog.test("Session id[%d] date[%d]", header.getSessionId(), header.getDateId());

                LogStatistics statistics = logManager.loadSessionStatistics(header, () -> false);
                assertNotNull(statistics);
                AppLog.test("  - Start[%s] End[%s] Speed[%.1f km/h] Distance[%.1f km] Alt[%d m]",
                        statistics.getStartDate(), statistics.getEndDate(),
                        statistics.getMaxSpeedKmh(),
                        statistics.getSumDistanceKm(), (int) statistics.getSumAltitudeMeter()
                );
            });
        }
    }

    @Test
    public void GPS速度を無効化した場合速度は常に0である() throws Throwable {
        GpxImporter build = new GpxImporter.Builder(getContext()) {
            @Override
            public GpxImporter build() {
                if (mParser == null) {
                    parser(GpxParser.DateOption.None);
                }
                return new GpxImporter(this) {
                    @Override
                    SessionInfo newSession(Clock clock) {
                        return new SessionInfo.Builder(mContext, clock) {
                            @Override
                            protected CentralServiceSettings newCentralServiceSettings(PropertyStore store) {
                                return new CentralServiceSettings(store) {
                                    @Override
                                    public boolean isGpsSpeedEnable() {
                                        // テストのためGPS速度を無効化する
                                        return false;
                                    }
                                };
                            }
                        }.build();
                    }
                };
            }
        }.parser(GpxParser.DateOption.None)
                .file(new File("../sdk/src/test/assets/gpx/sample-aacr2016.gpx").getAbsoluteFile())
                .build();

        build.install(new SessionImporter.Listener() {
            @Override
            public void onSessionStart(SessionImporter self, CentralDataManager dataManager) throws AppException {

            }

            // 速度は常に0である
            @Override
            public void onPointInsert(SessionImporter self, CentralDataManager dataManager, RawCentralData latest) throws AppException {
                if (latest.sensor.speed != null) {
                    validate(latest.sensor.speed.speedKmh).eq(0.0);
                    assertEquals(latest.sensor.speed.flags & RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_GPS, 0x00);
                }
                validate(latest.record.maxSpeedKmh).eq(0.0);
                validate(latest.record.maxSpeedKmhSession).eq(0.0);
                validate(latest.record.maxSpeedKmhToday).eq(0.0);
            }

            @Override
            public void onSessionFinished(SessionImporter self, CentralDataManager dataManager) throws AppException {

            }
        }, () -> false);
    }
}
