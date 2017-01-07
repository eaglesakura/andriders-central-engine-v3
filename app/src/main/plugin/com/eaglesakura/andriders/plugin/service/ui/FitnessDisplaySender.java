package com.eaglesakura.andriders.plugin.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.CentralDataHandler;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.plugin.display.LineValue;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSessionData;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * フィットネス情報を送信する
 */
public class FitnessDisplaySender extends DisplayDataSender {
    public static final String DISPLAY_ID = "FITNESS_ALL";

    RawSessionData.RawFitnessStatus mSessionFitnessStatus;

    RawSessionData.RawFitnessStatus mTodayFitnessStatus;

    public FitnessDisplaySender(@NonNull PluginConnection session) {
        super(session);
    }

    public FitnessDisplaySender bind() {
        if (mDataReceiver != null) {
            mDataReceiver.addHandler(mDataHandler);
        }
        return this;
    }

    @Override
    public void onUpdate(double deltaSec) {
        if (mTodayFitnessStatus == null || mSessionFitnessStatus == null) {
            return;
        }

        Context context = getContext();

        DisplayData data = new DisplayData(context, DISPLAY_ID);
        LineValue value = new LineValue(3);

        // 最高速度
        value.setLine(0, "カロリー消費", "");
        value.setLine(1, "今日合計", StringUtil.format("%.1f kcal", mTodayFitnessStatus.calorie));
        value.setLine(2, "セッション", StringUtil.format("%.1f kcal", mSessionFitnessStatus.calorie));

        data.setValue(value);
        mSession.getDisplay().setValue(data);
    }


    private CentralDataHandler mDataHandler = new CentralDataHandler() {
        @Override
        public void onReceived(RawCentralData newData) {
            mTodayFitnessStatus = newData.today.fitness;
            mSessionFitnessStatus = newData.session.fitness;
        }
    };

    public static DisplayKey newInformation(Context context) {
        DisplayKey result = new DisplayKey(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Title_Display_FitnessCalories));
        result.setSummary(context.getString(R.string.Message_Display_FitnessCaloriesSummary));
        return result;
    }
}
