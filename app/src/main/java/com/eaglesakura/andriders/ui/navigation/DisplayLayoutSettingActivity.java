package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.display.DisplaySettingFragmentMain;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;
import com.eaglesakura.material.widget.support.SupportProgressFragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * サイコンディスプレイ表示内容セットアップ
 */
public class DisplayLayoutSettingActivity extends AppNavigationActivity implements DisplaySettingFragmentMain.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            SupportProgressFragment.attach(this, R.id.Root);
        }
    }

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new DisplaySettingFragmentMain();
    }

    @Override
    public void onInitializeFailed(DisplaySettingFragmentMain self, Throwable error) {
        AppLog.printStackTrace(error);
        AppDialogBuilder.newError(this, error)
                .positiveButton(R.string.EsMaterial_Dialog_Close, () -> finish())
                .cancelable(false)
                .show(mLifecycleDelegate);
    }
}
