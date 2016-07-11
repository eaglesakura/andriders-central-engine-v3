package com.eaglesakura.andriders.service.base;

import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.provider.StorageProvider;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.SubscriptionController;

import android.app.Service;
import android.content.Context;

public abstract class AppBaseService extends Service {
    ServiceLifecycleDelegate mLifecycleDelegate = new ServiceLifecycleDelegate();

    @Inject(StorageProvider.class)
    protected Settings mSettings;

    @Override
    public void onCreate() {
        super.onCreate();

        Garnet.create(this)
                .depend(Context.class, this)
                .inject();

        mLifecycleDelegate.onCreate();
    }

    @Override
    public void onDestroy() {
        mLifecycleDelegate.onDestroy();
        super.onDestroy();
    }

    public LifecycleState getLifecycleState() {
        return mLifecycleDelegate.getLifecycleState();
    }

    public SubscriptionController getSubscription() {
        return mLifecycleDelegate.getSubscription();
    }


}
