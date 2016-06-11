package com.eaglesakura.andriders.ui.navigation.menu;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.ui.auth.AcesAuthActivity;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.material.widget.MaterialAlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * Googleログイン管理を行うFragment
 * <p/>
 * バージョンアップで不足したPermissionが発生した場合も表示する
 */
public class GoogleLoginCtrlFragment extends AppBaseFragment {

    /**
     * 初期リリースセットアップ
     */
    static final int RELEASE_INITIALIZE_NUMBER_release1 = 1;

    /**
     * Google Play Game Service対応版リリース
     */
    static final int RELEASE_INITIALIZE_NUMBER_release2 = 2;

    /**
     * アプリが2.x系列にリニューアルされた
     */
    static final int RELEASE_INITIALIZE_NUMBER_release3 = 3;

    /**
     * 初期化番号
     */
    static final int RELEASE_INITIALIZE_NUMBER = RELEASE_INITIALIZE_NUMBER_release3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int releasedNumber = getSettings().getUpdateCheckProps().getInitializeReleased();
        if (!FrameworkCentral.getSettings().getLoginGoogleClientApi()
                || releasedNumber < RELEASE_INITIALIZE_NUMBER) {
            showLoginDialog(releasedNumber);
        }

        if (releasedNumber < RELEASE_INITIALIZE_NUMBER) {
            // 初期化が済んでなかったらセットアップする
            releaseMigration();
        }
    }

    /**
     * ログイン画面を表示する
     */
    void showLoginDialog(int releasedNumber) {
        // 一旦ログイン状態を解除
        FrameworkCentral.getSettings().setLoginGoogleClientApi(false);
        asyncCommitSettings();

        // ログインを必須とする
        MaterialAlertDialog dialog = new MaterialAlertDialog(getActivity());
        dialog.setTitle(R.string.Common_Dialog_Title_Verification);
        if (releasedNumber != 0 && releasedNumber < RELEASE_INITIALIZE_NUMBER_release3) {
            // アップデート警告
            dialog.setMessage(R.string.Login_Welcome_Information_UpdateRelease3);
        } else {
            // 通常のログイン警告
            dialog.setMessage(R.string.Login_Welcome_Information);
        }
        dialog.setCancelable(false);
        dialog.setPositiveButton(R.string.Login_Initial_Login, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity(), AcesAuthActivity.class);
                startActivityForResult(intent, REQUEST_GOOGLE_AUTH);
            }
        });
        dialog.setNegativeButton(R.string.Login_Initial_Exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });
        dialog.show();
    }

    void releaseMigration() {
        asyncUI(it -> {
            try {
                pushProgress(R.string.Common_Update_Migration);

                int initialVersion = getSettings().getUpdateCheckProps().getInitializeReleased();

                // 初回起動時に初期セットアップを行う
                if (initialVersion < RELEASE_INITIALIZE_NUMBER_release1) {
                    setupRelease1();
                    initialVersion = RELEASE_INITIALIZE_NUMBER_release1;
                }

                // TODO マイグレーションが必要ならここに記述する
                if (initialVersion < RELEASE_INITIALIZE_NUMBER_release3) {

                }

                // リリース番号をコミット
                Settings settings = getSettings();
                settings.getUpdateCheckProps().setInitializeReleased(RELEASE_INITIALIZE_NUMBER);
                settings.commitAndLoad();
            } finally {
                popProgress();
            }
            return this;
        }).start();
    }

    /**
     * 初期データ
     */
    private void setupRelease1() {
    }
}
