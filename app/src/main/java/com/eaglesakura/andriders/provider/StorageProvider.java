package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.db.AppSettings;
import com.eaglesakura.andriders.db.storage.AppStorageManager;
import com.eaglesakura.android.garnet.Depend;
import com.eaglesakura.android.garnet.Provide;
import com.eaglesakura.android.garnet.Provider;

import android.content.Context;

public class StorageProvider implements Provider {
    Context mContext;

    @Depend(require = true)
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void onDependsCompleted(Object inject) {

    }

    @Provide
    public AppStorageManager provideStorageManager() {
        return new AppStorageManager(mContext);
    }

    @Provide
    public AppSettings provideSettings() {
        return new AppSettings(mContext);
    }

    @Override
    public void onInjectCompleted(Object inject) {

    }
}
