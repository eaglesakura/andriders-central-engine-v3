package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.central.data.geo.GeoSpeedData;
import com.eaglesakura.andriders.central.data.geo.LocationData;
import com.eaglesakura.andriders.central.data.hrsensor.FitnessData;
import com.eaglesakura.andriders.central.data.scsensor.CadenceData;
import com.eaglesakura.andriders.central.data.scsensor.SensorSpeedData;
import com.eaglesakura.andriders.central.data.session.SessionData;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.internal.protocol.RawSensorData;
import com.eaglesakura.andriders.internal.protocol.RawSessionData;
import com.eaglesakura.andriders.internal.protocol.RawSpecs;
import com.eaglesakura.andriders.sensor.SpeedZone;

import android.content.Context;

/**
 * サイコンが持つべき情報を統括する
 *
 * 各種Data系クラスへのアクセサはUnitTestのためpackage privateとして扱う
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
    final FitnessData mFitnessData;

    /**
     * 速度情報管理
     *
     * S&Cセンサーが有効であればセンサーを、無効であれば位置情報をソースにして速度を取得する。
     * 位置情報が無効である場合は速度0となる。
     */
    final SpeedData mSpeedData;

    /**
     * センサー由来の速度情報
     *
     * S&Cセンサーの取得時に更新される
     */
    final SensorSpeedData mSensorSpeedData;

    /**
     * ケイデンス情報
     *
     * S&Cセンサーの取得時に更新される
     */
    final CadenceData mCadenceData;

    /**
     * 移動距離管理
     *
     * 現在速度と時間経過から自動的に計算される
     */
    final DistanceData mDistanceData;

    /**
     * 位置情報管理
     */
    final LocationData mLocationData;

    /**
     * セッション情報管理
     */
    final SessionData mSessionData;

    /**
     * 時刻設定
     */
    private final Clock mClock;

    /**
     * sync管理
     *
     * データは別なパイプラインから呼び出されるため、ロックを行ってデータがコンフリクトしないようにする。
     */
    private final Object lock = new Object();

    public CycleComputerData(Context context, long sessionStartTime) {
        mContext = context.getApplicationContext();
        mClock = new Clock(sessionStartTime);
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
     * サイコンの時刻を取得する
     */
    public long now() {
        return mClock.now();
    }

    /**
     * 自走中であればtrue
     */
    public boolean isActiveMoving() {
        synchronized (lock) {
            if (mSpeedData.getSpeedZone() == SpeedZone.Stop) {
                // 停止中なら自走ではない
                return false;
            }

            if (mCadenceData.getCadenceRpm() <= 5) {
                // 脚が止まっているなら自走ではない
                return false;
            }

            return true;
        }
    }

    /**
     * セッション識別子を取得する
     *
     * MEMO: 識別子は普遍なため、sync不要
     */
    public String getSessionId() {
        return mSessionData.getSessionId();
    }

    /**
     * 心拍を設定する
     */
    public void setHeartrate(int bpm) {
        synchronized (lock) {
            mFitnessData.setHeartrate(bpm);
        }
    }

    /**
     * 位置情報を設定する
     *
     * MEMO : 位置情報はLocationDataが一括して管理する。速度計はLocData.GeoSpeed経由で検証する
     *
     * @param lat           緯度
     * @param lng           経度
     * @param alt           高度(クラス内部で適度に補正される)
     * @param accuracyMeter メートル単位の精度
     */
    public boolean setLocation(double lat, double lng, double alt, double accuracyMeter) {
        synchronized (lock) {
            return mLocationData.setLocation(lat, lng, alt, accuracyMeter);
        }
    }

    /**
     * S&Cセンサーの情報を設定する
     *
     * @param crankRpm        クランク回転数 / min
     * @param crankRevolution クランク合計回転数
     * @param wheelRpm        ホイール回転数 / min
     * @param wheelRevolution ホイール合計回転数
     * @return 更新した情報数
     */
    public int setSpeedAndCadence(float crankRpm, int crankRevolution, float wheelRpm, int wheelRevolution) {
        synchronized (lock) {
            int result = 0;
            if (mSensorSpeedData.setSensorSpeed(wheelRpm, wheelRevolution)) {
                ++result;
            }

            if (mCadenceData.setCadence(crankRpm, crankRevolution)) {
                ++result;
            }

            return result;
        }
    }

    /**
     * 時間を更新する
     */
    public void onUpdateTime(long diffTimeMs) {
        if (diffTimeMs <= 0) {
            throw new IllegalArgumentException();
        }

        synchronized (lock) {
            // 時計を進める
            mClock.offset(diffTimeMs);

            // 消費カロリーを更新する
            mFitnessData.onUpdateTime(diffTimeMs);

            // 走行距離を更新する
            mDistanceData.onUpdate(diffTimeMs, mSpeedData.getSpeedKmh());

            // 自走中であれば走行距離を追加する
            if (isActiveMoving()) {
                mSessionData.addActiveTimeMs(diffTimeMs);
            }
        }
    }

    private void getSpecs(RawSpecs.RawAppSpec dst) {
        dst.appPackageName = BuildConfig.APPLICATION_ID;
        dst.appVersionName = BuildConfig.VERSION_NAME;
        dst.protocolVersion = com.eaglesakura.andriders.sdk.BuildConfig.ACE_PROTOCOL_VERSION;
    }

    private void getStatus(RawCentralData.RawCentralStatus dst) {
        dst.debug = Settings.isDebugable();
    }

    /**
     * 現在の状態からセントラルを生成する
     */
    public RawCentralData newCentralData() {
        RawCentralData result = new RawCentralData();

        result.specs = new RawSpecs();
        result.specs.application = new RawSpecs.RawAppSpec();
        result.specs.fitness = new RawSpecs.RawFitnessSpec();
        result.centralStatus = new RawCentralData.RawCentralStatus();
        result.sensor = new RawSensorData();
        result.session = new RawSessionData();
        result.today = new RawSessionData();

        getSpecs(result.specs.application);
        getStatus(result.centralStatus);

        mFitnessData.getSpec(result.specs.fitness);

        // 各種センサーデータを取得する
        mFitnessData.getSensor(result.sensor);
        mCadenceData.getSensor(result.sensor);
        mSpeedData.getSensor(result.sensor);
        mLocationData.getSensor(result.sensor);

        return result;
    }

    /**
     * ログを強制的に書き出す
     *
     * TODO 実装する
     */
    public void commitLog() {

    }

}
