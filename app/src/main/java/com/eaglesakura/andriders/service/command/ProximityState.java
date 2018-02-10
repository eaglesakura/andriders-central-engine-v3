package com.eaglesakura.andriders.service.command;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * 近接コマンド状態
 */
public class ProximityState {

    @NonNull
    private final Date mDate;

    private final boolean mProximityState;

    /**
     * @param date           時刻情報
     * @param proximityState 近接状態でああればtrue
     */
    public ProximityState(@NonNull Date date, boolean proximityState) {
        mDate = date;
        mProximityState = proximityState;
    }

    @NonNull
    public Date getDate() {
        return mDate;
    }

    /**
     * 手が近接状態であるならtrue
     */
    public boolean isProximity() {
        return mProximityState;
    }

}
