package com.eaglesakura.andriders.ui.base;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.RequestCodes;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.provider.StorageProvider;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.SupportFragment;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.material.widget.MaterialAlertDialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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

    
//    @OnActivityResult(REQUEST_GOOGLE_AUTH)
//    protected void onAuthResult(int result, Intent data) {
//        // ログインを必須とする
//        MaterialAlertDialog dialog = new MaterialAlertDialog(getActivity());
//        if (result == Activity.RESULT_OK) {
//            dialog.setTitle(R.string.Login_Initial_Success);
//            dialog.setMessage(R.string.Login_Initial_Success_Information);
//            dialog.setPositiveButton(R.string.Common_OK, null);
//        } else {
//            dialog.setTitle(R.string.Login_Initial_Error);
//            dialog.setMessage(R.string.Login_Initial_Error_Information);
//            dialog.setCancelable(false);
//            dialog.setPositiveButton(R.string.Login_Initial_Login, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    startGooglePlayServiceLogin();
//                }
//            });
//            dialog.setNegativeButton(R.string.Login_Initial_Exit, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    getActivity().finish();
//                }
//            });
//        }
//        dialog.show();
//    }


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
