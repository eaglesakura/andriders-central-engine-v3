package com.eaglesakura.andriders.ui.base;

import com.eaglesakura.andriders.AceApplication;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.ui.auth.AcesAuthActivity;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.framework.context.Resources;
import com.eaglesakura.android.framework.ui.BaseFragment;
import com.eaglesakura.android.framework.ui.UserNotificationController;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.playservice.GoogleApiClientToken;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.android.thread.async.AsyncTaskController;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.thread.async.CachedTaskHandler;
import com.eaglesakura.android.thread.async.IAsyncTask;
import com.eaglesakura.material.widget.MaterialAlertDialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.widget.Toast;

import rx.subjects.BehaviorSubject;


public abstract class AppBaseFragment extends BaseFragment {

    /**
     * Googleの認証を行う
     */
    protected static final int REQUEST_GOOGLE_AUTH = 0x2400;


    private BehaviorSubject<LifecycleState> mLifecycleSubject = BehaviorSubject.create(LifecycleState.NewObject);

    private SubscriptionController mSubscription = new SubscriptionController();

    private AsyncTaskController localTasks;

    private CachedTaskHandler localTaskHandler = new CachedTaskHandler();


    public AppBaseFragment() {
        mSubscription.bind(mLifecycleSubject);
    }

    public GoogleApiClientToken getGoogleApiClientToken() {
        Activity activity = getActivity();
        if (activity instanceof AppBaseActivity) {
            return ((AppBaseActivity) activity).getApiClientToken();
        } else {
            return null;
        }
    }

    /**
     * 現在のライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return mLifecycleSubject.getValue();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLifecycleSubject.onNext(LifecycleState.OnCreated);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLifecycleSubject.onNext(LifecycleState.OnStarted);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLifecycleSubject.onNext(LifecycleState.OnPaused);
    }

    @Override
    public void onResume() {
        super.onResume();
        mLifecycleSubject.onNext(LifecycleState.OnResumed);
    }

    @Override
    public void onStop() {
        super.onStop();
        mLifecycleSubject.onNext(LifecycleState.OnStopped);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (localTasks != null) {
            localTasks.cancelListeners();
            localTasks.dispose();
        }
        mLifecycleSubject.onNext(LifecycleState.OnDestroyed);
    }

    public AppBaseActivity getBaseActivity() {
        return (AppBaseActivity) getActivity();
    }

    public Settings getSettings() {
        return Settings.getInstance();
    }

//    /**
//     * 戻り値のない非同期処理を行う
//     * @param target コールバック対象
//     * @param background バックグラウンドタスク
//     * @return
//     */
//    public RxActionCreator<Void> async(LifecycleTarget target, RxTask.Action0 background) {
//        return async(target, Void.class, (RxTask<Void> it) -> {
//            background.call(it);
//            return null;
//        });
//    }

    /**
     * UIに関わる処理を非同期で実行する。
     *
     * 処理順を整列するため、非同期・直列処理されたあと、アプリがフォアグラウンドのタイミングでコールバックされる。
     */
    public <T> RxTaskBuilder<T> asyncUI(RxTask.Async<T> background) {
        return async(SubscribeTarget.Pipeline, ObserveTarget.Forground, background);
    }

    /**
     * 規定のスレッドとタイミングで非同期処理を行う
     */
    public <T> RxTaskBuilder<T> async(SubscribeTarget subscribe, ObserveTarget observe, RxTask.Async<T> background) {
        return new RxTaskBuilder<T>(mSubscription)
                .subscribeOn(subscribe)
                .observeOn(observe)
                .async(background);
    }

    /**
     * ユーザーデータを非同期ロードする
     */
    public AsyncTaskResult<Settings> asyncReloadSettings() {
        return getTaskController().pushBack(new IAsyncTask<Settings>() {
            @Override
            public Settings doInBackground(AsyncTaskResult<Settings> result) throws Exception {
                Settings settings = getSettings();
                settings.load();
                return settings;
            }
        });
    }

    /**
     * ユーザーデータを非同期保存する
     */
    public AsyncTaskResult<Settings> asyncCommitSettings() {
        return getTaskController().pushBack(new IAsyncTask<Settings>() {
            @Override
            public Settings doInBackground(AsyncTaskResult<Settings> result) throws Exception {
                Settings settings = getSettings();
                settings.commitAndLoad();
                return settings;
            }
        });
    }

    /**
     * GooglePlayServiceのログインを開始する
     */
    protected void startGooglePlayServiceLogin() {
        Intent intent = new Intent(getActivity(), AcesAuthActivity.class);
        startActivityForResult(intent, REQUEST_GOOGLE_AUTH);
    }

    /**
     * @param result
     * @param data
     */
    @OnActivityResult(REQUEST_GOOGLE_AUTH)
    protected void onAuthResult(int result, Intent data) {
        // ログインを必須とする
        MaterialAlertDialog dialog = new MaterialAlertDialog(getActivity());
        if (result == Activity.RESULT_OK) {
            dialog.setTitle(R.string.Login_Initial_Success);
            dialog.setMessage(R.string.Login_Initial_Success_Information);
            dialog.setPositiveButton(R.string.Common_OK, null);
        } else {
            dialog.setTitle(R.string.Login_Initial_Error);
            dialog.setMessage(R.string.Login_Initial_Error_Information);
            dialog.setCancelable(false);
            dialog.setPositiveButton(R.string.Login_Initial_Login, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startGooglePlayServiceLogin();
                }
            });
            dialog.setNegativeButton(R.string.Login_Initial_Exit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            });
        }
        dialog.show();
    }

    @Override
    protected AsyncTaskController getTaskController() {
        if (localTasks == null) {
            synchronized (this) {
                if (localTasks == null) {
                    localTasks = new AsyncTaskController(AceApplication.getTaskController());
                    localTasks.setTaskHandler(localTaskHandler);
                }
            }
        }
        return localTasks;
    }

    public UserNotificationController getNotificationController() {
        Activity activity = getActivity();
        if (activity instanceof AppBaseActivity) {
            return ((AppBaseActivity) activity).getNotificationController(null);
        } else {
            return null;
        }
    }

    public void pushProgress(@StringRes int resId) {
        getNotificationController().pushProgress(this, getString(resId));
    }

    public void pushProgress(String message) {
        getNotificationController().pushProgress(this, message);
    }

    public void popProgress() {
        getNotificationController().popProgress(this);
    }

    public void toast(@StringRes final int resId) {
        runUI(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FrameworkCentral.getApplication(), Resources.string(resId), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void toast(@StringRes final String msg) {
        runUI(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FrameworkCentral.getApplication(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
