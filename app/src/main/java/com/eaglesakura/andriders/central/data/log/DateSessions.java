package com.eaglesakura.andriders.central.data.log;

import com.eaglesakura.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 1日の走行をグルーピングする
 */
public class DateSessions {
    List<SessionHeader> mHeaders = new ArrayList<>();

    long mDateId;

    public DateSessions(long dateId) {
        mDateId = dateId;
    }

    /**
     * その日の初セッション開始時刻を取得する
     */
    public long getStartTime() {
        return mHeaders.get(0).getSessionId();
    }

    public long getDateId() {
        return mDateId;
    }

    /**
     * 同じ日の走行であればデータを追加してtrueを返却する。
     *
     * それ以外はfalseを返却する。
     */
    public boolean add(SessionHeader header) {
        if (mDateId == header.getDateId()) {
            mHeaders.add(header);
            return true;
        } else {
            return false;
        }
    }

    public int size() {
        return mHeaders.size();
    }

    public boolean isEmpty() {
        return mHeaders.isEmpty();
    }

    public boolean remove(long sessionId) {
        return CollectionUtil.safeEachRemove(mHeaders, header -> header.getSessionId() == sessionId) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateSessions that = (DateSessions) o;

        return mDateId == that.mDateId;

    }

    @Override
    public int hashCode() {
        return (int) (mDateId ^ (mDateId >>> 32));
    }

    public static final Comparator<DateSessions> COMPARATOR_DESC = (a, b) -> Long.compare(b.mDateId, a.mDateId);
}
