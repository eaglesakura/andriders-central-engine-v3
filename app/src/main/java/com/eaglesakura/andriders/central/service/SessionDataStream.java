package com.eaglesakura.andriders.central.service;

import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.sloth.app.lifecycle.SlothLiveData;

/**
 * CentralDataの更新を受け取るStream
 */
public class SessionDataStream extends SlothLiveData<RawCentralData> {

    /**
     * 同期対象のセッション
     */
    private CentralSession mSession;

    SessionDataStream(CentralSession session) {
        mSession = session;
    }

    /**
     * 元データを取得する
     */
    public CentralSession getSession() {
        return mSession;
    }

    /**
     * データを更新する
     */
    void onUpdate(RawCentralData data) {
        syncValue(data, false);
    }
}
