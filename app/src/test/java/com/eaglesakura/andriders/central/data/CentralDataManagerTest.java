package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.central.data.sensor.SpeedData;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.sensor.HeartrateZone;
import com.eaglesakura.andriders.sensor.SpeedZone;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.json.JSON;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.MathUtil;
import com.eaglesakura.util.SerializeUtil;
import com.eaglesakura.util.Timer;

import org.junit.Test;

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

    CentralDataManager newCentralDataManager(Clock clock) {
        SessionInfo info = new SessionInfo.Builder(getContext(), clock)
                .build();
        return new CentralDataManager(info, null, null);
    }

    /**
     * セッション開始時刻が正常であることを検証する
     */
    @Test
    public void セッション開始時刻が正確であることを確認する() throws Throwable {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = newCentralDataManager(clock);

        assertNotNull(data.getSessionInfo());
        assertEquals(data.getSessionId(), START_TIME);
        validate(data.getSessionInfo().getUserProfiles().getUserWeight()).eq(USER_WEIGHT);

        // 時間を分割して1分経過させる
        for (int i = 0; i < 60; ++i) {
            clock.offset(1000);
            data.onUpdate();
        }
        assertEquals(data.getLatestCentralData().session.durationTimeMs, 1000 * 60);     // データも1分経過している
    }

    public static void assertCentralData(CentralDataManager data, RawCentralData centralData) throws Throwable {
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
        assertNotNull(centralData.sensor);

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
            assertEquals(data.mSpeedData.getSpeedKmh(), centralData.sensor.speed.speedKmh, 0.1);

            // 速度チェック
            validate(centralData.record.maxSpeedKmh)                // 最高速度は
                    .from(centralData.record.maxSpeedKmhToday)      // 今日の最高速度以上
                    .from(centralData.record.maxSpeedKmhSession)    // セッションの最高速度以上
                    .from(centralData.sensor.speed.speedKmh); // 現在速度以上

            validate(centralData.record.maxSpeedKmhToday)           // 今日の最高速度は
                    .from(centralData.record.maxSpeedKmhSession)    // セッションの最高速度以上
                    .from(centralData.sensor.speed.speedKmh); // 現在速度以上

            validate(centralData.record.maxSpeedKmhSession)         // セッション最高速度は
                    .from(centralData.sensor.speed.speedKmh); // 現在速度以上
        } else {
            assertNull(centralData.sensor.speed);
        }

        // シリアライズとデシリアライズが正常である
        {
            byte[] bytes = SerializeUtil.serializePublicFieldObject(centralData, true);
            assertFalse(CollectionUtil.isEmpty(bytes));

            RawCentralData deserialized = SerializeUtil.deserializePublicFieldObject(RawCentralData.class, bytes);
            assertEquals(centralData, deserialized);
        }
        {
            String json = JSON.encode(centralData);
            RawCentralData deserialized = JSON.decode(json, RawCentralData.class);
            assertEquals(centralData, deserialized);
        }
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
    public void ケイデンスセンサーによる速度を測定する() throws Throwable {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = newCentralDataManager(clock);

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


                assertCentralData(data, data.getLatestCentralData());

                assertTrue(data.isActiveMoving()); // ケイデンスから速度を得ているので、アクティブなはずである

                // 速度をチェックする
                assertNotEquals(data.mSpeedData.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない

                // このギア比では速度は20～25km/h程度になるはずである
                validate(data.mSpeedData.getSpeedKmh()).from(20.0);
                validate(data.mSpeedData.getSpeedKmh()).to(30.0);
                assertTrue(data.isActiveMoving());

                maxSpeed = Math.max(data.mSpeedData.getSpeedKmh(), maxSpeed);
            }
        }

        // 結果だけを出力
        AppLog.test("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), data.mSpeedData.getSpeedZone().name());

        // 最高速度が一致する
        validate(data.mSpeedData.getMaxSpeedKmh()).eq(maxSpeed);
        // 常に自走であるので、セッション時間と自走時間は一致する
        assertEquals(data.mSessionTime.getSessionDurationMs(), data.mSessionTime.getActiveTimeMs());
    }

    @Test
    public void ケイデンス停止で速度が得られている場合はアクティブとして扱わない() throws Throwable {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = newCentralDataManager(clock);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        double maxSpeed = 0.0;
        int crankRevolution = 0;
        {
            while (current < 1.0) {
                clock.offset((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));
//                final long OFFSET_TIME_MS = (long) ((double) (1000 * 60 * 60) * current);

                final float SAMPLE_CRANK_RPM = (float) (90.0 + Math.random() * 10.0);
                final float gear = 2.05f; // 19T-39T
                assertEquals(data.setSpeedAndCadence(0, 0, SAMPLE_CRANK_RPM * gear, (int) (crankRevolution * gear)), 2);
                data.onUpdate();
                current += OFFSET_TIME_HOUR;

                assertCentralData(data, data.getLatestCentralData());

                // 速度をチェックする
                assertNotEquals(data.mSpeedData.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない
                // このギア比では速度は20～25km/h程度になるはずである
                validate(data.mSpeedData.getSpeedKmh()).from(20.0);
                validate(data.mSpeedData.getSpeedKmh()).to(30.0);
                assertFalse(data.isActiveMoving()); // 速度は出ているがケイデンスは止まっている。つまり坂道や慣性移動である

                maxSpeed = Math.max(data.mSpeedData.getSpeedKmh(), maxSpeed);
            }
        }
        // 結果だけを出力
        AppLog.test("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), data.mSpeedData.getSpeedZone().name());

        // 最高速度が一致する
        validate(data.mSpeedData.getMaxSpeedKmh()).eq(maxSpeed);
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
    public void GPS座標移動から距離と速度を測定する() throws Throwable {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = newCentralDataManager(clock);

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

                assertTrue(data.setLocation(lat, lng, alt, Math.random() * 25)); // 現在地点をオフセット
                assertFalse(data.setLocation(lat, lng, alt, 51));  // 信頼性の低い座標は弾かれる
                data.onUpdate();
                assertFalse(data.isActiveMoving()); // ケイデンスが発生しないので、アクティブにはならないはずである
                assertNotNull(data.mSpeedData.getSpeedZone()); // ゾーンは必ず取得できる
                current += OFFSET_TIME_HOUR;

                assertCentralData(data, data.getLatestCentralData());

                // 速度をチェックする
                // 時速1kmの誤差を認める
                if (current > 0.1) {
                    assertNotEquals(data.mSpeedData.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない
                    validate(Math.abs(data.mLocationData.getInclinationPercent())).notEq(0.0);  // 標高1000mに向かって走るので、傾斜が存在しなければならない
                    validate(data.mLocationData.getSumAltitude()).from(0.0).notEq(0.0); // 獲得標高がなければならない
                    validate(data.mSpeedData.getSpeedKmh()).delta(1.0).eq(SAMPLE_DISTANCE_KM);

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
        validate(data.mDistanceData.getDistanceKm()).delta(1.0).eq(data.mSpeedData.getSpeedKmh());

        // GPS走行なので、自走時間は0でなければならない
        assertEquals(data.mSessionTime.getActiveTimeMs(), 0);

        // 獲得標高が目的値とほぼ同等でなければならない
        // MEMO 標高は適当な回数だけ平均を取るので、完全一致はしなくて良い
        validate(data.mLocationData.getSumAltitude()).delta(25.0).eq((SAMPLE_END_ALTITUDE - SAMPLE_START_ALTITUDE));

        // 最高速度が一致する
        validate(data.mSpeedData.getMaxSpeedKmh()).eq(maxSpeed);
    }

    @Test
    public void GPS座標とケイデンスセンサーが与えられた場合ケイデンスが優先される() throws Throwable {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = newCentralDataManager(clock);

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
                assertTrue(data.setLocation(lat, lng, alt, Math.random() * 25)); // 現在地点をオフセット
                assertFalse(data.setLocation(lat, lng, alt, 51));  // 信頼性の低い座標は弾かれる
                data.onUpdate();
                assertTrue(data.isActiveMoving()); // ケイデンスから速度を得ているので、アクティブなはずである
                assertNotNull(data.mSpeedData.getSpeedZone()); // ゾーンは必ず取得できる
                current += OFFSET_TIME_HOUR;

                assertCentralData(data, data.getLatestCentralData());

                // 速度をチェックする
                // 時速1kmの誤差を認める
                if (current > 0.1) {
                    assertNotEquals(data.mSpeedData.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない
                    assertNotEquals(Math.abs(data.mLocationData.getInclinationPercent()), 0.0, 0.1); // 標高1000mに向かって走るので、傾斜が存在しなければならない
                    assertTrue(data.mLocationData.getSumAltitude() > 0); // 獲得標高がなければならない
                    // このギア比では速度は20～25km/h程度になるはずである
                    validate(data.mSpeedData.getSpeedKmh()).from(20.0).to(30.0);
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
        validate(data.mSpeedData.getMaxSpeedKmh()).from(20.0).to(30.0);
        // 常にアクティブ移動である
        assertEquals(data.mSessionTime.getSessionDurationMs(), data.mSessionTime.getActiveTimeMs());
    }

    @Test
    public void ケイデンスセンサーの値がタイムアウトしたら自動的にGPS速度に切り替わる() throws Throwable {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = newCentralDataManager(clock);

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
                assertTrue(data.setLocation(lat, lng, alt, Math.random() * 25)); // 現在地点をオフセット
                assertFalse(data.setLocation(lat, lng, alt, 51));  // 信頼性の低い座標は弾かれる
                data.onUpdate();
                assertNotNull(data.mSpeedData.getSpeedZone()); // ゾーンは必ず取得できる
                current += OFFSET_TIME_HOUR;

                assertCentralData(data, data.getLatestCentralData());

                if (current < 0.5) {
                    // 最初の30分はS&Cセンサー由来の速度
                    assertNotEquals(data.mSpeedData.getSpeedZone(), SpeedZone.Stop); // スピードは停止にはならない
                    // S&Cセンサー由来のデータである
                    assertEquals(data.mSpeedData.getSource(), SpeedData.SpeedSource.Sensor);
                    assertTrue(data.isActiveMoving()); // ケイデンスから速度を得ているので、アクティブなはずである
                    // このギア比では速度は20～25km/h程度になるはずである
                    validate(data.mSpeedData.getSpeedKmh()).from(20.0).to(30.0);
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
        validate(data.mSpeedData.getMaxSpeedKmh()).delta(1.0).eq(SAMPLE_DISTANCE_KM);

        // アクティブ時間は全体の半分である
        validate(((double) data.mSessionTime.getActiveTimeMs() / (double) data.mSessionTime.getSessionDurationMs())).delta(0.01).eq(0.5);
    }

    @Test
    public void 一時間の消費カロリーを計算する() throws Throwable {
        final long START_TIME = System.currentTimeMillis();
        Clock clock = new Clock(START_TIME);
        CentralDataManager data = newCentralDataManager(clock);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;

        {
            while (current < 1.0) {
                clock.offset((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));

                // 心拍140前後をキープさせる
                data.setHeartrate((int) (130.0 + 10.0 * Math.random()));
                data.onUpdate();
                current += OFFSET_TIME_HOUR;

                assertCentralData(data, data.getLatestCentralData());

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
        validate(data.mFitnessData.getSumCalories()).from(280.0).to(400.0);
        validate(data.mFitnessData.getSumExercise()).from(3.0).to(5.0);
    }

}
