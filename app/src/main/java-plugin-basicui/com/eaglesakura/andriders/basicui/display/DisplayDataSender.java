package com.eaglesakura.andriders.basicui.display;

import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.plugin.CentralEngineConnection;

import android.content.Context;
import android.support.annotation.NonNull;

public abstract class DisplayDataSender {
    @NonNull
    protected final CentralDataReceiver mDataReceiver;

    @NonNull
    protected final CentralEngineConnection mSession;

    public DisplayDataSender(@NonNull CentralEngineConnection connection) {
        mSession = connection;
        if (mSession.isAcesSession()) {
            mDataReceiver = mSession.getCentralDataReceiver();
        } else {
            mDataReceiver = null;
        }
    }

    public void onUpdate(double deltaSec) {

    }

    public Context getContext() {
        return mSession.getContext();
    }
}
