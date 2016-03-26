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

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * 心拍更新を行う
 */
public class HeartrateDisplayUpdater extends DisplayUpdater {
    public static final String DISPLAY_ID = "DISPLAY_ID_HEARTRATE";

    @NonNull
    private final ZoneColor mZoneColor;

    @BindStringArray(R.array.Display_Heartrate_ZoneName)
    @NonNull
    String[] mZoneTitles;

    public HeartrateDisplayUpdater(@NonNull ExtensionSession session, @NonNull ZoneColor zoneColor) {
        super(session);
        mZoneColor = zoneColor;

        MargarineKnife.bind(this, this);
    }

    public void bind() {
        if (mDataReceiver != null) {
            mDataReceiver.addHandler(mHeartrateHandler);
        }
    }


    private SensorDataReceiver.HeartrateHandler mHeartrateHandler = new SensorDataReceiver.HeartrateHandler() {
        @Override
        public void onReceived(@NonNull RawCentralData master, @NonNull RawSensorData.RawHeartrate sensor) {
            Context context = getContext();

            DisplayData data = new DisplayData(context, DISPLAY_ID);
            data.setTimeoutMs(1000 * 5);   // 5秒心拍がなければ切断されている

            BasicValue value = new BasicValue();
            value.setTitle(context.getString(R.string.Display_Common_Heartrate));
            value.setValue(String.valueOf(sensor.bpm));
            value.setBarColorARGB(mZoneColor.getColor(sensor.zone));
            value.setZoneText(mZoneTitles[sensor.zone.ordinal()]);

            data.setValue(value);
            mSession.getDisplayExtension().setValue(data);
        }

        @Override
        public void onDisconnectedSensor(@NonNull RawCentralData master) {
            Context context = getContext();

            DisplayData data = new DisplayData(context, DISPLAY_ID);
            data.setTimeoutMs(1000 * 60);   // 5秒心拍がなければ切断されている

            BasicValue value = new BasicValue();
            value.setTitle(context.getString(R.string.Display_Common_Heartrate));
            value.setValue(context.getString(R.string.Display_Heartrate_Reconnect));
            value.setBarColorARGB(0x00);

            data.setValue(value);
            mSession.getDisplayExtension().setValue(data);
        }
    };

    public static DisplayInformation newInformation(Context context) {
        DisplayInformation result = new DisplayInformation(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Display_Common_Heartrate));
        return result;
    }
}
