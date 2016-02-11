package com.eaglesakura.andriders.ui.auth;

import com.google.android.gms.common.api.GoogleApiClient;

import com.eaglesakura.andriders.AceApplication;
import com.eaglesakura.android.playservice.GoogleAuthActivity;

public class AcesAuthActivity extends GoogleAuthActivity {
    @Override
    protected GoogleApiClient.Builder newGoogleApiClient() {
        return AceApplication.newFullPermissionClientBuilder();
    }
}
