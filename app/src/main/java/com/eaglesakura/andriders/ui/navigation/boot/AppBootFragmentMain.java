package com.eaglesakura.andriders.ui.navigation.boot;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.NavigationActivity;
import com.eaglesakura.andriders.ui.navigation.NavigationBaseFragment;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.firebase.auth.FirebaseAuthorizeManager;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.gms.client.PlayServiceConnection;
import com.eaglesakura.android.gms.error.SignInRequireException;
import com.eaglesakura.android.gms.util.PlayServiceUtil;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.android.util.AndroidNetworkUtil;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.lambda.CancelCallback;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;

import java.util.Arrays;
import java.util.List;

/**
 * アプリを起動し、必要なハンドリングを行う
 */
public class AppBootFragmentMain extends NavigationBaseFragment {

    /**
     * パーミッションをリクエストした回数
     *
     * 一定を超えたら、警告を出して終了
     */
    @BundleState
    int mPermissionRequestCount;

    final int GOOGLE_AUTH_STEP_NONE = 0;

    final int GOOGLE_AUTH_STEP_API_CONNECT = 1;

    /**
     * Google認証中である場合true
     */
    @BundleState
    int mGoogleAutStep = GOOGLE_AUTH_STEP_NONE;

    /**
     * サインインを行えた場合
     */
    GoogleSignInAccount mSignInAccount;

    FirebaseAuthorizeManager mFirebaseAuthorizeManager = FirebaseAuthorizeManager.getInstance();

    List<PermissionUtil.PermissionType> REQUIRE_PERMISSIONS =
            Arrays.asList(PermissionUtil.PermissionType.SelfLocation,
                    PermissionUtil.PermissionType.ExternalStorage,
                    PermissionUtil.PermissionType.BluetoothLE
            );

    public AppBootFragmentMain() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_app_boot);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!PermissionUtil.isRuntimePermissionGranted(getContext(), REQUIRE_PERMISSIONS)) {
            // パーミッションを取得する
            if (mPermissionRequestCount < 2) {
                ++mPermissionRequestCount;
                requestRuntimePermission(REQUIRE_PERMISSIONS);
            } else {
                // 警告ダイアログを出す
                onFailedUserPermission();
            }
        } else {
            if (!PermissionUtil.canDrawOverlays(getContext())) {
                // 特殊パーミッションを取得する
                onFailedDrawOverlays();
            }
        }

        startBootCheck();
    }

    /**
     * 起動時の診断を行う
     */
    @UiThread
    void startBootCheck() {
        if (mGoogleAutStep != GOOGLE_AUTH_STEP_NONE) {
            // Google Auth中は起動チェックを行わない
            return;
        }

        async(SubscribeTarget.Pipeline, ObserveTarget.CurrentForeground, (RxTask<Intent> task) -> {
            CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task);

            // 所定のパーミッションを得るまで起動させない
            while (!PermissionUtil.isRuntimePermissionGranted(getContext(), REQUIRE_PERMISSIONS)) {
                task.waitTime(100);
            }

            // UsageStatusチェックが行えるか
            while (!PermissionUtil.canDrawOverlays(getContext())) {
                task.waitTime(100);
            }

            // ネットワーク接続されているならば、アカウントログインチェック
            if (AndroidNetworkUtil.isNetworkConnected(getContext())) {
                try (PlayServiceConnection connection = PlayServiceConnection.newInstance(AppUtil.newFullPermissionClient(getActivity()), GoogleApiClient.SIGN_IN_MODE_OPTIONAL, cancelCallback)) {
                    if (!connection.isConnectionSuccess(Auth.GOOGLE_SIGN_IN_API, Fitness.SESSIONS_API, Fitness.HISTORY_API)) {
                        // 必要なAPIを満たしていない場合、ログインを行わせる
                        PlayServiceUtil.await(Auth.GoogleSignInApi.signOut(connection.getClient()), cancelCallback);
                        throw new SignInRequireException(connection.newSignInIntent());
                    }

                    // Firebaseログイン
                    if (mSignInAccount != null) {
                        mFirebaseAuthorizeManager.signIn(mSignInAccount, cancelCallback);
                    } else if (mFirebaseAuthorizeManager.getCurrentUser() == null) {
                        // Firebaseログインが必要
                        throw new SignInRequireException(connection.newSignInIntent());
                    }
                }
            }

            AppLog.system("Boot Success");
            Intent intent = new Intent(getActivity(), NavigationActivity.class);
            return intent;
        }).completed((intent, task) -> {
            // Activityを起動する
            startActivity(intent);
            getActivity().finish();
        }).failed((error, task) -> {
            AppLog.report(error);
            if (error instanceof SignInRequireException) {
                startActivityForResult(((SignInRequireException) error).getSignInIntent(), AppConstants.GOOGLE_AUTH);
            }
        }).cancelSignal(this).start();
    }

    /**
     * 基本的なパーミッション取得に失敗した
     */
    void onFailedUserPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.Common_Dialog_Title_Error);
        builder.setMessage("アプリの実行に必要な権限を得られませんでした。\nアプリの権限を確認し、再起動してください。");
        builder.setCancelable(false);
        builder.setPositiveButton("権限を確認する", (dlg, which) -> {
            startActivity(ContextUtil.getAppSettingIntent(getActivity()));
        });
        builder.show();
    }

    /**
     * オーバーレイ表示に失敗した
     */
    void onFailedDrawOverlays() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.Common_Dialog_Title_Error);
        builder.setMessage("サイコン表示を行うため、「他のアプリの上に重ねて表示」を許可してください。");
        builder.setCancelable(false);
        builder.setPositiveButton("設定を開く", (dlg, which) -> {
            startActivity(ContextUtil.getAppOverlaySettingIntent(getActivity()));
        });
        builder.show();
    }

    /**
     * Google Authに失敗した
     */
    void onFailedGoogleAuth() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.Common_Dialog_Title_Error);
        builder.setMessage("Googleサービスとの連携を行うため、Googleアカウントでログインする必要があります。");
        builder.setCancelable(false);
        builder.setPositiveButton("ログイン", (dlg, which) -> {
            startGoogleSignIn();
        });
        builder.show();
    }

    /**
     * Google Sign Inを開始する
     */
    void startGoogleSignIn() {
        asyncUI((RxTask<Intent> task) -> {
            return PlayServiceUtil.newSignInIntent(AppUtil.newFullPermissionClient(getActivity()), () -> task.isCanceled());
        }).completed((result, task) -> {
            startActivityForResult(result, AppConstants.GOOGLE_AUTH);
        }).failed((error, task) -> {
            AppLog.printStackTrace(error);
        }).start();
    }

    @OnActivityResult(AppConstants.GOOGLE_AUTH)
    void resultGoogleAuth(int result, Intent data) {
        GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (signInResult.isSuccess()) {
            mSignInAccount = signInResult.getSignInAccount();
            AppLog.test("mail[%s]", mSignInAccount.getEmail());
            AppLog.test("idToken[%s]", mSignInAccount.getIdToken());
        }
        mGoogleAutStep = GOOGLE_AUTH_STEP_NONE;
        startBootCheck();
    }
}
