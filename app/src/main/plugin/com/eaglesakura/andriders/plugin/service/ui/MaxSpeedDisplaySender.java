package com.eaglesakura.andriders.plugin.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.CentralDataHandler;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.plugin.display.LineValue;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawRecord;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * 最高速度更新を行う
 *
 * 表示は「最高速」「今日最高速」「セッション最高速」でそれぞれ表示する
 */
public class MaxSpeedDisplaySender extends DisplayDataSender {
    public static final String DISPLAY_ID = "MAX_SPEED";

    RawRecord mRecord;

    public MaxSpeedDisplaySender(@NonNull PluginConnection session) {
        super(session);
    }

    public MaxSpeedDisplaySender bind() {
        if (mDataReceiver != null) {
            mDataReceiver.addHandler(mDataHandler);
        }
        return this;
    }

    String getDisplayText(double speed) {
        if (speed < 20.0) {
            return "-";
        } else {
            return StringUtil.format("%.1f km/h", mRecord.maxSpeedKmh);
        }
    }

    @Override
    public void onUpdate(double deltaSec) {
        if (mRecord == null) {
            return;
        }

        Context context = getContext();

        DisplayData data = new DisplayData(context, DISPLAY_ID);
        LineValue value = new LineValue(3);

        // 最高速度
        value.setLine(0, "自己最速", getDisplayText(mRecord.maxSpeedKmh));
        value.setLine(1, "今日最速", getDisplayText(mRecord.maxSpeedKmhToday));
        value.setLine(2, "セッション最速", getDisplayText(mRecord.maxSpeedKmhSession));

        data.setValue(value);
        mSession.getDisplay().setValue(data);
    }

    private CentralDataHandler mDataHandler = new CentralDataHandler() {
        @Override
        public void onReceived(RawCentralData newData) {
            mRecord = newData.record;
        }
    };

    public static DisplayKey newInformation(Context context) {
        DisplayKey result = new DisplayKey(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Word_Display_Speed_Max));
        result.setSummary(context.getString(R.string.Message_Display_MaxSpeedSummary));
        return result;
    }
}
