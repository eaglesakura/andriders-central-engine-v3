package com.eaglesakura.andriders.central.data.log;

import com.eaglesakura.util.DateUtil;

import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * セッションログのロード用ヘッダ情報
 */
public class SessionHeader {
    /**
     * セッション管理ID
     */
    final long mSessionId;

    /**
     * セッション終了時刻
     */
    final Date mEndDate;

    /**
     * 走行日管理ID
     *
     * YYYYMMDDを使用する
     */
    final long mDateId;

    public SessionHeader(long sessionId, long endDate) {
        mSessionId = sessionId;
        mEndDate = new Date(endDate);
        mDateId = toDateId(sessionId);
    }

    /**
     * セッションIDを日付のIDに変換する
     */
    public static long toDateId(long sessionId) {
        Date date = new Date(sessionId);
        TimeZone timeZone = TimeZone.getDefault();
        return (long) DateUtil.getYear(date, timeZone) * 10000L +
                (long) DateUtil.getMonth(date, timeZone) * 100L +
                (long) DateUtil.getDay(date, timeZone);
    }


    public long getSessionId() {
        return mSessionId;
    }

    /**
     * セッション終了時刻を取得する
     */
    public Date getEndDate() {
        return mEndDate;
    }

    /**
     * 同一日付であることを保証するIDを取得する
     */
    public long getDateId() {
        return mDateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionHeader that = (SessionHeader) o;

        return mSessionId == that.mSessionId;

    }

    @Override
    public int hashCode() {
        return (int) (mSessionId ^ (mSessionId >>> 32));
    }

    public static final Comparator<SessionHeader> COMPARATOR_ASC = (a, b) -> Long.compare(a.getSessionId(), b.getSessionId());
}
