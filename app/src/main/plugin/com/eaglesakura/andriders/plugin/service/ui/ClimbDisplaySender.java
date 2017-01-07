package com.eaglesakura.andriders.plugin.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.CentralDataHandler;
import com.eaglesakura.andriders.central.data.base.BaseCalculator;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.plugin.display.LineValue;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * ヒルクライム情報
 */
public class ClimbDisplaySender extends DisplayDataSender {
    public static final String DISPLAY_ID = "HILL_CLIMB";

    Integer mClimbMeterSession;

    Integer mClimbMeterToday;

    Integer mAltitudeMeter;

    public ClimbDisplaySender(@NonNull PluginConnection session) {
        super(session);
    }

    public ClimbDisplaySender bind() {
        if (mDataReceiver != null) {
            mDataReceiver.addHandler(mDataHandler);
        }
        return this;
    }

    @Override
    public void onUpdate(double deltaSec) {
        Context context = getContext();

        DisplayData data = new DisplayData(context, DISPLAY_ID);
        LineValue value = new LineValue(3);

        String climbValueSession = context.getString(R.string.Word_Display_NoData);
        String climbValueToday = climbValueSession;
        String altitudeValue = climbValueSession;

        if (mClimbMeterSession != null) {
            climbValueSession = StringUtil.format("%d m", mClimbMeterSession);
        }
        if (mClimbMeterToday != null) {
            climbValueToday = StringUtil.format("%d m", mClimbMeterToday);
        }

        if (mAltitudeMeter != null) {
            altitudeValue = StringUtil.format("%d m", mAltitudeMeter);
        }

        value.setLine(0, "現在標高", altitudeValue);
        value.setLine(1, "今日獲得標高", climbValueToday);
        value.setLine(2, "セッション獲得標高", climbValueSession);

        data.setValue(value);
        mSession.getDisplay().setValue(data);
    }


    private CentralDataHandler mDataHandler = new CentralDataHandler() {
        @Override
        public void onReceived(RawCentralData newData) {
            mClimbMeterToday = (int) newData.today.sumAltitudeMeter;
            mClimbMeterSession = (int) newData.session.sumAltitudeMeter;
            if (newData.sensor.location != null && (System.currentTimeMillis() - newData.sensor.location.date) < BaseCalculator.DATA_TIMEOUT_MS) {
                mAltitudeMeter = (int) newData.sensor.location.altitude;
            } else {
                mAltitudeMeter = null;
            }
        }
    };

    public static DisplayKey newInformation(Context context) {
        DisplayKey result = new DisplayKey(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Title_Display_Climb));
        result.setSummary(context.getString(R.string.Message_Display_ClimbSummary));
        return result;
    }
}
