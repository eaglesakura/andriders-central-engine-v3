package com.eaglesakura.andriders.computer.central.data;

import com.eaglesakura.andriders.computer.central.data.geo.GeoSpeedData;
import com.eaglesakura.andriders.computer.central.data.geo.LocationData;
import com.eaglesakura.andriders.computer.central.data.hrsensor.FitnessData;
import com.eaglesakura.andriders.computer.central.data.scsensor.CadenceData;
import com.eaglesakura.andriders.computer.central.data.scsensor.SensorSpeedData;
import com.eaglesakura.andriders.computer.central.data.session.SessionData;

import android.content.Context;

/**
 * サイコンが持つべき情報を統括する
 */
public class CycleComputerData {
    /**
     * app context
     */
    private final Context mContext;

    /**
     * カロリー等のフィットネス情報管理
     *
     * 心拍から計算される
     */
    private final FitnessData mFitnessData;

    /**
     * 速度情報管理
     *
     * S&Cセンサーが有効であればセンサーを、無効であれば位置情報をソースにして速度を取得する。
     * 位置情報が無効である場合は速度0となる。
     */
    private final SpeedData mSpeedData;

    /**
     * センサー由来の速度情報
     *
     * S&Cセンサーの取得時に更新される
     */
    private final SensorSpeedData mSensorSpeedData;

    /**
     * ケイデンス情報
     *
     * S&Cセンサーの取得時に更新される
     */
    private final CadenceData mCadenceData;

    /**
     * 移動距離管理
     *
     * 現在速度と時間経過から自動的に計算される
     */
    private final DistanceData mDistanceData;

    /**
     * 位置情報管理
     */
    private final LocationData mLocationData;

    /**
     * セッション情報管理
     */
    private final SessionData mSessionData;

    /**
     * 時刻設定
     */
    private final CycleClock mClock = new CycleClock(System.currentTimeMillis());

    /**
     * sync管理
     *
     * データは別なパイプラインから呼び出されるため、ロックを行ってデータがコンフリクトしないようにする。
     */
    private final Object lock = new Object();

    public CycleComputerData(Context context, long sessionStartTime) {
        mContext = context.getApplicationContext();
        mSessionData = new SessionData(mClock, sessionStartTime);
        mFitnessData = new FitnessData(mClock);
        mSensorSpeedData = new SensorSpeedData(mClock);
        mCadenceData = new CadenceData(mClock);
        mDistanceData = new DistanceData(mClock);


        GeoSpeedData geoSpeedData = new GeoSpeedData(mClock); // 位置センサー由来の速度計

        mLocationData = new LocationData(mClock, geoSpeedData);
        mSpeedData = new SpeedData(mClock, geoSpeedData, mSensorSpeedData);
    }

    /**
     * 心拍を設定する
     */
    public void setHeartrate(long timestamp, int bpm) {
        synchronized (lock) {
            mFitnessData.setHeartrate(timestamp, bpm);
        }
    }


    /**
     * 位置情報を設定する
     *
     * @param timestamp     センサー時刻
     * @param lat           緯度
     * @param lng           経度
     * @param alt           高度(クラス内部で適度に補正される)
     * @param accuracyMeter メートル単位の精度
     */
    public boolean setLocation(long timestamp, double lat, double lng, double alt, double accuracyMeter) {
        synchronized (lock) {
            return mLocationData.setLocation(timestamp, lat, lng, alt, accuracyMeter);
        }
    }

    /**
     * S&Cセンサーの情報を設定する
     *
     * @param timestamp       センサー時刻
     * @param crankRpm        クランク回転数 / min
     * @param crankRevolution クランク合計回転数
     * @param wheelRpm        ホイール回転数 / min
     * @param wheelRevolution ホイール合計回転数
     * @return 更新した情報数
     */
    public int setSpeedAndCadence(long timestamp, float crankRpm, int crankRevolution, float wheelRpm, int wheelRevolution) {
        synchronized (lock) {
            int result = 0;
            if (mSensorSpeedData.setSensorSpeed(timestamp, wheelRpm, wheelRevolution)) {
                ++result;
            }

            if (mCadenceData.setCadence(timestamp, crankRpm, crankRevolution)) {
                ++result;
            }

            return result;
        }
    }

    /**
     * 時間を更新する
     */
    public void onUpdateTime(long diffTimeMs) {
        synchronized (lock) {
            // 消費カロリーを更新する
            mFitnessData.onUpdateTime(diffTimeMs);

            // 走行距離を更新する
            mDistanceData.onUpdate(diffTimeMs, mSpeedData.getSpeedKmh());
        }
    }

    /**
     * サイコンの現在時刻を指定する
     */
    public void setTime(long time) {
        synchronized (lock) {
            mClock.setCurrentTime(time);
        }
    }

    /**
     * サイコンの現在時刻を現実時刻に同期する
     */
    public void syncTime() {
        setTime(System.currentTimeMillis());
    }
}
