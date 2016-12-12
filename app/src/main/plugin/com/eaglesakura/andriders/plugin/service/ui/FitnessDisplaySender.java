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
 * 最高速度更新を行う
 *
 * 表示は「最高速」「今日最高速」「セッション最高速」でそれぞれ表示する
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
        LineValue value = new LineValue(2);

        // 最高速度
        value.setLine(0, "今日消費", StringUtil.format("%.1f kcal", mTodayFitnessStatus.calorie));
        value.setLine(1, "セッション消費", StringUtil.format("%.1f kcal", mSessionFitnessStatus.calorie));

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
        result.setTitle(context.getString(R.string.Word_Display_FitnessCalories_Title));
        result.setSummary(context.getString(R.string.Message_Display_FitnessCaloriesSummary));
        return result;
    }
}
