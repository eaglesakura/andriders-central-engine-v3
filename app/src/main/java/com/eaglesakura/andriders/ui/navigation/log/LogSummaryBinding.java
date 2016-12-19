package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;

import java.text.SimpleDateFormat;

/**
 * ログの概要表記を行う
 */
public class LogSummaryBinding {

    private Context mContext;

    private LogStatistics mLogStatistics;

    public LogSummaryBinding(Context context, LogStatistics logStatistics) {
        mContext = context;
        mLogStatistics = logStatistics;
    }

    private String getLoadingText() {
        return mContext.getString(R.string.Word_Common_DataLoad);
    }

    private static final SimpleDateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private static final SimpleDateFormat DEFAULT_DAY_FORMATTER = new SimpleDateFormat("yyyy/MM/dd");

    public String getSessionDayText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }
        return DEFAULT_DAY_FORMATTER.format(mLogStatistics.getStartDate());
    }

    public String getStartDateText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }
        return DEFAULT_DATE_FORMATTER.format(mLogStatistics.getStartDate());
    }

    public String getEndDateText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }
        return DEFAULT_DATE_FORMATTER.format(mLogStatistics.getEndDate());
    }

    /**
     * 開始-終了までの時間表示を取得する
     */
    public String getTimeText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }
        return AppUtil.formatTimeMilliSecToString(mLogStatistics.getEndDate().getTime() - mLogStatistics.getStartDate().getTime());
    }

    public String getSessionCountText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }
        return String.valueOf(mLogStatistics.getSessionCount());
    }

    public String getLongestDateDistanceText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }
        return StringUtil.format("%.1f km", mLogStatistics.getLongestDateDistanceKm());
    }

    public String getMaxDateAltitudeText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }
        return StringUtil.format("%d m", (int) mLogStatistics.getMaxDateAltitudeMeter());
    }

    public String getSumAltitudeMeterText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }
        return StringUtil.format("%d m", (int) mLogStatistics.getSumAltitudeMeter());
    }

    public String getActiveTimeText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }

        return AppUtil.formatTimeMilliSecToString(mLogStatistics.getActiveTimeMs());
    }

    public String getMaxSpeedKmhText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }

        return StringUtil.format("%.1f km/h", mLogStatistics.getMaxSpeedKmh());
    }

    public String getActiveDistanceKmText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }

        return StringUtil.format("%.1f km", mLogStatistics.getActiveDistanceKm());
    }

    public String getSumDistanceKmText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }

        return StringUtil.format("%.1f km", mLogStatistics.getSumDistanceKm());
    }

    public String getCaloriesText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }

        return StringUtil.format("%d kcal", (int) mLogStatistics.getCalories());
    }

    public String getExerciseText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }

        return StringUtil.format("%.1f Ex", mLogStatistics.getExercise());
    }

    public String getMaxCadenceText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }

        return StringUtil.format("%d RPM", mLogStatistics.getMaxCadence());
    }

    public String getMaxHeartrateText() {
        if (mLogStatistics == null) {
            return getLoadingText();
        }

        return StringUtil.format("%d BPM", mLogStatistics.getMaxHeartrate());
    }
}
