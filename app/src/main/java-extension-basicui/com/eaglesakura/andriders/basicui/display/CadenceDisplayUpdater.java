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
 * ケイデンス更新を行う
 */
public class CadenceDisplayUpdater extends DisplayUpdater {
    public static final String DISPLAY_ID = "DISPLAY_ID_CADENCE";

    @NonNull
    private final ZoneColor mZoneColor;

    @BindStringArray(R.array.Display_Cadence_ZoneName)
    @NonNull
    String[] mZoneTitles;

    public CadenceDisplayUpdater(@NonNull ExtensionSession session, @NonNull ZoneColor zoneColor) {
        super(session);
        mZoneColor = zoneColor;

        MargarineKnife.bind(this, this);
    }

    public void bind() {
        if (mDataReceiver != null) {
            mDataReceiver.addHandler(mCadenceHandler);
        }
    }

    private SensorDataReceiver.CadenceHandler mCadenceHandler = new SensorDataReceiver.CadenceHandler() {
        @Override
        public void onReceived(@NonNull RawCentralData master, @NonNull RawSensorData.RawCadence sensor) {
            Context context = getContext();

            DisplayData data = new DisplayData(context, DISPLAY_ID);

            BasicValue value = new BasicValue();
            value.setTitle(context.getString(R.string.Display_Common_Cadence));
            value.setValue(String.valueOf(sensor.rpm));
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
            value.setTitle(context.getString(R.string.Display_Common_Cadence));
            value.setValue(context.getString(R.string.Display_Common_Reconnect));
            value.setBarColorARGB(0x00);

            data.setValue(value);
            mSession.getDisplayExtension().setValue(data);
        }
    };

    public static DisplayInformation newInformation(Context context) {
        DisplayInformation result = new DisplayInformation(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Display_Common_Cadence));
        return result;
    }
}
