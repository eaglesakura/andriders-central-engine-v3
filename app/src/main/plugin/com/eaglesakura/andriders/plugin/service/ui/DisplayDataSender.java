package com.eaglesakura.andriders.plugin.service.ui;

import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;

import android.content.Context;
import android.support.annotation.NonNull;

public abstract class DisplayDataSender {
    @NonNull
    protected final CentralDataReceiver mDataReceiver;

    @NonNull
    protected final PluginConnection mSession;

    public DisplayDataSender(@NonNull PluginConnection connection) {
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
