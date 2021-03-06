package com.eaglesakura.andriders.service.command;

import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.system.ScreenEventReceiver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

/**
 * 近接コマンド用センサーの管理を行う
 */
public class ProximitySensorManager {

    @NonNull
    private Context mContext;

    @NonNull
    private final ProximityData.Bus mProximityData;

    @NonNull
    private final ScreenEventReceiver mScreenEventReceiver;

    @NonNull
    private final SensorManager mSensorManager;

    public ProximitySensorManager(@NonNull Context context) {
        mContext = context;
        mScreenEventReceiver = new ScreenEventReceiver(mContext);
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mProximityData = new ProximityData.Bus(new ProximityData(new Date(), false));
    }

    @NonNull
    public ProximityData.Bus getProximityDataBus() {
        return mProximityData;
    }

    public void connect() {
        mScreenEventReceiver.connect();

        // 近接コマンドに接続する
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_PROXIMITY);
        if (!sensorList.isEmpty()) {
            mSensorManager.registerListener(mProximityListener, sensorList.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void disconnect() {
        mScreenEventReceiver.disconnect();
        try {
            mSensorManager.unregisterListener(mProximityListener);
        } catch (Exception e) {
            AppLog.report(e);
        }
    }


    /**
     * センサーイベントの受理を行う
     */
    private SensorEventListener mProximityListener = new SensorEventListener() {
        /**
         * 近接センサーとして許可する値
         */
        private static final float COMMAND_INPUT_VALUE = 0.1f;

        @Override
        public void onSensorChanged(final SensorEvent event) {
            float value = event.values[0];

            if (!mScreenEventReceiver.isScreenPowerOn()) {
                // スクリーンOFFならコマンドに反応しない
                value = 1000;
            }

            AppLog.proximity("Proximity[%.1f]", value);

            boolean proximity = (value < COMMAND_INPUT_VALUE);
            if (mProximityData.isProximity() == proximity) {
                // ステートが変わってない
                return;
            }

            AppLog.proximity("Modified ProximityState[%s]", proximity ? "YES" : "NO");

            // ステートの切り替わりを送信する
            mProximityData.modified(new ProximityData(new Date(), proximity));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

}
