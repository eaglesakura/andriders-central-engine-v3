package com.eaglesakura.andriders.central;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.data.gpx.Gpx;
import com.eaglesakura.andriders.data.gpx.GpxParser;
import com.eaglesakura.andriders.data.gpx.GpxPoint;
import com.eaglesakura.andriders.data.gpx.GpxSegment;
import com.eaglesakura.andriders.db.session.SessionLogDatabase;
import com.eaglesakura.andriders.db.session.SessionTotal;
import com.eaglesakura.andriders.sensor.HeartrateZone;
import com.eaglesakura.andriders.sensor.SpeedZone;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawGeoPoint;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.IOUtil;
import com.eaglesakura.util.MathUtil;
import com.eaglesakura.util.SerializeUtil;
import com.eaglesakura.util.Timer;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

public class CentralDataManagerTest extends AppUnitTestCase {
    /**
     * サンプルの距離はAACR開始～折り返しとなっている。
     * 計算上、約53km程度となる。
     */
    final double SAMPLE_START_LATITUDE = 36.222704;
    final double SAMPLE_START_LONGITUDE = 137.883526;
    final double SAMPLE_START_ALTITUDE = 100;

    final double SAMPLE_END_LATITUDE = 36.705877;
    final double SAMPLE_END_LONGITUDE = 137.834935;
    final double SAMPLE_END_ALTITUDE = 1100;

    /**
     * サンプルの二点間距離
     *
     * http://keisan.casio.jp/exec/system/1257670779
     */
    final double SAMPLE_DISTANCE_KM = 53.9622;

    /**
     * セッション開始時刻が正常であることを検証する
     */
    @Test
    public void セッション開始時刻が正確であることを確認する() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = new CentralDataManager(getContext(), clock);

        assertNotNull(data.getSessionId());
        assertNotEquals(data.getSessionId(), "");
        assertEquals(data.mSessionTime.getStartDate(), START_TIME);
        assertEquals(data.mFitnessData.getUserWeight(), USER_WEIGHT, 0.1);

        // 時間を分割して1分経過させる
        for (int i = 0; i < 60; ++i) {
            clock.offset(1000);
            data.onUpdate();
        }
        assertEquals(data.mSessionTime.getSessionDurationMs(), 1000 * 60); // データも1分経過している
    }

    void assertObject(CentralDataManager data, RawCentralData centralData) throws Exception {
        assertNotNull(data);
        assertNotNull(data.mSpeedData.getSpeedZone());
        assertNotNull(data.mFitnessData.getZone());
        assertNotNull(data.mCadenceData.getZone());

        assertNotNull(centralData);
        assertNotNull(centralData.session);
        assertNotNull(centralData.session.fitness);
        assertNotNull(centralData.today);
        assertNotNull(centralData.today.fitness);
        assertNotNull(centralData.centralStatus);
        assertNotNull(centralData.specs);
        assertNotNull(centralData.record);

        // ログチェックを行う
        {
            assertNotNull(centralData.session.sessionId);
            assertNull(centralData.today.sessionId);
            assertTrue(centralData.session.startTime >= centralData.today.startTime);
            assertTrue(centralData.session.durationTimeMs <= centralData.today.durationTimeMs);

            assertTrue(centralData.session.activeDistanceKm <= centralData.today.activeDistanceKm);
            assertTrue(centralData.session.distanceKm <= centralData.today.distanceKm);
            assertTrue(centralData.session.sumAltitudeMeter <= centralData.today.sumAltitudeMeter);
            assertTrue(centralData.session.fitness.calorie <= centralData.today.fitness.calorie);
            assertTrue(centralData.session.fitness.exercise <= centralData.today.fitness.exercise);
        }
        // 記録チェックを行う
        {
            // 速度は [全体] >= [今日] >= [セッション]である
            assertTrue(centralData.record.maxSpeedKmh >= centralData.record.maxSpeedKmhToday);
            assertTrue(centralData.record.maxSpeedKmhToday >= centralData.record.maxSpeedKmhSession);

            // 心拍も同じくチェックする
            assertTrue(centralData.record.maxHeartrateToday >= centralData.record.maxHeartrateSession);
        }

        assertEquals(data.isActiveMoving(), centralData.session.isActiveMoving());
        assertEquals(centralData.centralStatus.date, data.now());

        if (data.mFitnessData.valid()) {
            assertNotNull(centralData.sensor.heartrate);
            validate(data.mFitnessData.getHeartrate())
                    .from(50)
                    .to(220);
            assertEquals((short) data.mFitnessData.getHeartrate(), centralData.sensor.heartrate.bpm);
            assertEquals(data.mFitnessData.getHeartrateDataTime(), centralData.sensor.heartrate.date);
            assertEquals(data.mFitnessData.getZone(), centralData.sensor.heartrate.zone);

            // フィットネスチェック
            assertTrue(centralData.record.maxHeartrateToday >= centralData.sensor.heartrate.bpm);
            assertTrue(centralData.record.maxHeartrateSession >= centralData.sensor.heartrate.bpm);
        } else {
            assertNull(centralData.sensor.heartrate);
        }

        if (data.mCadenceData.valid()) {
            validate(data.mCadenceData.getCadenceRpm())
                    .from(0)
                    .to(220);
            assertNotNull(centralData.sensor.cadence);
            assertEquals((short) data.mCadenceData.getCadenceRpm(), centralData.sensor.cadence.rpm);
            assertEquals(data.mCadenceData.getCrankRevolution(), centralData.sensor.cadence.crankRevolution);
            assertEquals(data.mCadenceData.getZone(), centralData.sensor.cadence.zone);
            assertEquals(data.mCadenceData.getUpdatedDate(), centralData.sensor.cadence.date);
        } else {
            assertNull(centralData.sensor.cadence);
        }

        if (data.mLocationData.valid()) {
            assertNotNull(centralData.sensor.location);
            assertNotNull(data.mLocationData.getInclinationType());

            validate(data.mLocationData.getLatitude())
                    .to(90);

            // latは+-90までしか存在しない
            validate(data.mLocationData.getLatitude())
                    .from(-90)
                    .to(90);

            // lonは+-180までしか存在しない
            validate(data.mLocationData.getLongitude())
                    .from(-180)
                    .to(180);

            // エベレストよりも高い場所には登れない
            validate(data.mLocationData.getAltitudeMeter())
                    .to(9000);

            assertEquals(data.mLocationData.getLatitude(), centralData.sensor.location.latitude, 0.001);
            assertEquals(data.mLocationData.getLongitude(), centralData.sensor.location.longitude, 0.001);
            assertEquals(data.mLocationData.getAltitudeMeter(), centralData.sensor.location.altitude, 0.001);
            assertEquals(data.mLocationData.getAccuracyMeter(), centralData.sensor.location.locationAccuracy, 0.001);
            assertEquals(data.mLocationData.isReliance(), centralData.sensor.location.locationReliance);
            assertEquals(data.mLocationData.getUpdatedTime(), centralData.sensor.location.date);
            assertEquals(data.mLocationData.getInclinationPercent(), centralData.sensor.location.inclinationPercent, 0.001);
            assertEquals(data.mLocationData.getInclinationType(), centralData.sensor.location.inclinationType);
        } else {
            assertNull(centralData.sensor.location);
        }

        if (data.mSpeedData.getSource() != SpeedData.SpeedSource.None) {
            assertNotNull(centralData.sensor.speed);
            switch (data.mSpeedData.getSource()) {
                case GPS:
                    assertEquals(centralData.sensor.speed.flags & RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_GPS, RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_GPS);
                    break;
                case Sensor:
                    assertEquals(centralData.sensor.speed.flags & RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_SENSOR, RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_SENSOR);
                    break;
            }

            assertEquals(data.mSpeedData.getSpeedZone(), centralData.sensor.speed.zone);
            assertEquals(data.mSpeedData.getSpeedKmh(), centralData.sensor.speed.speedKmPerHour, 0.1);

            // 速度チェック
            validate(centralData.record.maxSpeedKmh)                // 最高速度は
                    .from(centralData.record.maxSpeedKmhToday)      // 今日の最高速度以上
                    .from(centralData.record.maxSpeedKmhSession)    // セッションの最高速度以上
                    .from(centralData.sensor.speed.speedKmPerHour); // 現在速度以上

            validate(centralData.record.maxSpeedKmhToday)           // 今日の最高速度は
                    .from(centralData.record.maxSpeedKmhSession)    // セッションの最高速度以上
                    .from(centralData.sensor.speed.speedKmPerHour); // 現在速度以上

            validate(centralData.record.maxSpeedKmhSession)         // セッション最高速度は
                    .from(centralData.sensor.speed.speedKmPerHour); // 現在速度以上
        } else {
            assertNull(centralData.sensor.speed);
        }

        // シリアライズとデシリアライズが正常である
        byte[] bytes = SerializeUtil.serializePublicFieldObject(centralData, true);
        assertFalse(CollectionUtil.isEmpty(bytes));
        RawCentralData deserialized = SerializeUtil.deserializePublicFieldObject(RawCentralData.class, bytes);
        assertEquals(centralData, deserialized);
    }

    /**
     * ケイデンスセンサーのテストでは、速度が計算通りになることを検証する。
     *
     * ケイデンスセンサーが利用されている場合、アクティブ時間も同時にカウントアップされなければならない。
     *
     * ケイデンスと速度のリファレンス : http://dirtjapan.com/modules/pico/content0003.html
     * 39T-19Tギアの場合、クランクに対してホイール倍率が2.05となる。
     */
    @Test
    public void ケイデンスセンサーによる速度を測定する() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = new CentralDataManager(getContext(), clock);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        double maxSpeed = 0.0;
        int crankRevolution = 0;
        {
            while (current < 1.0) {
                clock.offset((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));

                final float SAMPLE_CRANK_RPM = (float) (90.0 + Math.random() * 10.0);
                final float gear = 2.05f; // 19T-39T
                assertEquals(data.setSpeedAndCadence(SAMPLE_CRANK_RPM, ++crankRevolution, SAMPLE_CRANK_RPM * gear, (int) (crankRevolution * gear)), 2);
                data.onUpdate();
                current += OFFSET_TIME_HOUR;


                assertObject(data, data.getLatestCentralData());

                assertTrue(data.isActiveMoving()); // ケイデンスから速度を得ているので、アクティブなはずである

                // 速度をチェックする
                assertNotEquals(data.mSpeedData.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない

                // このギア比では速度は20～25km/h程度になるはずである
                assertTrue(data.mSpeedData.getSpeedKmh() > 20.0);
                assertTrue(data.mSpeedData.getSpeedKmh() < 30.0);
                assertTrue(data.isActiveMoving());

                maxSpeed = Math.max(data.mSpeedData.getSpeedKmh(), maxSpeed);
            }
        }

        // 結果だけを出力
        AppLog.test("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), data.mSpeedData.getSpeedZone().name());

        // 最高速度が一致する
        assertEquals(data.mSpeedData.getMaxSpeedKmh(), maxSpeed, 0.1);
        // 常に自走であるので、セッション時間と自走時間は一致する
        assertEquals(data.mSessionTime.getSessionDurationMs(), data.mSessionTime.getActiveTimeMs());
    }

    @Test
    public void ケイデンス停止で速度が得られている場合はアクティブとして扱わない() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = new CentralDataManager(getContext(), clock);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        double maxSpeed = 0.0;
        int crankRevolution = 0;
        {
            while (current < 1.0) {
                clock.offset((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));
                final long OFFSET_TIME_MS = (long) ((double) (1000 * 60 * 60) * current);

                final float SAMPLE_CRANK_RPM = (float) (90.0 + Math.random() * 10.0);
                final float gear = 2.05f; // 19T-39T
                assertEquals(data.setSpeedAndCadence(0, 0, SAMPLE_CRANK_RPM * gear, (int) (crankRevolution * gear)), 2);
                data.onUpdate();
                current += OFFSET_TIME_HOUR;

                assertObject(data, data.getLatestCentralData());

                // 速度をチェックする
                assertNotEquals(data.mSpeedData.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない
                // このギア比では速度は20～25km/h程度になるはずである
                assertTrue(data.mSpeedData.getSpeedKmh() > 20.0);
                assertTrue(data.mSpeedData.getSpeedKmh() < 30.0);
                assertFalse(data.isActiveMoving()); // 速度は出ているがケイデンスは止まっている。つまり坂道や慣性移動である

                maxSpeed = Math.max(data.mSpeedData.getSpeedKmh(), maxSpeed);
            }
        }
        // 結果だけを出力
        AppLog.test("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), data.mSpeedData.getSpeedZone().name());

        // 最高速度が一致する
        assertEquals(data.mSpeedData.getMaxSpeedKmh(), maxSpeed, 0.1);
        // 自走していない。アクティブ0である。
        assertEquals(data.mSessionTime.getActiveTimeMs(), 0);
    }

    /**
     * AACRスタート～折り返し地点を適当な間隔で打刻する
     *
     * 採用している計算法では、2点間距離は約54kmである。
     * 1時間で移動した場合、走行速度は54km/hにならなければならない。
     */
    @Test
    public void GPS座標移動から距離と速度を測定する() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = new CentralDataManager(getContext(), clock);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        double maxSpeed = 0.0;

        {
            while (current < 1.0) {
                clock.offset((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));

                double lat = MathUtil.blendValue(SAMPLE_START_LATITUDE, SAMPLE_END_LATITUDE, 1.0 - current);
                double lng = MathUtil.blendValue(SAMPLE_START_LONGITUDE, SAMPLE_END_LONGITUDE, 1.0 - current);
                double alt = MathUtil.blendValue(SAMPLE_START_ALTITUDE, SAMPLE_END_ALTITUDE, 1.0 - current);

                final long OFFSET_TIME_MS = (long) ((double) (1000 * 60 * 60) * current);

                assertTrue(data.setLocation(lat, lng, alt, Math.random() * 100)); // 現在地点をオフセット
                data.onUpdate();
                assertFalse(data.isActiveMoving()); // ケイデンスが発生しないので、アクティブにはならないはずである
                assertNotNull(data.mSpeedData.getSpeedZone()); // ゾーンは必ず取得できる
                current += OFFSET_TIME_HOUR;

                assertObject(data, data.getLatestCentralData());

                // 速度をチェックする
                // 時速1kmの誤差を認める
                if (current > 0.1) {
                    assertNotEquals(data.mSpeedData.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない
                    assertNotEquals(Math.abs(data.mLocationData.getInclinationPercent()), 0.0, 0.1); // 標高1000mに向かって走るので、傾斜が存在しなければならない
                    assertTrue(data.mLocationData.getSumAltitude() > 0); // 獲得標高がなければならない
                    assertEquals(data.mSpeedData.getSpeedKmh(), SAMPLE_DISTANCE_KM, 1.0);

                    // S&Cセンサー由来のデータである
                    assertEquals(data.mSpeedData.getSource(), SpeedData.SpeedSource.GPS);
                }

                maxSpeed = Math.max(data.mSpeedData.getSpeedKmh(), maxSpeed);
            }
        }
        // 結果だけを出力
        AppLog.test("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), data.mSpeedData.getSpeedZone().name());

        // 約1時間経過していることを確認する
        assertEquals(data.mSessionTime.getSessionDurationMs(), 1000 * 60 * 60);

        // 最終的な移動距離をチェックする
        // 1時間分の動作分であるため、ほぼ一致するはずである
        assertEquals(data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), 1.0);

        // GPS走行なので、自走時間は0でなければならない
        assertEquals(data.mSessionTime.getActiveTimeMs(), 0);

        // 獲得標高が目的値とほぼ同等でなければならない
        // MEMO 標高は適当な回数だけ平均を取るので、完全一致はしなくて良い
        assertEquals(data.mLocationData.getSumAltitude(), (SAMPLE_END_ALTITUDE - SAMPLE_START_ALTITUDE), 25.0);

        // 最高速度が一致する
        assertEquals(data.mSpeedData.getMaxSpeedKmh(), maxSpeed, 0.1);
    }

    @Test
    public void GPS座標とケイデンスセンサーが与えられた場合ケイデンスが優先される() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = new CentralDataManager(getContext(), clock);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        double maxSpeed = 0.0;

        int crankRevolution = 0;

        {
            while (current < 1.0) {
                clock.offset((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));

                double lat = MathUtil.blendValue(SAMPLE_START_LATITUDE, SAMPLE_END_LATITUDE, 1.0 - current);
                double lng = MathUtil.blendValue(SAMPLE_START_LONGITUDE, SAMPLE_END_LONGITUDE, 1.0 - current);
                double alt = MathUtil.blendValue(SAMPLE_START_ALTITUDE, SAMPLE_END_ALTITUDE, 1.0 - current);

                final float SAMPLE_CRANK_RPM = (float) (90.0 + Math.random() * 10.0);
                final float gear = 2.05f; // 19T-39T
                assertEquals(data.setSpeedAndCadence(SAMPLE_CRANK_RPM, ++crankRevolution, SAMPLE_CRANK_RPM * gear, (int) (crankRevolution * gear)), 2);
                assertTrue(data.setLocation(lat, lng, alt, Math.random() * 100)); // 現在地点をオフセット
                data.onUpdate();
                assertTrue(data.isActiveMoving()); // ケイデンスから速度を得ているので、アクティブなはずである
                assertNotNull(data.mSpeedData.getSpeedZone()); // ゾーンは必ず取得できる
                current += OFFSET_TIME_HOUR;

                assertObject(data, data.getLatestCentralData());

                // 速度をチェックする
                // 時速1kmの誤差を認める
                if (current > 0.1) {
                    assertNotEquals(data.mSpeedData.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない
                    assertNotEquals(Math.abs(data.mLocationData.getInclinationPercent()), 0.0, 0.1); // 標高1000mに向かって走るので、傾斜が存在しなければならない
                    assertTrue(data.mLocationData.getSumAltitude() > 0); // 獲得標高がなければならない
                    // このギア比では速度は20～25km/h程度になるはずである
                    assertTrue(data.mSpeedData.getSpeedKmh() > 20.0);
                    assertTrue(data.mSpeedData.getSpeedKmh() < 30.0);
                    assertTrue(data.isActiveMoving());
                }

                // S&Cセンサー由来のデータである
                assertEquals(data.mSpeedData.getSource(), SpeedData.SpeedSource.Sensor);

                maxSpeed = Math.max(data.mSpeedData.getSpeedKmh(), maxSpeed);
            }
        }

        // 結果だけを出力
        AppLog.test("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), data.mSpeedData.getSpeedZone().name());

        // 最高速度はケイデンスの値である必要がある
        assertTrue(data.mSpeedData.getMaxSpeedKmh() > 20.0);
        assertTrue(data.mSpeedData.getMaxSpeedKmh() < 30.0);

        // 常にアクティブ移動である
        assertEquals(data.mSessionTime.getSessionDurationMs(), data.mSessionTime.getActiveTimeMs());
    }

    @Test
    public void ケイデンスセンサーの値がタイムアウトしたら自動的にGPS速度に切り替わる() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = new CentralDataManager(getContext(), clock);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;

        int crankRevolution = 0;

        {
            while (current < 1.0) {
                clock.offset((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));

                double lat = MathUtil.blendValue(SAMPLE_START_LATITUDE, SAMPLE_END_LATITUDE, 1.0 - current);
                double lng = MathUtil.blendValue(SAMPLE_START_LONGITUDE, SAMPLE_END_LONGITUDE, 1.0 - current);
                double alt = MathUtil.blendValue(SAMPLE_START_ALTITUDE, SAMPLE_END_ALTITUDE, 1.0 - current);

                final float SAMPLE_CRANK_RPM = (float) (90.0 + Math.random() * 10.0);
                final float gear = 2.05f; // 19T-39T

                if (current < 0.5) {
                    // 最初の30分はケイデンスセンサーの値を入力する
                    assertEquals(data.setSpeedAndCadence(SAMPLE_CRANK_RPM, ++crankRevolution, SAMPLE_CRANK_RPM * gear, (int) (crankRevolution * gear)), 2);
                }
                assertTrue(data.setLocation(lat, lng, alt, Math.random() * 100)); // 現在地点をオフセット
                data.onUpdate();
                assertNotNull(data.mSpeedData.getSpeedZone()); // ゾーンは必ず取得できる
                current += OFFSET_TIME_HOUR;

                assertObject(data, data.getLatestCentralData());

                if (current < 0.5) {
                    // 最初の30分はS&Cセンサー由来の速度
                    assertNotEquals(data.mSpeedData.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない
                    // S&Cセンサー由来のデータである
                    assertEquals(data.mSpeedData.getSource(), SpeedData.SpeedSource.Sensor);
                    assertTrue(data.isActiveMoving()); // ケイデンスから速度を得ているので、アクティブなはずである
                    // このギア比では速度は20～25km/h程度になるはずである
                    assertTrue(data.mSpeedData.getSpeedKmh() > 20.0);
                    assertTrue(data.mSpeedData.getSpeedKmh() < 30.0);
                    assertTrue(data.isActiveMoving());
                } else if (current > 0.6) {
                    // 後半30分はGPS由来の速度である
                    assertNotEquals(data.mSpeedData.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない
                    assertFalse(data.isActiveMoving()); // ケイデンスから値が得られていないため、非アクティブである
                    // S&Cセンサー由来のデータである
                    assertEquals(data.mSpeedData.getSource(), SpeedData.SpeedSource.GPS);

                    assertEquals(data.mSpeedData.getSpeedKmh(), SAMPLE_DISTANCE_KM, 1.0);
                }
            }
        }
        // 結果だけを出力
        AppLog.test("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), data.mSpeedData.getSpeedZone().name());

        // 最高速度はGPS由来である必要がある
        assertEquals(data.mSpeedData.getMaxSpeedKmh(), SAMPLE_DISTANCE_KM, 1.0);

        // アクティブ時間は全体の半分である
        assertEquals(((double) data.mSessionTime.getActiveTimeMs() / (double) data.mSessionTime.getSessionDurationMs()), 0.5, 0.05);
    }

    @Test
    public void 一時間の消費カロリーを計算する() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = new CentralDataManager(getContext(), clock);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;

        {
            while (current < 1.0) {
                clock.offset((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));

                // 心拍140前後をキープさせる
                data.setHeartrate((int) (130.0 + 10.0 * Math.random()));
                data.onUpdate();
                current += OFFSET_TIME_HOUR;

                assertObject(data, data.getLatestCentralData());

                assertFalse(data.isActiveMoving()); // ケイデンスが発生しないので、アクティブにはならないはずである
                assertNotNull(data.mFitnessData.getZone()); // ゾーンは必ず取得できる
                assertNotEquals(data.mFitnessData.getZone(), HeartrateZone.Repose);
            }
        }

        // 約1時間経過していることを確認する
        assertEquals(data.mSessionTime.getSessionDurationMs(), 1000 * 60 * 60);


        // 消費カロリー的には、300～400の間が妥当である
        // 獲得エクササイズは3.5～4.5程度が妥当な値となる
        AppLog.test("Fitness %.1f kcal / %.1f Ex", data.mFitnessData.getSumCalories(), data.mFitnessData.getSumExercise());
        assertTrue(data.mFitnessData.getSumCalories() > 280);
        assertTrue(data.mFitnessData.getSumCalories() < 400);
        assertTrue(data.mFitnessData.getSumExercise() > 3.0);
        assertTrue(data.mFitnessData.getSumExercise() < 5.0);
    }

    @Test
    public void AACR2015のデータを挿入する() throws Exception {
        InputStream is = new FileInputStream(new File("../sdk/src/test/assets/gpx/sample-aacr2015.gpx").getAbsoluteFile());
        Gpx gpx;
        try {
            GpxParser parser = new GpxParser();
            parser.setDateOption(GpxParser.DateOption.AddTimeZone);
            gpx = parser.parse(is);

        } finally {
            IOUtil.close(is);
        }

        assertNotNull(gpx);

        // GPXからデータをエミュレートする
        int segmentIndex = 0;
        for (GpxSegment segment : gpx.getTrackSegments()) {
            Clock clock = new Clock(segment.getFirstPoint().getTime().getTime());
            ClockTimer clockTimer = new ClockTimer(clock);
            CentralDataManager centralDataManager = new CentralDataManager(getContext(), clock);

            if (segmentIndex > 0) {
                assertEquals(centralDataManager.mSessionLogger.getTotalData().getSessionNum(), segmentIndex);
            } else {
                assertNull(centralDataManager.mSessionLogger.getTotalData());
            }

            int pointIndex = 0;
            for (GpxPoint pt : segment.getPoints()) {
                clock.set(pt.getTime().getTime());
//                LogUtil.log("insert :: " + pt.getTime().toString());
//                if (DateUtil.getHour(new Date(clock.now()), TimeZone.getDefault()) > 6) {
//                    LogUtil.log("break");
//                }

                RawGeoPoint location = pt.getLocation();
                centralDataManager.setLocation(location.latitude, location.longitude, location.altitude, 10);

                if (!centralDataManager.onUpdate()) {
                    AppLog.test("update abort.");
                    continue;
                }

                if (clockTimer.overTimeMs(1000 * 30)) {
                    centralDataManager.commit();
                    assertFalse(centralDataManager.mSessionLogger.hasPointCaches());
                    clockTimer.start();
                }

                // データを検証する
                assertObject(centralDataManager, centralDataManager.getLatestCentralData());

                // GPS走行をしていることを確認
                if (pointIndex > 10 && centralDataManager.getLatestCentralData().sensor.speed != null) {
                    assertNotEquals(
                            centralDataManager.getLatestCentralData().sensor.speed.flags & RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_GPS,
                            0x00
                    );
                }

                ++pointIndex;
            }

            // 最後のデータを書き込む
            centralDataManager.commit();
            assertFalse(centralDataManager.mSessionLogger.hasPointCaches());

            assertObject(centralDataManager, centralDataManager.getLatestCentralData());

            // Totalをチェックする
            {
                Date startTime = gpx.getFirstSegment().getFirstPoint().getTime();
                Date endTime = gpx.getLastSegment().getLastPoint().getTime();
                SessionLogDatabase sessionDb = new SessionLogDatabase(getContext());
                try {
                    sessionDb.openReadOnly();
                    SessionTotal total = sessionDb.loadTotal(startTime.getTime(), endTime.getTime());

                    AppLog.test("Distance %.1f km", total.getSumDistanceKm());
                    AppLog.test("MaxSpeed %.1f km/h", total.getMaxSpeedKmh());

                    RawCentralData data = centralDataManager.getLatestCentralData();
                    // ログを比較する
                    assertEquals(data.today.distanceKm, total.getSumDistanceKm(), 0.1);
                } finally {
                    sessionDb.close();
                }
            }

            ++segmentIndex;
        }

        // Totalをチェックする
        {
            Date startTime = gpx.getFirstSegment().getFirstPoint().getTime();
            Date endTime = gpx.getLastSegment().getLastPoint().getTime();
            SessionLogDatabase sessionDb = new SessionLogDatabase(getContext());
            try {
                sessionDb.openReadOnly();
                SessionTotal total = sessionDb.loadTotal(startTime.getTime(), endTime.getTime());

                AppLog.test("Distance %.1f km", total.getSumDistanceKm());
                AppLog.test("MaxSpeed %.1f km/h", total.getMaxSpeedKmh());

            } finally {
                sessionDb.close();
            }
        }
    }
}
