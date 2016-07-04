package com.eaglesakura.andriders.ui.base;

import com.eaglesakura.android.framework.ui.BackStackManager;
import com.eaglesakura.android.framework.ui.support.ContentHolderActivity;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.android.util.ContextUtil;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;

public abstract class AppBaseActivity extends ContentHolderActivity {
    static final String FRAGMENT_TAG_GOOGLE_API_FRAGMENT = "FRAGMENT_TAG_GOOGLE_API_FRAGMENT";

    @BundleState
    BackStackManager mBackStackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mBackStackManager == null) {
            mBackStackManager = new BackStackManager();
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

    @NonNull
    public BackStackManager getBackStackManager() {
        return mBackStackManager;
    }
}
