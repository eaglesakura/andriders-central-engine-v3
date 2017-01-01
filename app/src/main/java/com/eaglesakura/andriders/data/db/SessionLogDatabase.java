package com.eaglesakura.andriders.data.db;

import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.dao.session.DaoMaster;
import com.eaglesakura.andriders.dao.session.DaoSession;
import com.eaglesakura.andriders.dao.session.DbSessionPoint;
import com.eaglesakura.andriders.dao.session.DbSessionPointDao;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.error.io.AppDataNotFoundException;
import com.eaglesakura.andriders.error.io.AppDatabaseException;
import com.eaglesakura.andriders.error.io.AppIOException;
import com.eaglesakura.andriders.provider.AppStorageProvider;
import com.eaglesakura.andriders.sensor.InclinationType;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawLocation;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.andriders.storage.AppStorageManager;
import com.eaglesakura.android.db.DaoDatabase;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Initializer;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.android.sql.SupportCursor;
import com.eaglesakura.collection.StringFlag;
import com.eaglesakura.geo.Geohash;
import com.eaglesakura.geo.GeohashGroup;
import com.eaglesakura.json.JSON;
import com.eaglesakura.lambda.Action1;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.StringUtil;
import com.eaglesakura.util.Util;

import org.greenrobot.greendao.database.StandardDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.eaglesakura.android.framework.util.AppSupportUtil.assertNotCanceled;

/**
 * セッションごとのログを保持する
 */
public class SessionLogDatabase extends DaoDatabase<DaoSession> {
    private static final int SUPPORTED_DATABASE_VERSION = 1;

    @Inject(AppStorageProvider.class)
    AppStorageManager mStorageManager;

    public SessionLogDatabase(@NonNull Context context) {
        super(context, DaoMaster.class);

        Garnet.inject(this);
    }

    @Initializer
    public void initialize() {
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
     * 日時を指定するクエリを取得する
     *
     * 値は「時刻がセッション開始時刻を含んでいるか」のみで判断される
     *
     * @param startTime 開始時刻, 0の場合は日付を指定しない
     * @param endTime   終了時刻, 0の場合は日付を指定しない
     * @return クエリ文字列
     */
    @NonNull
    public String getDateRangeQuery(long startTime, long endTime) {
        StringBuilder result = new StringBuilder();
        if (startTime > 0) {
            result.append(" SESSION_ID >= " + startTime);
        }

        if (endTime > 0) {
            // 条件を加える
            if (result.length() > 0) {
                result.append(" AND ");
            }
            result.append(" SESSION_ID <= " + endTime);
        }

        return result.toString();
    }

    SupportCursor logQuery(String sql) {
        return new SupportCursor(query(true, sql));
    }

    /**
     * 指定したセッションのCentralDataを全て列挙する
     *
     * @param sessionId      セッションID
     * @param cancelCallback キャンセルチェック
     */
    @NonNull
    public List<RawCentralData> listCentralData(long sessionId, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        StringBuilder query = new StringBuilder();
        query.append(
                "SELECT SESSION_ID, DATE, CENTRAL_JSON" +
                        " FROM DB_SESSION_POINT" +
                        " WHERE SESSION_ID = " + sessionId +
                        " ORDER BY DATE ASC"
        );

        try (SupportCursor cursor = logQuery(query.toString())) {
            if (!cursor.moveToFirst() || cursor.getCount() == 0) {
                throw new AppDataNotFoundException("Query Error :: " + sessionId);
            }

            List<RawCentralData> result = new ArrayList<>(cursor.getCount());
            do {
                assertNotCanceled(cancelCallback);

                cursor.nextLong(); // skip session id;
                cursor.nextLong(); // skip date
                String central = cursor.nextString();
                RawCentralData data = JSON.decode(central, RawCentralData.class);
                result.add(data);
            } while (cursor.moveToNext());

            return result;
        } catch (IOException e) {
            throw new AppDatabaseException(e);
        }
    }

    /**
     * startTime～endTimeまでに開始されたセッションのID一覧を取得する
     * セッションが見つからない場合は空リストを返す
     *
     * @param startTime 開始時刻
     * @param endTime   終了時刻
     */
    @NonNull
    public List<SessionHeader> loadHeaders(long startTime, long endTime, CancelCallback cancelCallback) throws AppException, TaskCanceledException {

        String whereTime = getDateRangeQuery(startTime, endTime);

        StringBuilder query = new StringBuilder();
        query.append(
                "SELECT SESSION_ID, " +
                        " MIN(DATE) AS SORT_DATE, MAX(DATE)" +
                        " FROM DB_SESSION_POINT");

        if (!StringUtil.isEmpty(whereTime)) {
            query.append(" WHERE " + whereTime);
        }
        query.append(" GROUP BY SESSION_ID ORDER BY SORT_DATE ASC");

        try (SupportCursor cursor = logQuery(query.toString())) {
            if (!cursor.moveToFirst()) {
                return new ArrayList<>();
            }

            List<SessionHeader> result = new ArrayList<>();

            do {
                long sessionId = cursor.nextLong();
                long sortDate = cursor.nextLong();
                long endDate = cursor.nextLong();
                result.add(new SessionHeader(sessionId, endDate));

                assertNotCanceled(cancelCallback);
            } while (cursor.moveToNext());

            return result;
        } catch (IOException e) {
            throw new AppDatabaseException(e);
        }
    }

    /**
     * 指定した範囲のログ打刻点を列挙する
     *
     * @param action 実行内容
     * @return チェックされたポイント数
     */
    public int eachSessionPoints(long startTime, long endTime, Action1<RawCentralData> action, CancelCallback cancelCallback) throws AppException, TaskCanceledException {

        String whereTime = getDateRangeQuery(startTime, endTime);
        StringBuilder query = new StringBuilder();
        query.append("SELECT CENTRAL_JSON, DATE\n" +
                "FROM DB_SESSION_POINT\n");

        if (!StringUtil.isEmpty(whereTime)) {
            query.append("WHERE " + whereTime);
        }
        query.append(" ORDER BY DATE ASC");

        try (SupportCursor cursor = logQuery(query.toString())) {
            if (!cursor.moveToFirst()) {
                return 0;
            }

            assertNotCanceled(cancelCallback);

            int count = cursor.getCount();
            do {
                String json = cursor.nextString();
                RawCentralData raw = JSON.decode(json, RawCentralData.class);
                action.action(raw);
            } while (cursor.moveToNext());
            return count;
        } catch (IOException e) {
            throw new AppDatabaseException(e);
        } catch (Throwable e) {
            AppException.throwAppException(e);
            return 0;
        }
    }

    /**
     * startTime～endTimeまでに開始されたセッションの統計情報を返却する
     *
     * @param startTime 開始時刻, 0の場合はユーザーの全ログ探索
     * @param endTime   終了時刻, 0の場合はユーザーの全ログ探索
     * @return 合計値 / セッションが存在しない場合はnullを返却
     */
    @Nullable
    public LogStatistics loadTotal(long startTime, long endTime, CancelCallback cancelCallback) throws AppException, TaskCanceledException {

        String whereTime = getDateRangeQuery(startTime, endTime);

        StringBuilder query = new StringBuilder();
        query.append("SELECT\n" +
                "\tSESSION_ID,\n" +
                "\tMIN(DATE) AS SORT_DATE, MAX(DATE),\n" +
                "\tMAX(VALUE_HEARTRATE),\n" +
                "\tMAX(VALUE_CADENCE),\n" +
                "\tMAX(VALUE_SENSOR_SPEED),\n" +
                "\tMAX(VALUE_GPS_SPEED),\n" +
                "\tMAX(VALUE_FIT_CALORIES),\n" +
                "\tMAX(VALUE_FIT_EXERCISE),\n" +
                "\tMAX(VALUE_RECORD_DISTANCE_KM),\n" +
                "\tMAX(VALUE_RECORD_SUM_ALT_METER),\n" +
                "\tMAX(VALUE_ACTIVE_DISTANCE_KM),\n" +
                "\tMAX(VALUE_ACTIVE_TIME_MS)\n" +
                "FROM DB_SESSION_POINT\n");

        if (!StringUtil.isEmpty(whereTime)) {
            query.append("WHERE " + whereTime);
        }
        query.append(" GROUP BY SESSION_ID ORDER BY SORT_DATE ASC");

        long sumActiveTimeMs = 0;
        double sumActiveDistanceKm = 0;
        double sumAltitudeMeter = 0;
        double sumDistanceKm = 0;
        double sumCalories = 0;
        double sumExercise = 0;
        int maxCadence = 0;
        int maxHeartrate = 0;
        double maxSpeedKmh = 0;
        Date startDate = null;
        Date endDate = null;

        int numSessions = 0;    // 含まれるセッション数
        int numDates = 0;   // 含まれる日次数
        double longestDateDistanceKm = 0;   // 最長到達距離
        double maxDateAltitudeMeter = 0;    // 最大ヒルクライム高度


        try (SupportCursor cursor = logQuery(query.toString())) {
            if (!cursor.moveToFirst()) {
                return null;
            }

            // 現在チェック日のテンポラリ
            long currentDateId = 0;
            double currentDateDistance = 0;
            double currentDateAltitude = 0;
            do {
                long sessionId = cursor.nextLong();
                long dateId = SessionHeader.toDateId(sessionId);
                long sessionStartDate = cursor.nextLong();
                long sessionEndDate = cursor.nextLong();
                if (startDate == null) {
                    startDate = new Date(sessionStartDate);
                }
                endDate = new Date(sessionEndDate);

                maxHeartrate = Math.max(maxHeartrate, Util.getInt(cursor.nextInt(), 0));    // 心拍
                maxCadence = Math.max(maxCadence, Util.getInt(cursor.nextInt(), 0));        // ケイデンス
                maxSpeedKmh = Math.max(maxSpeedKmh, Util.getDouble(cursor.nextDouble(), 0.0));  // センサー由来速度
                maxSpeedKmh = Math.max(maxSpeedKmh, Util.getDouble(cursor.nextDouble(), 0.0));  // GPS由来速度

                sumCalories += Util.getInt(cursor.nextInt(), 0);    // セッション単位の合計消費カロリーを更に足し込む
                sumExercise += Util.getInt(cursor.nextInt(), 0);    // セッション単位の合計エクササイズを更に足し込む

                final double sessionDistance = Util.getDouble(cursor.nextDouble(), 0.0);  // セッション単位の走行距離
                final double sessionAltitudeMeter = Util.getDouble(cursor.nextDouble(), 0.0);   // セッション単位の獲得標高
                sumDistanceKm += sessionDistance; // セッション距離を加算する
                sumAltitudeMeter += sessionAltitudeMeter; // セッション獲得標高を加算する

                sumActiveDistanceKm += Util.getDouble(cursor.nextDouble(), 0.0);    // 自走距離を合計する
                sumActiveTimeMs += Util.getLong(cursor.nextLong(), 0);              // 自走時間を合計する

                if (dateId != currentDateId) {
                    // 情報をリセットする
                    ++numDates;
                    currentDateId = dateId;
                    currentDateDistance = 0;
                    currentDateAltitude = 0;
                }

                // セッション日の走行距離を足し込む
                currentDateDistance += sessionDistance;
                currentDateAltitude += sessionAltitudeMeter;

                // 日毎最大値をチェックする
                longestDateDistanceKm = Math.max(longestDateDistanceKm, currentDateDistance);
                maxDateAltitudeMeter = Math.max(maxDateAltitudeMeter, currentDateAltitude);
                ++numSessions;
                assertNotCanceled(cancelCallback);
            } while (cursor.moveToNext());
        } catch (IOException e) {
            throw new AppDatabaseException(e);
        }

        return new LogStatistics(
                startDate, endDate,
                (int) sumActiveTimeMs, (float) sumActiveDistanceKm,
                (float) sumAltitudeMeter, (float) sumDistanceKm,
                (float) sumCalories, (float) sumExercise,
                (short) maxCadence, (short) maxHeartrate, (float) maxSpeedKmh,
                numSessions, numDates, (float) longestDateDistanceKm, (float) maxDateAltitudeMeter
        );
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
     * 指定したセッションをすべて削除する
     *
     * @param sessionId セッションID
     */
    public void deleteSession(long sessionId) throws AppException {
        getSession().getDbSessionPointDao().queryBuilder()
                .where(DbSessionPointDao.Properties.SessionId.eq(sessionId))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

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
                        pt.setValueGpsSpeed(speed.speedKmh);
                    } else {
                        // センサー速度
                        pt.setValueSensorSpeed(speed.speedKmh);
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

                    // 座標を転転記
                    pt.setValueGpsLat((float) location.latitude);
                    pt.setValueGpsLng((float) location.longitude);
                    pt.setValueGpsAlt((float) location.altitude);

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
