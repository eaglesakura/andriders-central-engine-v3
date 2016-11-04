package com.eaglesakura.andriders.ui.navigation.base;

import com.eaglesakura.andriders.R;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;
import com.eaglesakura.android.framework.ui.support.ContentHolderActivity;
import com.eaglesakura.util.Util;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

/**
 * アプリ制御用Activity
 */
public abstract class AppNavigationActivity extends ContentHolderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.ifPresent(findViewById(Toolbar.class, R.id.EsMaterial_Toolbar), toolbar -> {
            setSupportActionBar(toolbar);
        });
    }

    @Override
    public int getDefaultLayoutId(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.system_activity;
    }
}
