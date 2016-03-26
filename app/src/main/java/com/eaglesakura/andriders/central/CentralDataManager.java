package com.eaglesakura.andriders.central;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.central.geo.GeoSpeedData;
import com.eaglesakura.andriders.central.geo.LocationData;
import com.eaglesakura.andriders.central.hrsensor.FitnessData;
import com.eaglesakura.andriders.central.log.SessionLogger;
import com.eaglesakura.andriders.central.scsensor.CadenceData;
import com.eaglesakura.andriders.central.scsensor.SensorSpeedData;
import com.eaglesakura.andriders.central.session.SessionData;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.andriders.serialize.RawSessionData;
import com.eaglesakura.andriders.serialize.RawSpecs;
import com.eaglesakura.andriders.sensor.SpeedZone;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.android.device.external.StorageInfo;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * サイコンが持つべき情報を統括する
 * <p>
 * 各種Data系クラスへのアクセサはUnitTestのためpackage privateとして扱う
 */
public class CentralDataManager {
    /**
     * app mContext
     */
    @NonNull
    private final Context mContext;

    /**
     * カロリー等のフィットネス情報管理
     * <p>
     * 心拍から計算される
     */
    @NonNull
    final FitnessData mFitnessData;

    /**
     * 速度情報管理
     * <p>
     * S&Cセンサーが有効であればセンサーを、無効であれば位置情報をソースにして速度を取得する。
     * 位置情報が無効である場合は速度0となる。
     */
    @NonNull
    final SpeedData mSpeedData;

    /**
     * センサー由来の速度情報
     * <p>
     * S&Cセンサーの取得時に更新される
     */
    @NonNull
    final SensorSpeedData mSensorSpeedData;

    /**
     * ケイデンス情報
     * <p>
     * S&Cセンサーの取得時に更新される
     */
    @NonNull
    final CadenceData mCadenceData;

    /**
     * 移動距離管理
     * <p>
     * 現在速度と時間経過から自動的に計算される
     */
    @NonNull
    final DistanceData mDistanceData;

    /**
     * 位置情報管理
     */
    @NonNull
    final LocationData mLocationData;

    /**
     * セッション情報管理
     */
    @NonNull
    final SessionData mSessionData;

    @NonNull
    final SessionLogger mSessionLogger;

    /**
     * 時刻設定
     */
    @NonNull
    private final Clock mClock;

    /**
     * タイマー
     */
    @NonNull
    private final ClockTimer mClockTimer;

    /**
     * sync管理
     * <p>
     * データは別なパイプラインから呼び出されるため、ロックを行ってデータがコンフリクトしないようにする。
     */
    @NonNull
    private final Object lock = new Object();

    /**
     * 最後に生成されたセントラル情報
     * <p>
     * onUpdateの更新時に生成される
     */
    private RawCentralData mLatestCentralData;

    /**
     * ログDBのパスを取得する
     */
    public static File getLogDatabasePath(Context context) {
        File external = StorageInfo.getExternalStorageRoot(context);
        return new File(external, "db/session.db");
    }

    /**
     * サイコンデータを生成する
     *
     * @param clock 同期用時計
     */
    public CentralDataManager(Context context, Clock clock) {
        mContext = context.getApplicationContext();
        mClock = clock;
        mClockTimer = new ClockTimer(clock);
        mSessionData = new SessionData(mClock, mClock.now());
        mFitnessData = new FitnessData(mClock);
        mSensorSpeedData = new SensorSpeedData(mClock);
        mCadenceData = new CadenceData(mClock);
        mDistanceData = new DistanceData(mClock);

        GeoSpeedData geoSpeedData = new GeoSpeedData(mClock); // 位置センサー由来の速度計

        mLocationData = new LocationData(mClock, geoSpeedData);
        mSpeedData = new SpeedData(mClock, geoSpeedData, mSensorSpeedData);

        // ログコントローラ
        mSessionLogger = new SessionLogger(context, mSessionData.getSessionId(), getLogDatabasePath(context), clock);
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
     * <p>
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
     * <p>
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
     *
     * @return 更新されたらtrue
     */
    public boolean onUpdate() {
        long diffTimeMs = mClockTimer.end();
        if (diffTimeMs <= 0) {
            return false;
        }
        mClockTimer.start();

        synchronized (lock) {
            // 消費カロリーを更新する
            mFitnessData.onUpdateTime(diffTimeMs);

            // 走行距離を更新する
            final double moveDistanceKm = mDistanceData.onUpdate(diffTimeMs, mSpeedData.getSpeedKmh());

            // 自走中であれば走行距離を追加する
            if (isActiveMoving()) {
                mSessionData.addActiveTimeMs(diffTimeMs);
                mSessionData.addActiveDistanceKm(moveDistanceKm);
            }

            // セントラル情報を生成する
            mLatestCentralData = newCentralData();

            // 毎フレーム更新をかける。結果としてデータが書き換わるので、Latestを更新する
            mSessionLogger.onUpdate(mLatestCentralData);
            mSessionLogger.getTotalData(mLatestCentralData.today);
        }
        return true;
    }

    private void getSpecs(RawSpecs.RawAppSpec dst) {
        dst.appPackageName = BuildConfig.APPLICATION_ID;
        dst.appVersionName = BuildConfig.VERSION_NAME;
        dst.protocolVersion = com.eaglesakura.andriders.sdk.BuildConfig.ACE_PROTOCOL_VERSION;
    }

    private void getStatus(RawCentralData.RawCentralStatus dst) {
        dst.debug = Settings.isDebugable();
        dst.date = now();
    }

    /**
     * このセッションでの統計情報を取得する
     */
    private void getSession(RawSessionData dst) {
        dst.flags |= (isActiveMoving() ? RawSessionData.FLAG_ACTIVE : 0x00);
        dst.activeTimeMs = (int) mSessionData.getActiveTimeMs();
        dst.activeDistanceKm = (float) mSessionData.getActiveDistanceKm();
        dst.distanceKm = (float) mDistanceData.getDistanceKm();
        dst.sessionId = mSessionData.getSessionId();
        dst.startTime = mSessionData.getStartDate();
        dst.durationTimeMs = (int) (now() - dst.startTime);
        dst.sumAltitudeMeter = (float) mLocationData.getSumAltitude();

        dst.fitness = new RawSessionData.RawFitnessStatus();
        mFitnessData.getFitness(dst.fitness);
    }

    /**
     * 最後にonUpdateTime()が呼び出された時点でのセントラル情報を取得する
     */
    public RawCentralData getLatestCentralData() {
        return mLatestCentralData;
    }

    /**
     * 現在の状態からセントラルを生成する
     */
    public RawCentralData newCentralData() {
        synchronized (lock) {
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
            getSession(result.session);
            mSessionLogger.getTotalData(result.today);

            mFitnessData.getSpec(result.specs.fitness);

            // 各種センサーデータを取得する
            mFitnessData.getSensor(result.sensor);
            mCadenceData.getSensor(result.sensor);
            mSpeedData.getSensor(result.sensor);
            mLocationData.getSensor(result.sensor);

            return result;
        }
    }

    /**
     * 必要なデータをデータベースへ書き出す
     * <p>
     * TODO 実装する
     */
    public void commit() {
        mSessionLogger.commit();
    }
}
