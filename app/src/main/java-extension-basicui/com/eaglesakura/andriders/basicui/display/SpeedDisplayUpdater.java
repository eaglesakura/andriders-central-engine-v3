package com.eaglesakura.andriders.basicui.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.SensorDataReceiver;
import com.eaglesakura.andriders.display.ZoneColor;
import com.eaglesakura.andriders.extension.DisplayInformation;
import com.eaglesakura.andriders.extension.ExtensionSession;
import com.eaglesakura.andriders.extension.display.BasicValue;
import com.eaglesakura.andriders.extension.display.DisplayData;
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
public class SpeedDisplayUpdater extends DisplayUpdater {
    public static final String DISPLAY_ID = "DISPLAY_ID_SPEED";

    @NonNull
    private final ZoneColor mZoneColor;

    @BindStringArray(R.array.Display_Cadence_ZoneName)
    @NonNull
    String[] mZoneTitles;

    public SpeedDisplayUpdater(@NonNull ExtensionSession session, @NonNull ZoneColor zoneColor) {
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
            value.setTitle(context.getString(R.string.Display_Common_Speed));
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

    public static DisplayInformation newInformation(Context context) {
        DisplayInformation result = new DisplayInformation(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Display_Common_Speed));
        return result;
    }
}
