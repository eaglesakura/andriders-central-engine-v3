package com.eaglesakura.andriders.ui.base;

import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.provider.StorageProvider;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.SupportFragment;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.support.annotation.StringRes;
import android.view.Menu;
import android.widget.Toast;


public abstract class AppBaseFragment extends SupportFragment {
    @Inject(StorageProvider.class)
    protected Settings mSettings;

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {

    }

    @Override
    public void onAfterBindMenu(SupportFragmentDelegate self, Menu menu) {

    }

    @Override
    public void onAfterInjection(SupportFragmentDelegate self) {

    }

    public Settings getSettings() {
        return mSettings;
    }

    /**
     * ユーザーデータを非同期ロードする
     */
    public RxTask<Settings> asyncReloadSettings() {
        return asyncUI((RxTask<Settings> task) -> {
            Settings settings = getSettings();
            settings.load();
            return settings;
        }).start();
    }

    /**
     * ユーザーデータを非同期保存する
     */
    public RxTask<Settings> asyncCommitSettings() {
        return asyncUI((RxTask<Settings> task) -> {
            Settings settings = getSettings();
            settings.commitAndLoad();
            return settings;
        }).start();
    }

    
    public void pushProgress(@StringRes int resId) {
    }

    public void pushProgress(String message) {
    }

    public void popProgress() {
    }

    public void toast(@StringRes final int resId) {
        UIHandler.postUI(() -> {
            Toast.makeText(FrameworkCentral.getApplication(), getString(resId), Toast.LENGTH_SHORT).show();
        });
    }

    public void toast(@StringRes final String msg) {
        UIHandler.postUI(() -> {
            Toast.makeText(FrameworkCentral.getApplication(), msg, Toast.LENGTH_SHORT).show();
        });
    }
}
