package com.eaglesakura.andriders.data.backup.serialize;

import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.serialize.RawCentralData;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import java.util.List;

/**
 * 1セッションのログを書き出したバックアップ情報
 */
@Keep
public class SessionBackup {

    /**
     * ログから書き出されたCentral情報
     */
    @NonNull
    @Size(min = 1)
    public List<RawCentralData> points;

    /**
     * セッション管理IDを取得する
     */
    public static long getSessionId(SessionBackup backup) {
        return backup.points.get(0).session.sessionId;
    }

    public static SessionBackup newInstance(List<RawCentralData> dataList) {
        SessionBackup result = new SessionBackup();
        result.points = dataList;
        return result;
    }

    public static SessionHeader getSessionHeader(SessionBackup backup) {
        return new SessionHeader(backup.points.get(backup.points.size() - 1));
    }
}
