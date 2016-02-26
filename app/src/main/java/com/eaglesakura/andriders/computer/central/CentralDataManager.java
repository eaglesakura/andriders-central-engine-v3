package com.eaglesakura.andriders.computer.central;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.computer.CycleComputerManager;
import com.eaglesakura.andriders.computer.central.data.geo.AltitudeData;
import com.eaglesakura.andriders.computer.central.data.DistanceData;
import com.eaglesakura.andriders.computer.central.data.hrsensor.FitnessData;
import com.eaglesakura.andriders.computer.central.data.geo.GeoSpeedData;
import com.eaglesakura.andriders.computer.central.geo.LocationCentral;
import com.eaglesakura.andriders.computer.central.sensor.CadenceDataCentral;
import com.eaglesakura.andriders.computer.central.sensor.HeartrateDataCentral;
import com.eaglesakura.andriders.computer.central.sensor.SpeedDataCentral;
import com.eaglesakura.andriders.computer.central.status.SessionStatusCentral;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.internal.protocol.ExtensionProtocol;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.internal.protocol.RawSensorData;
import com.eaglesakura.andriders.internal.protocol.RawSessionData;
import com.eaglesakura.andriders.internal.protocol.RawSpecs;
import com.eaglesakura.andriders.sensor.SpeedZone;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Timer;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * サイクルコンピュータのデータを中央集積する
 */
public class CentralDataManager extends CycleComputerManager {
    /**
     * 受け取ったデータが無効となるデフォルト時刻
     */
    public static final long DATA_TIMEOUT_MS = 1000 * 30;

    private final List<ICentral> mCentrals = new ArrayList<>();

    /**
     * 現在のステータス管理
     */
    private final SessionStatusCentral mSessionStatusCentral;

    /**
     * 登録されたセンサー情報一覧
     */
    private final HeartrateDataCentral mHeartrateDataCentral;

    /**
     * 速度
     */
    private final SpeedDataCentral mSpeedDataCentral;

    /**
     * ケイデンス
     */
    private final CadenceDataCentral mCadenceDataCentral;

    /**
     * ロケーション情報
     */
    private final LocationCentral mLocationCentral;

    private final FitnessData mFitnessDataCalculator = new FitnessData();

    private final DistanceData mDistanceCalculator = new DistanceData();

    /**
     * データsync用のロック
     */
    private final Object mLock = new Object();

    /**
     * 最後の更新時刻
     */
    private Timer mLastUpdatedTime = new Timer();

    public CentralDataManager(Context context) {
        super(context);

        // センサー用マネージャを追加
        {
            mSessionStatusCentral = new SessionStatusCentral(mContext);
            mCentrals.add(mSessionStatusCentral);

            mHeartrateDataCentral = new HeartrateDataCentral(mFitnessDataCalculator);
            mCentrals.add(mHeartrateDataCentral);

            mSpeedDataCentral = new SpeedDataCentral(mDistanceCalculator);
            mCentrals.add(mSpeedDataCentral);

            mCadenceDataCentral = new CadenceDataCentral();
            mCentrals.add(mCadenceDataCentral);

            mLocationCentral = new LocationCentral();
            mCentrals.add(mLocationCentral);
        }

        // 初期化タスクを投げておく
        initialize();
    }

    /**
     * 初期ロードを行う
     */
    private void initialize() {
        startSession();
    }

    /**
     * 定時処理を行う
     * <p/>
     * 処理はパイプラインに流される
     */
    @Override
    public void updateInPipeline(double deltaTimeSec) {
        synchronized (mLock) {
            update(mCentrals);
        }
    }

    /**
     * GPS座標を更新する
     */
    public void setLocation(final ExtensionProtocol.SrcLocation loc) {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                synchronized (mLock) {
                    // GPS座標を更新する
                    mLocationCentral.setLocation(loc);

                    // GPS由来の速度を更新する
                    mSpeedDataCentral.setGpsSpeed(mLocationCentral.getDistanceDataCalculator());

                    LogUtil.log("GPS lat(%f) lng(%f) alt(%f) acc(%f) spd(%.1f km/h)",
                            loc.latitude, loc.longitude, loc.altitude, loc.accuracyMeter,
                            mLocationCentral.getDistanceDataCalculator().getSpeedKmh()
                    );
                }
            }
        });
    }

    /**
     * Speed&Cadenceセンサーの情報を更新する
     */
    public void setSpeedAndCadence(final ExtensionProtocol.SrcSpeedAndCadence sc) {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                synchronized (mLock) {
                    // ケイデンス設定を行う
                    mCadenceDataCentral.setCadence(sc.crankRpm, sc.crankRevolution);

                    // センサー由来の速度更新を行う
                    mSpeedDataCentral.setSensorSpeed(sc.wheelRpm, sc.wheelRevolution);
                }
            }
        });
    }

    /**
     * 心拍を更新する
     */
    public void setHeartrate(final ExtensionProtocol.SrcHeartrate heartrate) {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                synchronized (mLock) {
                    mHeartrateDataCentral.setHeartrate(heartrate.bpm);
                }
            }
        });
    }

    /**
     * セッション開始
     */
    public void startSession() {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                synchronized (mLock) {
                    // 位置ステートをリセットする
                    mLocationCentral.setAltitudeDataCalculator(new AltitudeData());
                    mLocationCentral.setDistanceDataCalculator(new GeoSpeedData());

                    mSessionStatusCentral.onSessionStart();
                }
            }
        });
    }

    /**
     * ユーザーが自走している状態であればtrue
     */
    public boolean isActiveMoving() {
        synchronized (mLock) {
            if (mSpeedDataCentral.getSpeedZone() == SpeedZone.Stop) {
                // 停止中はアクティブではない
                return false;
            }

            if (mCadenceDataCentral.getRpm() <= 0) {
                // ケイデンスが取得できないので、アクティブではない
                return false;
            }

            return true;
        }
    }

    private void buildSpecs(RawCentralData result) {
        result.specs.application.appPackageName = BuildConfig.APPLICATION_ID;
        result.specs.application.appVersionName = BuildConfig.VERSION_NAME;
        result.specs.application.protocolVersion = com.eaglesakura.andriders.sdk.BuildConfig.ACE_PROTOCOL_VERSION;

        result.specs.fitness.weight = (float) mSettings.getUserProfiles().getUserWeight();
        result.specs.fitness.heartrateNormal = (short) mSettings.getUserProfiles().getNormalHeartrate();
        result.specs.fitness.heartrateMax = (short) mSettings.getUserProfiles().getMaxHeartrate();

        result.centralStatus.debug = Settings.isDebugable();
    }

    /**
     * Central Dataを生成する
     * 別途同期を行うため、syncは行わない
     */
    RawCentralData newCentralData() {
        RawCentralData result = new RawCentralData();

        result.specs = new RawSpecs();
        result.specs.application = new RawSpecs.RawAppSpec();
        result.specs.fitness = new RawSpecs.RawFitnessSpec();
        result.centralStatus = new RawCentralData.RawCentralStatus();
        result.sensor = new RawSensorData();
        result.session = new RawSessionData();
        result.today = new RawSessionData();

        buildSpecs(result);

        for (ICentral central : mCentrals) {
            central.buildData(this, result);
        }

        return result;
    }

    /**
     * セッション終了
     */
    public void finishSession() {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                synchronized (mLock) {
                    mSessionStatusCentral.onSessionFinished();
                }
            }
        });
    }

    /**
     * 不要になったデータを削除する
     */
    <T extends ICentral> void update(Iterable<T> datas) {
        synchronized (datas) {
            long diffTimeMS = mLastUpdatedTime.end();

            Iterator<T> iterator = datas.iterator();
            while (iterator.hasNext()) {
                T data = iterator.next();
                if (data.isDelete(this)) {
                    iterator.remove();
                } else {
                    data.onUpdate(this, diffTimeMS);
                }
            }

            mLastUpdatedTime.end();
        }
    }

    /**
     * サイコン情報として登録されたデータ
     */
    public interface ICentral {
        /**
         * 定期更新を行わせる
         *
         * @param diffTimeMs 前回の更新からの差分時間
         */
        void onUpdate(CentralDataManager parent, long diffTimeMs);

        /**
         * 送信するデータを構築する
         *
         * @param parent 呼び出し元
         * @param result 構築先
         */
        void buildData(CentralDataManager parent, RawCentralData result);

        boolean isDelete(CentralDataManager parent);
    }

}
