package com.eaglesakura.andriders.plugin.service.ui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;
import com.eaglesakura.andriders.plugin.display.BasicValue;
import com.eaglesakura.andriders.plugin.display.DisplayData;

import android.content.Context;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 時刻情報
 */
public class TimeDisplaySender extends DisplayDataSender {
    public static final String DISPLAY_ID = "TIME_NOW";

    private static final SimpleDateFormat DEFAULT_FORMATTER = new SimpleDateFormat("HH:mm.ss");

    public TimeDisplaySender(@NonNull PluginConnection session) {
        super(session);
    }

    public TimeDisplaySender bind() {
        return this;
    }

    @Override
    public void onUpdate(double deltaSec) {
        Context context = getContext();

        DisplayData data = new DisplayData(context, DISPLAY_ID);
        BasicValue value = new BasicValue();


        // 最高速度
        value.setTitle("現在時刻");
        value.setValue(DEFAULT_FORMATTER.format(new Date()));

        data.setValue(value);
        mSession.getDisplay().setValue(data);
    }

    public static DisplayKey newInformation(Context context) {
        DisplayKey result = new DisplayKey(context, DISPLAY_ID);
        result.setTitle(context.getString(R.string.Title_Display_NowTime));
        result.setSummary(context.getString(R.string.Message_Display_NowTimeSummary));
        return result;
    }
}
