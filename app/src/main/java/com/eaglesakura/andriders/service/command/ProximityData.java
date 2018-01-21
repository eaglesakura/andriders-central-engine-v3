package com.eaglesakura.andriders.service.command;

import com.eaglesakura.sloth.data.DataBus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * 近接コマンド状態
 */
public class ProximityData {

    @NonNull
    private final Date mDate;

    private final boolean mProximityState;

    public ProximityData(@NonNull Date date, boolean proximityState) {
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

    public static class Bus extends DataBus<ProximityData> {

        public Bus(@Nullable ProximityData data) {
            super(data);
        }

        @NonNull
        public Date getDate() {
            return getData().getDate();
        }

        public boolean isProximity() {
            return getData().isProximity();
        }
    }
}
