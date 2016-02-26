package com.eaglesakura.andriders.central;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.computer.CycleComputerManager;
import com.eaglesakura.andriders.central.data.CycleComputerData;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.internal.protocol.ExtensionProtocol;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.internal.protocol.RawSensorData;
import com.eaglesakura.andriders.internal.protocol.RawSessionData;
import com.eaglesakura.andriders.internal.protocol.RawSpecs;

import android.content.Context;

/**
 * サイコンのデータコントロールを行う
 *
 * 複数のExtensionから同時多発的に操作されるため、必ずパイプラインを通して直列的に整列されて操作されるようにする。
 */
public class CentralDataManager extends CycleComputerManager {

    /**
     * サイコンデータ管理
     */
    private final CycleComputerData mCycleComputerData;

    public CentralDataManager(Context context) {
        super(context);
        mCycleComputerData = new CycleComputerData(context, System.currentTimeMillis());
    }

    private long now() {
        return System.currentTimeMillis();
    }

    /**
     * 定時処理を行う
     * <p/>
     * 処理はパイプラインに流される
     */
    @Override
    public void updateInPipeline(double deltaTimeSec) {
        mCycleComputerData.onUpdateTime((long) (deltaTimeSec * 1000));
    }

    /**
     * GPS座標を更新する
     */
    public void setLocation(final ExtensionProtocol.SrcLocation loc) {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                mCycleComputerData.setLocation(now(), loc.latitude, loc.longitude, loc.altitude, loc.accuracyMeter);
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
                mCycleComputerData.setSpeedAndCadence(now(), sc.crankRpm, sc.crankRevolution, sc.wheelRpm, sc.wheelRevolution);
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
                mCycleComputerData.setHeartrate(now(), heartrate.bpm);
            }
        });
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

        return result;
    }

    /**
     * セッション終了
     */
    public void finishSession() {
        mPipeline.pushBack(new Runnable() {
            @Override
            public void run() {
                mCycleComputerData.commitLog();
            }
        });
    }

}
