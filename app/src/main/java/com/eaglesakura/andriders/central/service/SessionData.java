package com.eaglesakura.andriders.central.service;

import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.android.framework.delegate.task.DataBus;

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

    public static class Bus extends DataBus<SessionData> {
        public RawCentralData getLatestData() {
            return getData().getLatestData();
        }

        public CentralSession getSession() {
            return getData().getSession();
        }
    }
}
