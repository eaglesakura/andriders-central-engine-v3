package com.eaglesakura.andriders.basicui.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.SensorDataReceiver;
import com.eaglesakura.andriders.central.base.BaseCalculator;
import com.eaglesakura.andriders.display.ZoneColor;
import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.plugin.CentralEngineConnection;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.display.BasicValue;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.android.margarine.BindStringArray;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.util.StringUtil;
import com.eaglesakura.util.Timer;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * 速度更新を行う
 *
 * MEMO: 速度は「停止している」可能性があるため、定期的に送信するようにしている。
 */
public class SpeedDisplaySender extends DisplayDataSender {
    public static final String DISPLAY_ID = "DISPLAY_ID_SPEED";

    @NonNull
    private final ZoneColor mZoneColor;

    @BindStringArray(R.array.Display_Speed_ZoneName)
    @NonNull
    String[] mZoneTitles;

    RawSensorData.RawSpeed mSpeed;

    /**
     * 最後にデータを受け取ってからの時間
     */
    Timer mReceiveTimer = new Timer();

    /**
     * 時間がタイムアウトしたことを知らせる
     */
    boolean mTimeoutMessageSend = false;

    public SpeedDisplaySender(@NonNull CentralEngineConnection session, @NonNull ZoneColor zoneColor) {
        super(session);
        mZoneColor = zoneColor;

        MargarineKnife.bind(this, this);
    }

    public SpeedDisplaySender bind() {
        if (mDataReceiver != null) {
            mDataReceiver.addHandler(mSpeedHandler);
        }
        return this;
    }

    @Override
    public void onUpdate(double deltaSec) {
        if (mSpeed == null) {
            return;
        }

        Context context = getContext();

        DisplayData data = new DisplayData(context, DISPLAY_ID);
        BasicValue value = new BasicValue();

        double speed = mSpeed.speedKmPerHour;
        if ((mSpeed.flags & RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_GPS) != 0) {
            value.setTitle(context.getString(R.string.Display_Common_Speed_GPS));

            // GPSデータで、かつ最終取得からタイムアウトしていたら速度をリセットする
            if (mReceiveTimer.end() > BaseCalculator.DATA_TIMEOUT_MS) {
                speed = 0;
                if (!mTimeoutMessageSend) {
                    NotificationData notification = new NotificationData.Builder(context)
                            .icon(R.mipmap.ic_speed)
                            .message("GPS速度 / タイムアウト")
                            .getNotification();
                    mSession.getDisplayExtension().queueNotification(notification);
                    mTimeoutMessageSend = true;
                }
            }
        } else {
            value.setTitle(context.getString(R.string.Display_Common_Speed_Sensor));
        }
        value.setValue(StringUtil.format("%.01f", speed));
        value.setBarColorARGB(mZoneColor.getColor(mSpeed.zone));
        value.setZoneText(mZoneTitles[mSpeed.zone.ordinal()]);

        data.setValue(value);
        mSession.getDisplayExtension().setValue(data);
    }

    private SensorDataReceiver.SpeedHandler mSpeedHandler = new SensorDataReceiver.SpeedHandler() {
        @Override
        public void onReceived(@NonNull RawCentralData master, @NonNull RawSensorData.RawSpeed sensor) {
            mSpeed = sensor;
            mReceiveTimer.start();
            mTimeoutMessageSend = false;

            // データを受け取ったらUIも更新
            onUpdate(1.0 / 60.0);
        }

        @Override
        public void onDisconnectedSensor(@NonNull RawCentralData master) {
            Context context = getContext();

            DisplayData data = new DisplayData(context, DISPLAY_ID);
            data.setTimeoutMs(1000 * 60);

            BasicValue value = new BasicValue();
            value.setTitle(context.getString(R.string.Display_Common_Speed));
            value.setValue(context.getString(R.string.Display_Common_Reconnect));
            value.setBarColorARGB(0x00);

            data.setValue(value);
            mSession.getDisplayExtension().setValue(data);
        }
    };

    public static DisplayKey newInformation(Context context) {
        DisplayKey result = new DisplayKey(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Display_Common_Speed));
        return result;
    }
}
