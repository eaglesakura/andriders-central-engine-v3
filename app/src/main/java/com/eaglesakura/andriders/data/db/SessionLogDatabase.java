package com.eaglesakura.andriders.data.db;

import com.eaglesakura.andriders.central.log.LogStatistics;
import com.eaglesakura.andriders.dao.session.DaoMaster;
import com.eaglesakura.andriders.dao.session.DaoSession;
import com.eaglesakura.andriders.dao.session.DbSessionPoint;
import com.eaglesakura.andriders.dao.session.DbSessionPointDao;
import com.eaglesakura.andriders.system.AppStorageController;
import com.eaglesakura.andriders.error.io.AppDataNotFoundException;
import com.eaglesakura.andriders.error.io.AppDatabaseException;
import com.eaglesakura.andriders.error.io.AppIOException;
import com.eaglesakura.andriders.provider.AppControllerProvider;
import com.eaglesakura.andriders.sensor.InclinationType;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawLocation;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.db.DaoDatabase;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.collection.StringFlag;
import com.eaglesakura.geo.Geohash;
import com.eaglesakura.geo.GeohashGroup;
import com.eaglesakura.json.JSON;
import com.eaglesakura.util.Timer;

import org.greenrobot.greendao.database.StandardDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * セッションごとのログを保持する
 */
public class SessionLogDatabase extends DaoDatabase<DaoSession> {
    private static final int SUPPORTED_DATABASE_VERSION = 1;

    @Inject(AppControllerProvider.class)
    AppStorageController mStorageManager;

    public SessionLogDatabase(@NonNull Context context) {
        super(context, DaoMaster.class);

        Garnet.inject(this);
    }

    /**
     * 1個以上のアイテムを持つことを保証する
     */
    <T> void validate(List<T> itr) throws AppIOException {
        if (itr == null || itr.isEmpty()) {
            throw new AppDataNotFoundException();
        }
    }

    /**
     * 全記録の中から最高速度を取得する
     *
     * @return 最高速度[km/h]
     */
    public double loadMaxSpeedKmh() {
        return loadMaxSpeedKmh(0, Long.MAX_VALUE >> 1);
    }

    /**
     * 指定範囲内から最高速度を取得する
     *
     * @param startTime 開始時刻
     * @param endTime   終了時刻
     * @return 最高速度[km/h]
     */
    public double loadMaxSpeedKmh(long startTime, long endTime) {
        Timer timer = new Timer();
        try {
            List<DbSessionPoint> gpsSpeedList = session.getDbSessionPointDao().queryBuilder()
                    .orderDesc(DbSessionPointDao.Properties.ValueGpsSpeed)
                    .where(DbSessionPointDao.Properties.Date.ge(startTime), DbSessionPointDao.Properties.Date.le(endTime))
                    .limit(1)
                    .list();

            List<DbSessionPoint> sensorSpeedList = session.getDbSessionPointDao().queryBuilder()
                    .orderDesc(DbSessionPointDao.Properties.ValueSensorSpeed)
                    .where(DbSessionPointDao.Properties.Date.ge(startTime), DbSessionPointDao.Properties.Date.le(endTime))
                    .limit(1)
                    .list();

            float gpsSpeed = (gpsSpeedList.isEmpty() ? 0 : gpsSpeedList.get(0).getValueGpsSpeed());
            float sensorSpeed = (sensorSpeedList.isEmpty() ? 0 : sensorSpeedList.get(0).getValueSensorSpeed());

            return Math.max(gpsSpeed, sensorSpeed);
        } finally {
            AppLog.db("loadMaxSpeedKmh:range readTime[%d ms]", timer.end());
        }
    }

    /**
     * startTime～endTimeまでに開始されたセッションの統計情報を返却する
     *
     * @param startTime 開始時刻
     * @param endTime   終了時刻
     * @return 合計値 / セッションが存在しない場合はnullを返却
     */
    @Nullable
    public LogStatistics loadTotal(long startTime, long endTime) {
        throw new Error("NotImpl");
//        Timer timer = new Timer();
//        CloseableListIterator<DbSessionLog> iterator = null;
//        try {
//            QueryBuilder<DbSessionLog> builder = session.getDbSessionLogDao().queryBuilder();
//
//            AppLog.db("loadTotal start(%s) end(%s)", new Date(startTime).toString(), new Date(endTime).toString());
//
//            iterator = builder
//                    .where(DbSessionLogDao.Properties.StartTime.ge(startTime), DbSessionLogDao.Properties.StartTime.le(endTime))
//                    .orderAsc(DbSessionLogDao.Properties.StartTime)
//                    .listIterator();
//
//            if (iterator.hasNext()) {
//                return new SessionTotal(iterator);
//            } else {
//                return null;
//            }
//        } finally {
//            GreenDaoUtil.close(iterator);
//            AppLog.db("loadTotal readTime[%d ms]", timer.end());
//        }
    }

    /**
     * 自走状態のポイント
     */
    static final int POINT_FLAG_VALUE_ACTIVE = 0;

    /**
     * ヒルクライム状態のポイント
     */
    static final int POINT_FLAG_HILLCLIMB = 1;

    /**
     * 信頼できないGPS座標である
     */
    static final int POINT_FLAG_NO_REALIANCE = 2;

    /**
     * データを挿入する
     */
    public void insert(Iterable<RawCentralData> dataList) throws AppIOException {
        try {
            for (RawCentralData data : dataList) {
                DbSessionPoint pt = new DbSessionPoint();
                pt.setSessionId(data.session.sessionId);
                pt.setDate(new Date(data.centralStatus.date));
                pt.setCentralJson(JSON.encode(data));   // rawをそのままダンプ

                StringFlag flags = new StringFlag();
                if (data.session.isActiveMoving()) {
                    flags.add(POINT_FLAG_VALUE_ACTIVE);
                }

                // 必要な情報をダンプ
                if (data.sensor.heartrate != null) {
                    pt.setValueHeartrate((int) data.sensor.heartrate.bpm);
                }
                if (data.sensor.cadence != null) {
                    pt.setValueCadence((int) data.sensor.cadence.rpm);
                }
                if (data.sensor.speed != null) {
                    RawSensorData.RawSpeed speed = data.sensor.speed;
                    if ((speed.flags & RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_GPS) != 0) {
                        // GPS速度を設定
                        pt.setValueGpsSpeed(speed.speedKmPerHour);
                    } else {
                        // センサー速度
                        pt.setValueSensorSpeed(speed.speedKmPerHour);
                    }
                }
                if (data.sensor.location != null) {
                    RawLocation location = data.sensor.location;
                    // 周辺の大まかなハッシュ指定
                    GeohashGroup group = new GeohashGroup();
                    group.setGeohashLength(7);
                    group.updateLocation(location.latitude, location.longitude);
                    StringFlag geoFlags = new StringFlag();
                    for (String hash : group.getAdjustGeohash()) {
                        geoFlags.add(hash);
                    }
                    // 中央の詳細なハッシュ指定
                    pt.setValueGeohash10(Geohash.encode(location.latitude, location.longitude, 10));
                    pt.setValueGeohash7Peripherals(geoFlags.toString());

                    // 信頼性チェック
                    if (!location.locationReliance) {
                        flags.add(POINT_FLAG_NO_REALIANCE);
                    }
                    // 登坂チェック
                    if (location.inclinationType != null && location.inclinationType != InclinationType.None && location.inclinationPercent > 0) {
                        // 登坂領域である
                        flags.add(POINT_FLAG_HILLCLIMB);
                    }
                }
                pt.setValueRecordDistanceKm(data.session.distanceKm);
                pt.setValueRecordSumAltMeter(data.session.sumAltitudeMeter);
                pt.setValueFitCalories(data.session.fitness.calorie);
                pt.setValueFitExercise(data.session.fitness.exercise);
                pt.setValueActiveDistanceKm(data.session.activeDistanceKm);
                pt.setValueActiveTimeMs(data.session.activeTimeMs);

                // フラグ設定
                pt.setValueFlags(flags.toString());

                // インターバル機能が無いので、常に0
                pt.setValueIntervalIndex(0);

                // 挿入
                session.insertOrReplace(pt);
            }
        } catch (IOException e) {
            throw new AppIOException(e);
        } catch (Exception e) {
            throw new AppDatabaseException(e);
        }
    }

    @Override
    protected SQLiteOpenHelper createHelper() {
        return new SQLiteOpenHelper(context, mStorageManager.getExternalDatabasePath("v3_session_log.db").getAbsolutePath(), null, SUPPORTED_DATABASE_VERSION) {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                DaoMaster.createAllTables(new StandardDatabase(db), false);
            }
        };
    }
}
