package com.eaglesakura.andriders.central.data.log;

import java.util.ArrayList;
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
}
