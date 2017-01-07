package com.eaglesakura.andriders.plugin.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.CentralDataHandler;
import com.eaglesakura.andriders.central.data.base.BaseCalculator;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.plugin.display.LineValue;
import com.eaglesakura.andriders.sensor.InclinationType;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.android.margarine.BindStringArray;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * 坂道情報
 */
public class HillDisplaySender extends DisplayDataSender {
    public static final String DISPLAY_ID = "HILL_INFO";

    Float mInclinationPercent;

    InclinationType mInclinationType;

    @BindStringArray(R.array.Ace_Word_HillInclination)
    String[] mHillInclinationNames;

    public HillDisplaySender(@NonNull PluginConnection session) {
        super(session);
    }

    public HillDisplaySender bind() {
        if (mDataReceiver != null) {
            mDataReceiver.addHandler(mDataHandler);
        }
        MargarineKnife.bind(this, this);
        return this;
    }

    @Override
    public void onUpdate(double deltaSec) {
        Context context = getContext();

        DisplayData data = new DisplayData(context, DISPLAY_ID);
        LineValue value = new LineValue(3);

        String inclinationPercent = context.getString(R.string.Word_Display_NoData);
        String inclinationType = inclinationPercent;

        if (mInclinationPercent != null) {
            inclinationPercent = StringUtil.format("%.1f %%", mInclinationPercent);
        }
        if (mInclinationType != null) {
            inclinationType = mHillInclinationNames[mInclinationType.ordinal()];
        }

        value.setLine(0, "傾斜", "");
        value.setLine(1, "現在斜度", inclinationPercent);
        value.setLine(2, "傾斜種別", inclinationType);

        data.setValue(value);
        mSession.getDisplay().setValue(data);
    }


    private CentralDataHandler mDataHandler = new CentralDataHandler() {
        @Override
        public void onReceived(RawCentralData newData) {
            if (newData.sensor.location != null && (System.currentTimeMillis() - newData.sensor.location.date) < BaseCalculator.DATA_TIMEOUT_MS) {
                // タイムアウトしてないなら、データを保持する
                mInclinationPercent = newData.sensor.location.inclinationPercent;
                mInclinationType = newData.sensor.location.inclinationType;
            } else {
                // タイムアウトしているので、データをリセットする
                mInclinationPercent = null;
                mInclinationType = null;
            }
        }
    };

    public static DisplayKey newInformation(Context context) {
        DisplayKey result = new DisplayKey(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Title_Display_HillInfo));
        result.setSummary(context.getString(R.string.Message_Display_HillInfoSummary));
        return result;
    }
}
