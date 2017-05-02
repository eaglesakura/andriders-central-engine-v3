package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.central.service.CentralSession;
import com.eaglesakura.andriders.data.display.DisplayBindManager;
import com.eaglesakura.andriders.data.notification.CentralNotificationManager;
import com.eaglesakura.andriders.service.command.ProximityFeedbackManager;
import com.eaglesakura.andriders.service.command.ProximitySensorManager;
import com.eaglesakura.android.garnet.Depend;
import com.eaglesakura.android.garnet.Provide;
import com.eaglesakura.sloth.provider.ContextProvider;

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

    @Provide
    public ProximitySensorManager provideProximitySensorManager() {
        return new ProximitySensorManager(getContext());
    }

    @Provide
    public ProximityFeedbackManager provideProximityFeedbackManager() {
        return new ProximityFeedbackManager(getContext());
    }

    @Provide
    public DisplayBindManager provideDisplayBindManager() {
        return new DisplayBindManager(getContext(), mSessionInfo.getSessionClock());
    }
}
