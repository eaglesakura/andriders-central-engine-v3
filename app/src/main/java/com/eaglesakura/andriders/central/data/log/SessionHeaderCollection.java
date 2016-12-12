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
                if (current != null) {
                    // セッション一覧に追加する
                    sessions.add(current);
                }

                // 新しいグループを追加する
                current = new DateSessions(header.getDateId());
                current.add(header);
            }
        }

        return new DataCollection<>(sessions);
    }
}
