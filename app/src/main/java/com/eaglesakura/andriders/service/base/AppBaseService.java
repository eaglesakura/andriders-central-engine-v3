package com.eaglesakura.andriders.service.base;

import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.android.framework.service.BaseService;

public abstract class AppBaseService extends BaseService {
    @Override
    public void onCreate() {
        super.onCreate();
        Settings.getInstance().load();
    }
}
