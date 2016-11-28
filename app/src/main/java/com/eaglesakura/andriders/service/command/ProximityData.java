package com.eaglesakura.andriders.service.command;

import com.eaglesakura.android.framework.delegate.task.DataBus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * 近接コマンド状態
 */
public class ProximityData {

    @NonNull
    final Date mDate;

    final boolean mProximityState;

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
