package com.eaglesakura.andriders.ui.navigation.session;

import com.eaglesakura.andriders.plugin.connection.SessionControlConnection;
import com.eaglesakura.android.framework.delegate.task.DataBus;

/**
 * セッション制御を保持する
 */
public class SessionControlBus extends DataBus<SessionControlConnection> {

    public interface Holder {
        SessionControlBus getSessionControlBus();
    }
}
