package com.eaglesakura.andriders.ui.navigation.session;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.plugin.connection.SessionControlConnection;
import com.eaglesakura.andriders.serialize.RawSessionInfo;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.framework.ui.FragmentHolder;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.util.ResourceUtil;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.ToolbarBuilder;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;

/**
 * ユーザーのセッション情報を表示するActivity
 */
@FragmentLayout(R.layout.session_info)
public class UserSessionFragmentMain extends AppNavigationFragment implements SessionControlBus.Holder {
    @SuppressWarnings("unused")
    FragmentHolder<NavigationMapFragment> mNavigationMapFragment =
            FragmentHolder.newInstance(this, NavigationMapFragment.class, R.id.ViewHolder_Navigation).bind(mLifecycleDelegate);

    @Bind(R.id.Button_SessionChange)
    Button mSessionButton;

    final SessionControlBus mSessionControlBus = new SessionControlBus().bind(mLifecycleDelegate, this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectSessionCentral();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     *
     */
    void connectSessionCentral() {
        asyncUI((BackgroundTask<SessionControlConnection> task) -> {
            try (ProgressToken token = pushProgress(R.string.Word_Session_ConnectACEs)) {
                CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task);

                SessionControlConnection connection = new SessionControlConnection(getContext());
                connection.connect(cancelCallback);
                return connection;
            }
        }).completed((result, task) -> {
            mSessionControlBus.modified(result);
            syncSessionButtonState(result.getCentralSessionController().isSessionStarted());
        }).failed((error, task) -> {
            AppLog.printStackTrace(error);
            AppDialogBuilder.newError(getContext(), error)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(mLifecycleDelegate);
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSessionControlBus.ifPresent(connection -> connection.disconnectAsync());
        mSessionControlBus.modified(null);
    }

    /**
     * セッションの変更ボタンが押された
     */
    @OnClick(R.id.Button_SessionChange)
    void clickSession() {
        mSessionControlBus.ifPresent(connection -> {
            RawSessionInfo sessionInfo = connection.getCentralSessionController().getSessionInfo();
            if (sessionInfo != null) {
                showSessionStopMessage(sessionInfo);
            } else {
                showSessionStartMessage();
            }
        });
    }

    /**
     * セッション開始確認を行う
     */
    @UiThread
    void showSessionStartMessage() {
        AppDialogBuilder.newInformation(getContext(), R.string.Message_Session_SessionStart)
                .positiveButton(R.string.Word_Session_Start, () -> {
                    mSessionControlBus.ifPresent(connection -> {
                        connection.getCentralSessionController().requestSessionStart();
                    });
                    syncSessionButtonState(true);
                })
                .negativeButton(R.string.Word_Common_Cancel, null)
                .showOnce(mLifecycleDelegate, "sessionUI");
    }

    /**
     * セッション停止確認を行う
     */
    @UiThread
    void showSessionStopMessage(RawSessionInfo info) {
        long sessionTimeMs = (System.currentTimeMillis() - info.sessionId);
        String message = getString(R.string.Message_Session_SessionStart, AppUtil.formatTimeMilliSecToString(sessionTimeMs));
        AppDialogBuilder.newInformation(getContext(), message)
                .positiveButton(R.string.Word_Session_Stop, () -> {
                    mSessionControlBus.getData().getCentralSessionController().requestSessionStop();
                    syncSessionButtonState(false);
                })
                .negativeButton(R.string.Word_Common_Cancel, null)
                .showOnce(mLifecycleDelegate, "sessionUI");
    }

    @UiThread
    void syncSessionButtonState(boolean sessionStarted) {
        if (!mSessionControlBus.hasData()) {
            mSessionButton.setVisibility(View.INVISIBLE);
            return;
        }

        ToolbarBuilder toolbarBuilder = ToolbarBuilder.from(this);
        if (sessionStarted) {
            // 既にセッションが開始されている
            toolbarBuilder.title(R.string.Title_Session_Running);
            mSessionButton.setBackgroundTintList(ColorStateList.valueOf(ResourceUtil.argb(getContext(), R.color.App_Theme_Red)));
            mSessionButton.setText(R.string.Word_Session_SessionStopButton);
        } else {
            // セッションが開始されていない
            toolbarBuilder.title(R.string.Word_Common_AndridersCentralEngine_Short);
            mSessionButton.setBackgroundTintList(ColorStateList.valueOf(ResourceUtil.argb(getContext(), R.color.App_Theme_Blue)));
            mSessionButton.setText(R.string.Word_Session_SessionStartButton);
        }

        mSessionButton.setVisibility(View.VISIBLE);
        toolbarBuilder.build();
    }

    @Override
    public SessionControlBus getSessionControlBus() {
        return mSessionControlBus;
    }
}
