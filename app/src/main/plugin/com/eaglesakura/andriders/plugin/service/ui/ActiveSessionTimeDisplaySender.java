package com.eaglesakura.andriders.plugin.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.CentralDataHandler;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.plugin.display.LineValue;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.util.AppUtil;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * 走行時間表示
 */
public class ActiveSessionTimeDisplaySender extends DisplayDataSender {
    public static final String DISPLAY_ID = "ACTIVE_SESSION_TIME_TODAY_SESSION";

    private Integer mTodayTime;

    private Integer mSessionTime;

    public ActiveSessionTimeDisplaySender(@NonNull PluginConnection session) {
        super(session);
    }

    public ActiveSessionTimeDisplaySender bind() {
        if (mDataReceiver != null) {
            mDataReceiver.addHandler(mDataHandler);
        }
        return this;
    }

    @Override
    public void onUpdate(double deltaSec) {
        if (mTodayTime == null || mSessionTime == null) {
            return;
        }

        Context context = getContext();

        DisplayData data = new DisplayData(context, DISPLAY_ID);
        LineValue value = new LineValue(3);

        // 最高速度
        value.setLine(0, "自走時間", "");
        value.setLine(1, "今日", AppUtil.formatTimeMilliSecToString(mTodayTime));
        value.setLine(2, "セッション", AppUtil.formatTimeMilliSecToString(mSessionTime));

        data.setValue(value);
        mSession.getDisplay().setValue(data);
    }


    private CentralDataHandler mDataHandler = new CentralDataHandler() {
        @Override
        public void onReceived(RawCentralData newData) {
            mTodayTime = newData.today.activeTimeMs;
            mSessionTime = newData.session.activeTimeMs;
        }
    };

    public static DisplayKey newInformation(Context context) {
        DisplayKey result = new DisplayKey(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Title_Display_ActiveDuration));
        result.setSummary(context.getString(R.string.Message_Display_ActiveDurationSummary));
        return result;
    }
}
