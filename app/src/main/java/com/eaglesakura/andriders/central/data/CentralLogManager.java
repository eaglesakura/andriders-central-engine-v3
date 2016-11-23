package com.eaglesakura.andriders.central.data;

import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.provider.AppDatabaseProvider;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Initializer;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.util.DateUtil;
import com.eaglesakura.util.Timer;

import org.greenrobot.greendao.annotation.NotNull;

import android.content.Context;

import java.util.Date;
import java.util.TimeZone;

/**
 * ログを管理する
 */
public class CentralLogManager {

    Context mContext;

    @Inject(AppDatabaseProvider.class)
    SessionLogDatabase mSessionLogDatabase;

    TimeZone mTimeZone = TimeZone.getDefault();

    public CentralLogManager(@NotNull Context context) {
        mContext = context;
    }

    @Initializer
    public void initialize() {
        Garnet.create(this)
                .depend(Context.class, mContext)
                .inject();
    }

    SessionLogDatabase open() {
        return mSessionLogDatabase.openWritable(SessionLogDatabase.class);
    }

    /**
     * 指定した日の記録を生成する
     */
    public LogStatistics loadTodayStatistics(long now) throws AppException {
        try (SessionLogDatabase db = open()) {
            Date dateStart = DateUtil.getDateStart(new Date(now), mTimeZone);
            return db.loadTotal(dateStart.getTime(), dateStart.getTime() + Timer.toMilliSec(1, 0, 0, 0, 0) - 1);
        }
    }

    /**
     * 全ての期間の記録を生成する
     */
    public LogStatistics loadAllStatistics() throws AppException {
        try (SessionLogDatabase db = open()) {
            return db.loadTotal(0, 0);
        }
    }
}
