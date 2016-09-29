package com.eaglesakura.andriders.ui.base;

import com.eaglesakura.andriders.data.AppSettings;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.progress.ProgressStackManager;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.SupportFragment;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.widget.Toast;


public abstract class AppBaseFragment extends SupportFragment {
    @Inject(AppContextProvider.class)
    protected AppSettings mSettings;

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {

    }

    @Override
    public void onAfterBindMenu(SupportFragmentDelegate self, Menu menu) {

    }

    @Override
    public void onAfterInjection(SupportFragmentDelegate self) {

    }

    public synchronized AppSettings getSettings() {
        return mSettings;
    }

    /**
     * ユーザーデータを非同期保存する
     */
    public BackgroundTask<AppSettings> asyncCommitSettings() {
        return asyncUI((BackgroundTask<AppSettings> task) -> {
            AppSettings settings = getSettings();
            settings.commit();
            return settings;
        }).start();
    }


    /**
     * Progress処理クラスを取得する
     */
    public ProgressStackManager getProgressStackManager() {
        return null;
    }

    @NonNull
    public ProgressToken pushProgress(@StringRes int stringRes) {
        return pushProgress(getString(stringRes));
    }

    @NonNull
    public ProgressToken pushProgress(String message) {
        Fragment fragment = this;

        while (fragment != null) {
            if (fragment instanceof AppBaseFragment) {
                ProgressStackManager stackManager = ((AppBaseFragment) fragment).getProgressStackManager();
                if (stackManager != null) {
                    ProgressToken token = ProgressToken.fromMessage(stackManager, message);
                    stackManager.push(token);
                    return token;
                }
            }

            fragment = fragment.getParentFragment();
        }

        throw new IllegalStateException(message);
    }

    public void toast(@StringRes final int resId) {
        UIHandler.postUI(() -> {
            Toast.makeText(FrameworkCentral.getApplication(), getString(resId), Toast.LENGTH_SHORT).show();
        });
    }

    public void toast(final String msg) {
        UIHandler.postUI(() -> {
            Toast.makeText(FrameworkCentral.getApplication(), msg, Toast.LENGTH_SHORT).show();
        });
    }
}
