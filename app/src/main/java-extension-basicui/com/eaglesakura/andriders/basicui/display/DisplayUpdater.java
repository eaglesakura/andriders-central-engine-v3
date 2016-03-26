package com.eaglesakura.andriders.basicui.display;

import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.extension.ExtensionSession;

import android.content.Context;
import android.support.annotation.NonNull;

public abstract class DisplayUpdater {
    @NonNull
    protected final CentralDataReceiver mDataReceiver;

    @NonNull
    protected final ExtensionSession mSession;

    public DisplayUpdater(@NonNull ExtensionSession session) {
        mSession = session;
        if (mSession.isAcesSession()) {
            mDataReceiver = mSession.getCentralDataReceiver();
        } else {
            mDataReceiver = null;
        }
    }

    public Context getContext() {
        return mSession.getContext();
    }
}
