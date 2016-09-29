package com.eaglesakura.andriders.model;

import android.support.annotation.NonNull;

/**
 *
 */
public class DaoModel<T> {
    @NonNull
    protected final T mRaw;

    protected DaoModel(@NonNull T raw) {
        mRaw = raw;
    }

    @NonNull
    public T getRaw() {
        return mRaw;
    }
}
