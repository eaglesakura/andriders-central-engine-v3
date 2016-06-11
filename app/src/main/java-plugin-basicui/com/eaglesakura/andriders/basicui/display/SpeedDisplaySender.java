package com.eaglesakura.andriders.basicui.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.SensorDataReceiver;
import com.eaglesakura.andriders.display.ZoneColor;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.CentralEngineConnection;
import com.eaglesakura.andriders.plugin.display.BasicValue;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.android.margarine.BindStringArray;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * ケイデンス更新を行う
 */
public class SpeedDisplaySender extends DisplayDataSender {
    public static final String DISPLAY_ID = "DISPLAY_ID_SPEED";

    @NonNull
    private final ZoneColor mZoneColor;

    @BindStringArray(R.array.Display_Speed_ZoneName)
    @NonNull
    String[] mZoneTitles;

    public SpeedDisplaySender(@NonNull CentralEngineConnection session, @NonNull ZoneColor zoneColor) {
        super(session);
        mZoneColor = zoneColor;

        MargarineKnife.bind(this, this);
    }

    public void bind() {
        if (mDataReceiver != null) {
            mDataReceiver.addHandler(mSpeedHandler);
        }
    }

    private SensorDataReceiver.SpeedHandler mSpeedHandler = new SensorDataReceiver.SpeedHandler() {
        @Override
        public void onReceived(@NonNull RawCentralData master, @NonNull RawSensorData.RawSpeed sensor) {
            Context context = getContext();

            DisplayData data = new DisplayData(context, DISPLAY_ID);

            BasicValue value = new BasicValue();
            if ((sensor.flags & RawSensorData.RawSpeed.SPEEDSENSOR_TYPE_GPS) != 0) {
                value.setTitle(context.getString(R.string.Display_Common_Speed_GPS));
            } else {
                value.setTitle(context.getString(R.string.Display_Common_Speed_Sensor));
            }
            value.setValue(StringUtil.format("%.01f", sensor.speedKmPerHour));
            value.setBarColorARGB(mZoneColor.getColor(sensor.zone));
            value.setZoneText(mZoneTitles[sensor.zone.ordinal()]);

            data.setValue(value);
            mSession.getDisplayExtension().setValue(data);
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
