package com.eaglesakura.andriders.plugin.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.CentralDataHandler;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.plugin.display.LineValue;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * セッションの走行時間・距離を表示する
 */
public class SessionDistanceTimeClimbDisplaySender extends DisplayDataSender {
    public static final String DISPLAY_ID = "SESSION_DURATION_DISTANCE_CLIMB";

    private Integer mSessionTime;

    private Float mSessionDistanceKm;

    private Float mSessionClimbMeter;

    public SessionDistanceTimeClimbDisplaySender(@NonNull PluginConnection session) {
        super(session);
    }

    public SessionDistanceTimeClimbDisplaySender bind() {
        if (mDataReceiver != null) {
            mDataReceiver.addHandler(mDataHandler);
        }
        return this;
    }

    @Override
    public void onUpdate(double deltaSec) {
        if (mSessionDistanceKm == null || mSessionTime == null || mSessionClimbMeter == null) {
            return;
        }

        Context context = getContext();

        DisplayData data = new DisplayData(context, DISPLAY_ID);
        LineValue value = new LineValue(3);

        // 最高速度
        value.setLine(0, "セッション", "");
        value.setLine(1, "経過時間", AppUtil.formatTimeMilliSecToString(mSessionTime));
        value.setLine(2, "走行距離", StringUtil.format("%.1f km", mSessionDistanceKm));

        data.setValue(value);
        mSession.getDisplay().setValue(data);
    }


    private CentralDataHandler mDataHandler = new CentralDataHandler() {
        @Override
        public void onReceived(RawCentralData newData) {
            mSessionTime = newData.session.durationTimeMs;
            mSessionDistanceKm = newData.session.distanceKm;
            mSessionClimbMeter = newData.session.sumAltitudeMeter;
        }
    };

    public static DisplayKey newInformation(Context context) {
        DisplayKey result = new DisplayKey(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Title_Display_SessionTimeDistance));
        result.setSummary(context.getString(R.string.Message_Display_SessionTimeDistanceSummary));
        return result;
    }
}
