package com.eaglesakura.andriders.computer.central.data.session;

import com.eaglesakura.andriders.computer.central.data.CycleClock;
import com.eaglesakura.andriders.computer.central.data.base.BaseCalculator;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * セッション情報を構築する
 */
public class SessionData extends BaseCalculator {
    private final String mSessionId;

    private final long mStartDate;

    private static final SimpleDateFormat SESSION_KEY_FORMAT = new SimpleDateFormat("yyyyMMdd.HH.mm.ss.SS");


    /**
     * セッションを生成する
     *
     * @param startDate 開始時刻
     */
    public SessionData(CycleClock clock, long startDate) {
        super(clock);
        mStartDate = startDate;
        mSessionId = String.format("ssn.%s", SESSION_KEY_FORMAT.format(new Date(startDate)));
    }

    /**
     * セッション情報
     */
    public String getSessionId() {
        return mSessionId;
    }
}
