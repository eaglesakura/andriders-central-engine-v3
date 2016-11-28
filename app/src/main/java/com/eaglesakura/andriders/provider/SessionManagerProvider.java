package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.data.notification.CentralNotificationManager;
import com.eaglesakura.android.framework.provider.ContextProvider;
import com.eaglesakura.android.garnet.Depend;
import com.eaglesakura.android.garnet.Provide;

/**
 * セッション関連の依存を解決する
 */
public class SessionManagerProvider extends ContextProvider {
    SessionInfo mSessionInfo;

    CentralSession mCentralSession;

    @Depend
    public void setSessionInfo(SessionInfo info) {
        mSessionInfo = info;
    }

    @Depend
    public void setCentralSession(CentralSession centralSession) {
        mCentralSession = centralSession;
        setSessionInfo(centralSession.getSessionInfo());
    }

    @Provide
    public CentralNotificationManager provideCentralNotificationView() {
        return new CentralNotificationManager(getContext(), mSessionInfo.getSessionClock());
    }
}
