package com.eaglesakura.andriders.central.service;

import com.eaglesakura.andriders.serialize.RawCentralData;

/**
 * 最新のセッション情報
 */
public class SessionData {
    RawCentralData mLatestData;

    CentralSession mSession;

    public SessionData(RawCentralData latestData, CentralSession session) {
        mLatestData = latestData;
        mSession = session;
    }

    public RawCentralData getLatestData() {
        return mLatestData;
    }

    public CentralSession getSession() {
        return mSession;
    }
}
