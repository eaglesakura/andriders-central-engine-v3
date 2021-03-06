package com.eaglesakura.andriders.central.data.log;

import com.eaglesakura.collection.DataCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * セッション情報の一覧構成
 */
public class SessionHeaderCollection extends DataCollection<SessionHeader> {
    public SessionHeaderCollection(List<SessionHeader> dataList) {
        super(dataList);
    }

    /**
     * セッションを日付単位で分解して列挙する。
     */
    public DataCollection<DateSessions> listSessionDates() {
        List<DateSessions> sessions = new ArrayList<>();
        DateSessions current = null;
        for (SessionHeader header : list()) {
            if (current == null || !current.add(header)) {
                // 新しいグループを追加する
                current = new DateSessions(header.getDateId());
                sessions.add(current);
                current.add(header);
            }
        }

        DataCollection<DateSessions> result = new DataCollection<>(sessions);
        result.setComparator(DateSessions.COMPARATOR_DESC);
        return result;
    }
}
