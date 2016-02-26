package com.eaglesakura.andriders.computer.central.status;

import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.internal.protocol.RawSessionData;
import com.eaglesakura.util.StringUtil;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ACEのステータス管理を行う
 */
public class SessionStatusCentral implements CentralDataManager.ICentral {
    final Context context;

    private static final SimpleDateFormat SESSION_KEY_FORMAT = new SimpleDateFormat("yyyyMMdd.HH.mm.ss.SS");

    private String mSessionId;

    /**
     * セッション開始時刻
     */
    private Date mSessionStartTime;

    public SessionStatusCentral(Context context) {
        this.context = context;
    }

    public void onSessionStart() {
        mSessionStartTime = new Date();
        mSessionId = String.format("ssn.%s", SESSION_KEY_FORMAT.format(mSessionStartTime));
    }

    public void onSessionFinished() {

    }

    @Override
    public void onUpdate(CentralDataManager parent, long diffTime) {

    }

    @Override
    public void buildData(CentralDataManager parent, RawCentralData result) {
        if (StringUtil.isEmpty(mSessionId)) {
            throw new IllegalStateException("SessionId is null");
        }
        result.session.sessionId = mSessionId;

        // ユーザーが自走中の場合はフラグを立てる
        if (parent.isActiveMoving()) {
            result.session.flags |= RawSessionData.FLAG_ACTIVE;
        }
    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}
