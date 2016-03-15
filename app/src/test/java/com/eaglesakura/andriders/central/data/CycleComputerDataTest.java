package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.AceJUnitTester;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.internal.protocol.RawSensorData;
import com.eaglesakura.andriders.sensor.HeartrateZone;
import com.eaglesakura.andriders.sensor.SpeedZone;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.MathUtil;
import com.eaglesakura.util.SerializeUtil;
import com.eaglesakura.util.Timer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CycleComputerDataTest extends AceJUnitTester {
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
     * ユーザー体重
     */
    final double USER_WEIGHT = 65;

    @Override
    public void onSetup() {
        super.onSetup();

        // 計算を確定させるため、フィットネスデータを構築する
        // 計算しやすくするため、データはキリの良い数にしておく
        Settings settings = Settings.getInstance();
        settings.getUserProfiles().setUserWeight(USER_WEIGHT);
        settings.getUserProfiles().setNormalHeartrate(90);
        settings.getUserProfiles().setMaxHeartrate(190);
        settings.getUserProfiles().setWheelOuterLength(2096); // 700 x 23c
    }

    /**
     * セッション開始時刻が正常であることを検証する
     */
    @Test
    public void セッション開始時刻が正確であることを確認する() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        CycleComputerData data = new CycleComputerData(mContext, START_TIME);

        assertNotNull(data.getSessionId());
        assertNotEquals(data.getSessionId(), "");
        assertEquals(data.mSessionData.getStartDate(), START_TIME);
        assertEquals(data.mFitnessData.getUserWeight(), USER_WEIGHT, 0.1);

        // 時間を分割して1分経過させる
        for (int i = 0; i < 60; ++i) {
            data.onUpdateTime(1000);
        }
        assertEquals(data.mSessionData.getSessionDulationMs(), 1000 * 60); // データも1分経過している
    }

    void assertObject(CycleComputerData data, RawCentralData centralData) throws Exception {
        assertNotNull(data);
        assertNotNull(data.mSpeedData.getSpeedZone());
        assertNotNull(data.mFitnessData.getZone());
        assertNotNull(data.mCadenceData.getZone());

        assertNotNull(centralData);
        assertNotNull(centralData.session);
        assertNotNull(centralData.centralStatus);
        assertNotNull(centralData.specs);

        assertNotNull(centralData.session.fitness);
        assertEquals(data.isActiveMoving(), centralData.session.isActiveMoving());
        assertEquals(centralData.centralStatus.date, data.now());

        if (data.mFitnessData.valid()) {
            assertNotNull(centralData.sensor.heartrate);
            assertThat("HR : " + data.mFitnessData.getHeartrate(),
                    data.mFitnessData.getHeartrate() > 50 && data.mFitnessData.getHeartrate() < 220,
                    isTrue());

            assertEquals((short) data.mFitnessData.getHeartrate(), centralData.sensor.heartrate.bpm);
            assertEquals(data.mFitnessData.getHeartrateDataTime(), centralData.sensor.heartrate.date);
            assertEquals(data.mFitnessData.getZone(), centralData.sensor.heartrate.zone);
        } else {
            assertNull(centralData.sensor.heartrate);
        }

        if (data.mCadenceData.valid()) {
            assertThat("cadence : " + data.mCadenceData.getCadenceRpm(),
                    data.mCadenceData.getCadenceRpm() >= 0 && data.mCadenceData.getCadenceRpm() <= 200,
                    isTrue());

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
            assertThat("latitude : " + data.mLocationData.getLatitude(),
                    Math.abs(data.mLocationData.getLatitude()) <= 90, // 北緯は90度までしか存在しない
                    isTrue());
            assertThat("longitude : " + data.mLocationData.getLongitude(),
                    Math.abs(data.mLocationData.getLongitude()) <= 180, // 東経180度までしか存在しない
                    isTrue());
            assertThat("altitude : " + data.mLocationData.getAltitudeMeter(),
                    Math.abs(data.mLocationData.getAltitudeMeter()) <= 8000, // エベレストよりも高い場所には登れない
                    isTrue());


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
        CycleComputerData data = new CycleComputerData(mContext, START_TIME);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        double maxSpeed = 0.0;
        LogUtil.setOutput(false);

        int crankRevolution = 0;
        {
            while (current < 1.0) {
                final float SAMPLE_CRANK_RPM = (float) (90.0 + Math.random() * 10.0);
                final float gear = 2.05f; // 19T-39T
                assertEquals(data.setSpeedAndCadence(SAMPLE_CRANK_RPM, ++crankRevolution, SAMPLE_CRANK_RPM * gear, (int) (crankRevolution * gear)), 2);
                data.onUpdateTime((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));
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
        LogUtil.setOutput(true);

        // 結果だけを出力
        LogUtil.log("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), data.mSpeedData.getSpeedZone().name());

        // 最高速度が一致する
        assertEquals(data.mSpeedData.getMaxSpeedKmh(), maxSpeed, 0.1);
        // 常に自走であるので、セッション時間と自走時間は一致する
        assertEquals(data.mSessionData.getSessionDulationMs(), data.mSessionData.getActiveTimeMs());
    }

    @Test
    public void ケイデンス停止で速度が得られている場合はアクティブとして扱わない() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        CycleComputerData data = new CycleComputerData(mContext, START_TIME);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        double maxSpeed = 0.0;
        LogUtil.setOutput(false);

        int crankRevolution = 0;
        {
            while (current < 1.0) {
                final long OFFSET_TIME_MS = (long) ((double) (1000 * 60 * 60) * current);

                final float SAMPLE_CRANK_RPM = (float) (90.0 + Math.random() * 10.0);
                final float gear = 2.05f; // 19T-39T
                assertEquals(data.setSpeedAndCadence(0, 0, SAMPLE_CRANK_RPM * gear, (int) (crankRevolution * gear)), 2);
                data.onUpdateTime((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));
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
        LogUtil.setOutput(true);

        // 結果だけを出力
        LogUtil.log("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), data.mSpeedData.getSpeedZone().name());

        // 最高速度が一致する
        assertEquals(data.mSpeedData.getMaxSpeedKmh(), maxSpeed, 0.1);
        // 自走していない。アクティブ0である。
        assertEquals(data.mSessionData.getActiveTimeMs(), 0);
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
        CycleComputerData data = new CycleComputerData(mContext, START_TIME);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        double maxSpeed = 0.0;
        LogUtil.setOutput(false);
        {
            while (current < 1.0) {
                double lat = MathUtil.blendValue(SAMPLE_START_LATITUDE, SAMPLE_END_LATITUDE, 1.0 - current);
                double lng = MathUtil.blendValue(SAMPLE_START_LONGITUDE, SAMPLE_END_LONGITUDE, 1.0 - current);
                double alt = MathUtil.blendValue(SAMPLE_START_ALTITUDE, SAMPLE_END_ALTITUDE, 1.0 - current);

                final long OFFSET_TIME_MS = (long) ((double) (1000 * 60 * 60) * current);

                assertTrue(data.setLocation(lat, lng, alt, Math.random() * 100)); // 現在地点をオフセット
                data.onUpdateTime((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));
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
        LogUtil.setOutput(true);

        // 結果だけを出力
        LogUtil.log("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), data.mSpeedData.getSpeedZone().name());

        // 約1時間経過していることを確認する
        assertEquals(data.mSessionData.getSessionDulationMs(), 1000 * 60 * 60);

        // 最終的な移動距離をチェックする
        // 1時間分の動作分であるため、ほぼ一致するはずである
        assertEquals(data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), 1.0);

        // GPS走行なので、自走時間は0でなければならない
        assertEquals(data.mSessionData.getActiveTimeMs(), 0);

        // 獲得標高が目的値とほぼ同等でなければならない
        // MEMO 標高は適当な回数だけ平均を取るので、完全一致はしなくて良い
        assertEquals(data.mLocationData.getSumAltitude(), (SAMPLE_END_ALTITUDE - SAMPLE_START_ALTITUDE), 25.0);

        // 最高速度が一致する
        assertEquals(data.mSpeedData.getMaxSpeedKmh(), maxSpeed, 0.1);
    }

    @Test
    public void GPS座標とケイデンスセンサーが与えられた場合ケイデンスが優先される() throws Exception {

        final long START_TIME = System.currentTimeMillis();
        CycleComputerData data = new CycleComputerData(mContext, START_TIME);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        double maxSpeed = 0.0;

        int crankRevolution = 0;
        LogUtil.setOutput(false);
        {
            while (current < 1.0) {
                double lat = MathUtil.blendValue(SAMPLE_START_LATITUDE, SAMPLE_END_LATITUDE, 1.0 - current);
                double lng = MathUtil.blendValue(SAMPLE_START_LONGITUDE, SAMPLE_END_LONGITUDE, 1.0 - current);
                double alt = MathUtil.blendValue(SAMPLE_START_ALTITUDE, SAMPLE_END_ALTITUDE, 1.0 - current);

                final float SAMPLE_CRANK_RPM = (float) (90.0 + Math.random() * 10.0);
                final float gear = 2.05f; // 19T-39T
                assertEquals(data.setSpeedAndCadence(SAMPLE_CRANK_RPM, ++crankRevolution, SAMPLE_CRANK_RPM * gear, (int) (crankRevolution * gear)), 2);
                assertTrue(data.setLocation(lat, lng, alt, Math.random() * 100)); // 現在地点をオフセット
                data.onUpdateTime((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));
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
        LogUtil.setOutput(true);

        // 結果だけを出力
        LogUtil.log("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), data.mSpeedData.getSpeedZone().name());

        // 最高速度はケイデンスの値である必要がある
        assertTrue(data.mSpeedData.getMaxSpeedKmh() > 20.0);
        assertTrue(data.mSpeedData.getMaxSpeedKmh() < 30.0);

        // 常にアクティブ移動である
        assertEquals(data.mSessionData.getSessionDulationMs(), data.mSessionData.getActiveTimeMs());
    }

    @Test
    public void ケイデンスセンサーの値がタイムアウトしたら自動的にGPS速度に切り替わる() throws Exception {

        final long START_TIME = System.currentTimeMillis();
        CycleComputerData data = new CycleComputerData(mContext, START_TIME);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;

        int crankRevolution = 0;
        LogUtil.setOutput(false);
        {
            while (current < 1.0) {
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
                data.onUpdateTime((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));
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
        LogUtil.setOutput(true);

        // 結果だけを出力
        LogUtil.log("1Hour dist(%.3f km) speed(%.1f km/h : %s)", data.mDistanceData.getDistanceKm(), data.mSpeedData.getSpeedKmh(), data.mSpeedData.getSpeedZone().name());

        // 最高速度はGPS由来である必要がある
        assertEquals(data.mSpeedData.getMaxSpeedKmh(), SAMPLE_DISTANCE_KM, 1.0);

        // アクティブ時間は全体の半分である
        assertEquals(((double) data.mSessionData.getActiveTimeMs() / (double) data.mSessionData.getSessionDulationMs()), 0.5, 0.05);
    }

    @Test
    public void 一時間の消費カロリーを計算する() throws Exception {
        final long START_TIME = System.currentTimeMillis();
        CycleComputerData data = new CycleComputerData(mContext, START_TIME);

        final double OFFSET_TIME_HOUR = (1.0 / 60.0 / 60.0); // 適当な間隔でGPSが到達したと仮定する
        double current = 0.0;
        LogUtil.setOutput(false);
        {
            while (current < 1.0) {
                // 心拍140前後をキープさせる
                data.setHeartrate((int) (130.0 + 10.0 * Math.random()));
                data.onUpdateTime((long) (OFFSET_TIME_HOUR * Timer.toMilliSec(0, 1, 0, 0, 0)));
                current += OFFSET_TIME_HOUR;

                assertObject(data, data.getLatestCentralData());

                assertFalse(data.isActiveMoving()); // ケイデンスが発生しないので、アクティブにはならないはずである
                assertNotNull(data.mFitnessData.getZone()); // ゾーンは必ず取得できる
                assertNotEquals(data.mFitnessData.getZone(), HeartrateZone.Repose);
            }
        }
        LogUtil.setOutput(true);

        // 約1時間経過していることを確認する
        assertEquals(data.mSessionData.getSessionDulationMs(), 1000 * 60 * 60);


        // 消費カロリー的には、300～400の間が妥当である
        // 獲得エクササイズは3.5～4.5程度が妥当な値となる
        LogUtil.log("Fitness %.1f kcal / %.1f Ex", data.mFitnessData.getSumCalories(), data.mFitnessData.getSumExercise());
        assertTrue(data.mFitnessData.getSumCalories() > 280);
        assertTrue(data.mFitnessData.getSumCalories() < 400);
        assertTrue(data.mFitnessData.getSumExercise() > 3.0);
        assertTrue(data.mFitnessData.getSumExercise() < 5.0);
    }
}
