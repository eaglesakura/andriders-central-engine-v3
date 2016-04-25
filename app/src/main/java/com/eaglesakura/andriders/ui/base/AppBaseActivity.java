package com.eaglesakura.andriders.ui.base;

import com.google.android.gms.common.api.GoogleApiClient;

import com.eaglesakura.andriders.AceApplication;
import com.eaglesakura.andriders.ui.navigation.notification.NotificationFragment;
import com.eaglesakura.android.framework.ui.BackStackManager;
import com.eaglesakura.android.framework.ui.UserNotificationController;
import com.eaglesakura.android.framework.ui.support.ContentHolderActivity;
import com.eaglesakura.android.playservice.GoogleApiClientToken;
import com.eaglesakura.android.playservice.GoogleApiFragment;
import com.eaglesakura.android.util.ContextUtil;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;

import icepick.State;


public abstract class AppBaseActivity extends ContentHolderActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiFragment.Callback, NotificationFragment.Callback {
    static final String FRAGMENT_TAG_GOOGLE_API_FRAGMENT = "FRAGMENT_TAG_GOOGLE_API_FRAGMENT";

    GoogleApiClientToken apiClientToken;

    UserNotificationController notificationController;

    @State
    BackStackManager mBackStackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mBackStackManager == null) {
            mBackStackManager = new BackStackManager();
        }

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            {
                GoogleApiFragment fragment = new GoogleApiFragment();
                transaction.add(fragment, FRAGMENT_TAG_GOOGLE_API_FRAGMENT);
            }
            {
                NotificationFragment fragment = new NotificationFragment();
                transaction.add(fragment, fragment.getClass().getName());
            }
            transaction.commit();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (ContextUtil.isBackKeyEvent(event)) {
            if (mBackStackManager.onBackPressed(getSupportFragmentManager(), event)) {
                // ハンドリングを行ったのでここで処理終了
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public GoogleApiClientToken newClientToken(GoogleApiFragment self) {
        if (apiClientToken == null) {
            apiClientToken =
                    new GoogleApiClientToken(AceApplication.newFullPermissionClientBuilder().addConnectionCallbacks(this));
        }
        return apiClientToken;
    }

    /**
     * get token
     */
    public GoogleApiClientToken getApiClientToken() {
        return apiClientToken;
    }

    @Override
    public void onGooglePlayServiceRecoverCanceled(GoogleApiFragment self, int statusCode) {

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int state) {

    }

    @Override
    public synchronized UserNotificationController getNotificationController(NotificationFragment self) {
        if (notificationController == null) {
            notificationController = new UserNotificationController(this, null);
        }
        return notificationController;
    }

    @Override
    public void onNotificationDetatched(NotificationFragment self, UserNotificationController controller) {
        notificationController = null;
    }

    @NonNull
    public BackStackManager getBackStackManager() {
        return mBackStackManager;
    }
}
