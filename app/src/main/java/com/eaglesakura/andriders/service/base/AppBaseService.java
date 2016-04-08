package com.eaglesakura.andriders.service.base;

import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.provider.StorageProvider;
import com.eaglesakura.android.framework.service.BaseService;
import com.eaglesakura.android.garnet.Inject;

public abstract class AppBaseService extends BaseService {
    @Inject(StorageProvider.class)
    protected Settings mSettings;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
