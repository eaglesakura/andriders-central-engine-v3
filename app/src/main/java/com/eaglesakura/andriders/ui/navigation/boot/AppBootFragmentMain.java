package com.eaglesakura.andriders.ui.navigation.boot;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.firebase.auth.FirebaseAuthorizeManager;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.gms.client.PlayServiceConnection;
import com.eaglesakura.android.gms.error.SignInRequireException;
import com.eaglesakura.android.gms.util.PlayServiceUtil;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.Timer;

import android.content.Intent;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * アプリを起動し、必要なハンドリングを行う
 */
public class AppBootFragmentMain extends AppNavigationFragment {

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
    @BundleState
    GoogleSignInAccount mSignInAccount;

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    FirebaseAuthorizeManager mFirebaseAuthorizeManager = FirebaseAuthorizeManager.getInstance();

    List<PermissionUtil.PermissionType> REQUIRE_PERMISSIONS =
            Arrays.asList(PermissionUtil.PermissionType.SelfLocation,
                    PermissionUtil.PermissionType.ExternalStorage,
                    PermissionUtil.PermissionType.BluetoothLE
            );

    /**
     * タップでスキップして良い
     */
    boolean mBootSkipEnabled;

    /**
     * 起動が完了した
     */
    boolean mBootCompleted;


    public AppBootFragmentMain() {
        mFragmentDelegate.setLayoutId(R.layout.boot);
    }

    @Override
    public void onResume() {
        super.onResume();
        setBootSkipEnabled(false);

        if (!PermissionUtil.isRuntimePermissionGranted(getContext(), REQUIRE_PERMISSIONS)) {
            // パーミッションを取得する
            if (mPermissionRequestCount < 2) {
                ++mPermissionRequestCount;
                requestRuntimePermission(REQUIRE_PERMISSIONS);
            } else {
                // 警告ダイアログを出す
                onFailedUserPermission();
            }
            return;
        } else {
            if (!PermissionUtil.canDrawOverlays(getContext())) {
                // 特殊パーミッションを取得する
                onFailedDrawOverlays();
                return;
            } else if (!PermissionUtil.isUsageStatsAllowed(getContext())) {
                // アプリ履歴にアクセス出来ない
                onFailedUsageStatus();
                return;
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

        async(ExecuteTarget.LocalQueue, CallbackTime.CurrentForeground, task -> {
            task.throwIfCanceled();

            CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task);

            // 所定のパーミッションを得るまで起動させない
            while (!PermissionUtil.isRuntimePermissionGranted(getContext(), REQUIRE_PERMISSIONS)) {
                task.waitTime(1);
            }

            task.throwIfCanceled();

            // オーバーレイ描画が行えるか
            while (!PermissionUtil.canDrawOverlays(getContext())) {
                task.waitTime(1);
            }

            // アプリ使用履歴チェック
            while (!PermissionUtil.isUsageStatsAllowed(getContext())) {
                task.throwIfCanceled();

                task.waitTime(1);
            }

            task.throwIfCanceled();

            // アカウントログインチェック
            try (PlayServiceConnection connection = PlayServiceConnection.newInstance(AppUtil.newFullPermissionClient(getActivity()), GoogleApiClient.SIGN_IN_MODE_OPTIONAL, cancelCallback)) {
                if (!connection.isConnectionSuccess(Auth.GOOGLE_SIGN_IN_API, Fitness.SESSIONS_API, Fitness.HISTORY_API)) {
                    task.throwIfCanceled();
                    // 必要なAPIを満たしていない場合、ログインを行わせる
                    PlayServiceUtil.await(Auth.GoogleSignInApi.revokeAccess(connection.getClient()), cancelCallback);
                    throw new SignInRequireException(connection.newSignInIntent());
                }

                // Firebaseログイン
                if (mSignInAccount != null) {
                    mFirebaseAuthorizeManager.signIn(mSignInAccount, cancelCallback);
                    mSignInAccount = null;
                }

                if (mFirebaseAuthorizeManager.await(cancelCallback) == null) {
                    // Firebaseログインが必要
                    throw new SignInRequireException(connection.newSignInIntent());
                }
            }

            task.throwIfCanceled();

            if (!mAppSettings.getConfig().requireFetch()) {
                // Fetchが必須でない場合、起動をスキップできる
                setBootSkipEnabled(true);
            }

            // Configを取得する
            Timer timer = new Timer();
            try {
                CancelCallback callback = AppSupportUtil.asCancelCallback(task, 1000 * 10, TimeUnit.MILLISECONDS);
                mAppSettings.getConfig().fetch(callback);
            } catch (Throwable e) {
                if (mAppSettings.getConfig().requireFetch()) {
                    // Fetchに失敗し、かつコンフィグの同期も行われていない初回は起動に失敗しなければならない
                    // もしFetchに失敗し、古いコンフィグさえある状態であれば動作の継続は行えるため例外を握りつぶす
                    throw e;
                }
            } finally {
                AppLog.system("Config SyncTime[%d ms]", timer.end());
            }

            AppLog.system("Boot Success");
            return this;
        }).completed((result, task) -> {
            // Activityを起動する
            onBootCompleted();
        }).failed((error, task) -> {
            // 起動処理が完了しているなら文句を言わない
            if (mBootCompleted) {
                return;
            }

            AppLog.report(error);
            if (error instanceof SignInRequireException && mGoogleAutStep == GOOGLE_AUTH_STEP_NONE) {
                mGoogleAutStep = GOOGLE_AUTH_STEP_API_CONNECT;
                startActivityForResult(((SignInRequireException) error).getSignInIntent(), AppConstants.REQUEST_GOOGLE_AUTH);
            }
        }).start();
    }

    @OnClick(R.id.Root)
    void clickRoot() {
        if (mBootSkipEnabled) {
            AppLog.system("Skip BootProcess");
            onBootCompleted();
        }
    }

    void setBootSkipEnabled(boolean nextValue) {
        getCallbackQueue().run(CallbackTime.CurrentForeground, () -> {
            mBootSkipEnabled = nextValue;
            new AQuery(getView()).id(R.id.Item_Message).text(nextValue ? R.string.Message_Boot_SkipEnabled : R.string.Message_Boot);
        });
    }

    @UiThread
    void onBootCompleted() {
        if (mBootCompleted) {
            return;
        }

        // Activityを起動する
        for (Listener listener : listInterfaces(Listener.class)) {
            listener.onBootCompleted(this);
        }
        mBootCompleted = true;
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
     * 最近のアプリアクセスに失敗した
     */
    void onFailedUsageStatus() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.Common_Dialog_Title_Error);
        builder.setMessage("サイコン表示切り替えを有効化するため、「使用履歴へアクセス」を許可してください。");
        builder.setCancelable(false);
        builder.setPositiveButton("設定を開く", (dlg, which) -> {
            startActivity(ContextUtil.getUsageStatusAcesss(getActivity()));
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
        asyncUI((BackgroundTask<Intent> task) -> {
            return PlayServiceUtil.newSignInIntent(AppUtil.newFullPermissionClient(getActivity()), () -> task.isCanceled());
        }).completed((result, task) -> {
            startActivityForResult(result, AppConstants.REQUEST_GOOGLE_AUTH);
        }).failed((error, task) -> {
            AppLog.printStackTrace(error);
        }).start();
    }

    @OnActivityResult(AppConstants.REQUEST_GOOGLE_AUTH)
    void resultGoogleAuth(int result, Intent data) {
        GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (signInResult.isSuccess()) {
            mSignInAccount = signInResult.getSignInAccount();
            AppLog.test("mail[%s]", mSignInAccount.getEmail());
            AppLog.test("photo[%s]", mSignInAccount.getPhotoUrl().toString());
        }
        mGoogleAutStep = GOOGLE_AUTH_STEP_NONE;
        startBootCheck();
    }

    public interface Listener {
        void onBootCompleted(AppBootFragmentMain self);
    }
}
