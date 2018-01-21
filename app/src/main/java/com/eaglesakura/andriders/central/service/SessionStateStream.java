package com.eaglesakura.andriders.central.service;

import com.eaglesakura.sloth.app.lifecycle.SlothLiveData;

/**
 * 現在のセッション状態を通知するストリーム
 */
public class SessionStateStream extends SlothLiveData<SessionState> {

    private CentralSession mSession;

    SessionStateStream(CentralSession session) {
        mSession = session;
        initValue(new SessionState(SessionState.State.Initializing));
    }

    public CentralSession getSession() {
        return mSession;
    }

    void onUpdate(SessionState state) {
        syncValue(state, false);
    }

    public boolean is(SessionState.State state) {
        return getValueOrThrow().getState() == state;
    }

}
