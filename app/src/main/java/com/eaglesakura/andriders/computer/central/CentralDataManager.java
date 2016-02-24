package com.eaglesakura.andriders.computer.central;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.computer.CycleComputerManager;
import com.eaglesakura.andriders.computer.central.calculator.AltitudeDataCalculator;
import com.eaglesakura.andriders.computer.central.calculator.DistanceDataCalculator;
import com.eaglesakura.andriders.computer.central.calculator.FitnessDataCalculator;
import com.eaglesakura.andriders.computer.central.calculator.SpeedDataCalculator;
import com.eaglesakura.andriders.computer.central.geo.LocationCentral;
import com.eaglesakura.andriders.computer.central.sensor.CadenceDataCentral;
import com.eaglesakura.andriders.computer.central.sensor.HeartrateDataCentral;
import com.eaglesakura.andriders.computer.central.sensor.SpeedDataCentral;
import com.eaglesakura.andriders.computer.central.status.SessionStatusCentral;
import com.eaglesakura.andriders.internal.protocol.ApplicationProtocol;
import com.eaglesakura.andriders.internal.protocol.ExtensionProtocol;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.internal.protocol.RawSensorData;
import com.eaglesakura.util.LogUtil;

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

    private final FitnessDataCalculator mFitnessDataCalculator = new FitnessDataCalculator();

    private final SpeedDataCalculator mSpeedDataCalculator = new SpeedDataCalculator();


    public CentralDataManager(Context context) {
        super(context);

        // センサー用マネージャを追加
        {
            mSessionStatusCentral = new SessionStatusCentral(mContext);
            mCentrals.add(mSessionStatusCentral);

            mHeartrateDataCentral = new HeartrateDataCentral(mFitnessDataCalculator);
            mCentrals.add(mHeartrateDataCentral);

            mSpeedDataCentral = new SpeedDataCentral(mSpeedDataCalculator);
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
    }

    /**
     * 定時処理を行う
     * <p/>
     * 処理はパイプラインに流される
     */
    @Override
    public void updateInPipeline(double deltaTimeSec) {
        update(mCentrals);
    }

    /**
     * GPS座標を更新する
     */
    public void setLocation(final ExtensionProtocol.SrcLocation loc) {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                // GPS座標を更新する
                mLocationCentral.setRaw(loc);

                // GPS由来の速度を更新する
                DistanceDataCalculator distanceDataCalculator = mLocationCentral.getDistanceDataCalculator();
                mSpeedDataCentral.setGpsSensorSpeed(distanceDataCalculator.getGeoSpeedKmh());

                LogUtil.log("GPS lat(%f) lng(%f) alt(%f) acc(%f) spd(%.1f km/h)",
                        loc.latitude, loc.longitude, loc.altitude, loc.accuracyMeter,
                        distanceDataCalculator.getGeoSpeedKmh()
                );
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
                // ケイデンス設定を行う
                mCadenceDataCentral.setCadence(sc.crankRpm, sc.crankRevolution);

                // S&Cセンサー由来の速度更新を行う
                mSpeedDataCentral.setBleSensorSpeed(sc.wheelRpm, sc.wheelRevolution);
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
                mHeartrateDataCentral.setHeartrate(heartrate.bpm);
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
                // 位置ステートをリセットする
                mLocationCentral.setAltitudeDataCalculator(new AltitudeDataCalculator());
                mLocationCentral.setDistanceDataCalculator(new DistanceDataCalculator());

                mSessionStatusCentral.onSessionStart();
            }
        });
    }

    /**
     * Central Dataを生成する
     * 別途同期を行うため、syncは行わない
     */
    private RawCentralData newCentralData() {
        RawCentralData result = new RawCentralData();

        result.centralSpec = new ApplicationProtocol.RawCentralSpec();
        result.centralSpec.appPackageName = BuildConfig.APPLICATION_ID;
        result.centralSpec.appVersionName = BuildConfig.VERSION_NAME;
        result.centralSpec.protocolVersion = com.eaglesakura.andriders.sdk.BuildConfig.ACE_PROTOCOL_VERSION;

        result.centralStatus = new ApplicationProtocol.RawCentralStatus();
        result.sensor = new RawSensorData();

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
                mSessionStatusCentral.onSessionFinished();
            }
        });
    }

    /**
     * 不要になったデータを削除する
     */
    <T extends ICentral> void update(Iterable<T> datas) {
        synchronized (datas) {
            Iterator<T> iterator = datas.iterator();
            while (iterator.hasNext()) {
                T data = iterator.next();
                if (data.isDelete(this)) {
                    iterator.remove();
                } else {
                    data.onUpdate(this);
                }
            }
        }
    }

    /**
     * サイコン情報として登録されたデータ
     */
    public interface ICentral {
        void onUpdate(CentralDataManager parent);

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
