package com.eaglesakura.andriders.service.base;

import com.eaglesakura.andriders.db.AppSettings;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.provider.StorageProvider;
import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.PendingCallbackQueue;
import com.eaglesakura.android.rx.SubscriptionController;

import android.app.Service;
import android.content.Context;

public abstract class AppBaseService extends Service {
    ServiceLifecycleDelegate mLifecycleDelegate = new ServiceLifecycleDelegate();

    @Inject(AppContextProvider.class)
    protected AppSettings mSettings;

    @Override
    public void onCreate() {
        super.onCreate();

        Garnet.inject(this);

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

    public PendingCallbackQueue getCallbackQueue() {
        return mLifecycleDelegate.getCallbackQueue();
    }


}
