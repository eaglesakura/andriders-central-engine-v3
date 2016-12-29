package com.eaglesakura.andriders.plugin.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.CentralDataHandler;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.plugin.display.LineValue;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * 最高速度更新を行う
 *
 * 表示は今日とセッションの走行距離を表示する
 */
public class DistanceDisplaySender extends DisplayDataSender {
    public static final String DISPLAY_ID = "DISTANCE_TODAY_SESSION";

    private Float mTodayDistanceKm;

    private Float mSessionDistanceKm;

    public DistanceDisplaySender(@NonNull PluginConnection session) {
        super(session);
    }

    public DistanceDisplaySender bind() {
        if (mDataReceiver != null) {
            mDataReceiver.addHandler(mDataHandler);
        }
        return this;
    }

    @Override
    public void onUpdate(double deltaSec) {
        if (mTodayDistanceKm == null || mSessionDistanceKm == null) {
            return;
        }

        Context context = getContext();

        DisplayData data = new DisplayData(context, DISPLAY_ID);
        LineValue value = new LineValue(2);

        // 最高速度
        value.setLine(0, "今日走行距離", StringUtil.format("%.1 km", mTodayDistanceKm));
        value.setLine(1, "セッション距離", StringUtil.format("%.1 km", mSessionDistanceKm));

        data.setValue(value);
        mSession.getDisplay().setValue(data);
    }


    private CentralDataHandler mDataHandler = new CentralDataHandler() {
        @Override
        public void onReceived(RawCentralData newData) {
            mTodayDistanceKm = newData.today.distanceKm;
            mSessionDistanceKm = newData.session.distanceKm;
        }
    };

    public static DisplayKey newInformation(Context context) {
        DisplayKey result = new DisplayKey(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Title_Display_Distance));
        result.setSummary(context.getString(R.string.Message_Display_DistanceSummary));
        return result;
    }
}
