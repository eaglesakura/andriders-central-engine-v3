package com.eaglesakura.andriders.central;

import com.eaglesakura.andriders.central.data.CycleComputerData;
import com.eaglesakura.andriders.computer.CycleComputerManager;
import com.eaglesakura.andriders.internal.protocol.ExtensionProtocol;
import com.eaglesakura.android.rx.SubscriptionController;

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

    public CentralDataManager(Context context, SubscriptionController subscription) {
        super(context, subscription);
        mCycleComputerData = new CycleComputerData(context, System.currentTimeMillis());
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
        execute(it -> {
            mCycleComputerData.setLocation(loc.latitude, loc.longitude, loc.altitude, loc.accuracyMeter);
            return this;
        });
    }

    /**
     * Speed&Cadenceセンサーの情報を更新する
     */
    public void setSpeedAndCadence(final ExtensionProtocol.SrcSpeedAndCadence sc) {
        execute(it -> {
            mCycleComputerData.setSpeedAndCadence(sc.crankRpm, sc.crankRevolution, sc.wheelRpm, sc.wheelRevolution);
            return this;
        });
    }

    /**
     * 心拍を更新する
     */
    public void setHeartrate(final ExtensionProtocol.SrcHeartrate heartrate) {
        execute(it -> {
            mCycleComputerData.setHeartrate(heartrate.bpm);
            return this;
        });
    }

    /**
     * セッション終了
     */
    public void finishSession() {
        execute(it -> {
            mCycleComputerData.commitLog();
            return this;
        });
    }

}
