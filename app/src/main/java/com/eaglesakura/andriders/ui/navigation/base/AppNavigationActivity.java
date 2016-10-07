package com.eaglesakura.andriders.ui.navigation.base;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.common.AppProgressFragment;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;
import com.eaglesakura.android.framework.ui.support.ContentHolderActivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;

/**
 * アプリ制御用Activity
 */
public abstract class AppNavigationActivity extends ContentHolderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            addProgressFragment();
        }
    }

    /**
     * Progress管理用のFragmentを挿入する
     */
    protected void addProgressFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.Content_Holder_Progress, new AppProgressFragment(), AppProgressFragment.class.getSimpleName());
        transaction.commit();
    }

    @Override
    public int getDefaultLayoutId(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.system_activity;
    }
}
