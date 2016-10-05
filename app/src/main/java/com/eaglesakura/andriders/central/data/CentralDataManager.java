package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.central.data.fitness.FitnessData;
import com.eaglesakura.andriders.central.data.geo.LocationData;
import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.sensor.CadenceData;
import com.eaglesakura.andriders.central.data.sensor.DistanceData;
import com.eaglesakura.andriders.central.data.sensor.GeoSpeedData;
import com.eaglesakura.andriders.central.data.sensor.SensorSpeedData;
import com.eaglesakura.andriders.central.data.sensor.SpeedData;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.central.data.session.SessionRecord;
import com.eaglesakura.andriders.central.data.session.SessionTime;
import com.eaglesakura.andriders.data.gpx.GpxPoint;
import com.eaglesakura.andriders.sensor.SpeedZone;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawGeoPoint;
import com.eaglesakura.andriders.serialize.RawRecord;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.andriders.serialize.RawSessionData;
import com.eaglesakura.andriders.serialize.RawSpecs;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.util.CollectionUtil;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * サイコンが持つべき情報を統括する
 * <p>
 * 各種Data系クラスへのアクセサはUnitTestのためpackage privateとして扱う
 */
public class CentralDataManager {

    /**
     * セッション情報
     */
    private final SessionInfo mSessionInfo;

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
     * カロリー等のフィットネス情報管理
     * <p>
     * 心拍から計算される
     */
    @NonNull
    FitnessData mFitnessData;

    /**
     * 速度情報管理
     * <p>
     * S&Cセンサーが有効であればセンサーを、無効であれば位置情報をソースにして速度を取得する。
     * 位置情報が無効である場合は速度0となる。
     */
    @NonNull
    SpeedData mSpeedData;

    /**
     * センサー由来の速度情報
     * <p>
     * S&Cセンサーの取得時に更新される
     */
    @NonNull
    SensorSpeedData mSensorSpeedData;

    /**
     * ケイデンス情報
     * <p>
     * S&Cセンサーの取得時に更新される
     */
    @NonNull
    CadenceData mCadenceData;

    /**
     * 移動距離管理
     * <p>
     * 現在速度と時間経過から自動的に計算される
     */
    @NonNull
    DistanceData mDistanceData;

    /**
     * 位置情報管理
     */
    @NonNull
    LocationData mLocationData;

    /**
     * セッション情報管理
     */
    @NonNull
    SessionTime mSessionTime;

    /**
     * セッションに関連した最高記録を保持する
     */
    @NonNull
    SessionRecord mSessionRecord;

    /**
     * 最後に生成されたセントラル情報
     * <p>
     * onUpdateの更新時に生成される
     */
    private RawCentralData mLatestCentralData;

    /**
     * サイコンデータを生成する
     *
     * @param info            セッション情報
     * @param allStatistics   全セッショントータル（初回セッションは読み込めないので、null許容）
     * @param todayStatistics 今日トータル（初回セッションは読み込めないので、null許容）
     */
    public CentralDataManager(@NonNull SessionInfo info, @Nullable LogStatistics allStatistics, @Nullable LogStatistics todayStatistics) {

        // データチェック
        if (!CollectionUtil.allNotNull(info)) {
            throw new NullPointerException();
        }

        mSessionInfo = info;
        mClockTimer = new ClockTimer(info.getSessionClock());

        Clock clock = info.getSessionClock();

        mSessionTime = new SessionTime(clock);
        mFitnessData = new FitnessData(clock, info.getUserProfiles());
        mSensorSpeedData = new SensorSpeedData(clock, info.getUserProfiles().getWheelOuterLength());
        mCadenceData = new CadenceData(clock, info.getUserProfiles());
        mDistanceData = new DistanceData(clock);
        mSessionRecord = new SessionRecord(info, allStatistics, todayStatistics);
        {
            GeoSpeedData geoSpeedData = new GeoSpeedData(clock); // 位置センサー由来の速度計
            mLocationData = new LocationData(clock, geoSpeedData);
            mSpeedData = new SpeedData(clock, info.getUserProfiles(), geoSpeedData, mSensorSpeedData);
        }
    }

    /**
     * サイコンの時刻を取得する
     */
    long now() {
        return mSessionInfo.getSessionClock().now();
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
    public long getSessionId() {
        return mSessionInfo.getSessionId();
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
     * GPXの地点情報から設定する
     */
    public void setGpxPoint(GpxPoint point) {
        RawGeoPoint location = point.getLocation();
        if (location != null) {
            setLocation(location.latitude, location.longitude, location.altitude, 10.0);
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
                mSessionTime.addActiveTimeMs(diffTimeMs);
                mSessionTime.addActiveDistanceKm(moveDistanceKm);
            }

            // セントラル情報を生成する
            mLatestCentralData = newCentralData();
        }
        return true;
    }

    private void getSpecs(RawSpecs.RawAppSpec dst) {
        dst.appPackageName = BuildConfig.APPLICATION_ID;
        dst.appVersionName = BuildConfig.VERSION_NAME;
        dst.protocolVersion = com.eaglesakura.andriders.sdk.BuildConfig.ACE_PROTOCOL_VERSION;
    }

    private void getStatus(RawCentralData.RawCentralStatus dst) {
        dst.debug = mSessionInfo.isDebuggable();
        dst.date = now();
    }

    /**
     * このセッションでの統計情報を取得する
     */
    private void getSession(RawSessionData dst) {
        dst.flags |= (isActiveMoving() ? RawSessionData.FLAG_ACTIVE : 0x00);
        dst.activeTimeMs = (int) mSessionTime.getActiveTimeMs();
        dst.activeDistanceKm = (float) mSessionTime.getActiveDistanceKm();
        dst.distanceKm = (float) mDistanceData.getDistanceKm();
        dst.sessionId = mSessionInfo.getSessionId();
        dst.startTime = mSessionTime.getStartDate();
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
            result.record = new RawRecord();

            getSpecs(result.specs.application);
            getStatus(result.centralStatus);
            getSession(result.session);

            mFitnessData.getSpec(result.specs.fitness);

            // 各種センサーデータを取得する
            mFitnessData.getSensor(result.sensor);
            mCadenceData.getSensor(result.sensor);
            mSpeedData.getSensor(result.sensor);
            mLocationData.getSensor(result.sensor);

            // 現在の状態に基づいて記録を更新し、トータル記録を算出する
            mSessionRecord.update(result);
            mSessionRecord.getTotalData(result.centralStatus, result.today, result.record);

            return result;
        }
    }
}
