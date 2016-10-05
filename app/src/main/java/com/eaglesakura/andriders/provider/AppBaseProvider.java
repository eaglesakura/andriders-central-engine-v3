package com.eaglesakura.andriders.provider;

import com.eaglesakura.andriders.AceApplication;
import com.eaglesakura.android.framework.provider.ContextProvider;

import android.support.annotation.NonNull;

/**
 * アプリ内で使用するProviderの共通機能を集約する
 */
public abstract class AppBaseProvider extends ContextProvider {

    @NonNull
    public AceApplication getAceApplication() {
        return (AceApplication) getContext().getApplicationContext();
    }
}
