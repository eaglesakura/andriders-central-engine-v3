package com.eaglesakura.andriders.ui.navigation.session;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.plugin.connection.SessionControlConnection;
import com.eaglesakura.andriders.serialize.RawSessionInfo;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.app.FragmentHolder;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;
import com.eaglesakura.sloth.ui.progress.ProgressToken;
import com.eaglesakura.sloth.view.builder.ToolbarBuilder;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.view.View;

/**
 * ユーザーのセッション情報を表示するActivity
 */
@FragmentLayout(R.layout.session_info)
public class UserSessionFragmentMain extends AppNavigationFragment implements SessionControlBus.Holder {
    FragmentHolder<NavigationMapFragment> mNavigationMapFragment =
            FragmentHolder.newInstance(this, NavigationMapFragment.class, R.id.ViewHolder_Navigation);

    @Bind(R.id.Button_SessionChange)
    View mSessionButton;

    final SessionControlBus mSessionControlBus = new SessionControlBus();

    @Override
    protected void onCreateLifecycle(FragmentLifecycle lifecycle) {
        super.onCreateLifecycle(lifecycle);
        mNavigationMapFragment.subscribe(lifecycle);
        mSessionControlBus.bind(lifecycle, this);
    }

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
        asyncQueue((BackgroundTask<SessionControlConnection> task) -> {
            try (ProgressToken token = pushProgress(R.string.Word_Session_ConnectACEs)) {
                SupportCancelCallbackBuilder.CancelChecker checker = SupportCancelCallbackBuilder.from(task).build();
                SessionControlConnection connection = new SessionControlConnection(getContext());
                connection.connect(checker);
                return connection;
            }
        }).completed((result, task) -> {
            mSessionControlBus.modified(result);
            syncSessionButtonState(result.getCentralSessionController().isSessionStarted());
        }).failed((error, task) -> {
            AppLog.report(error);
            AppDialogBuilder.newError(getContext(), error)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getFragmentLifecycle());
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
                .showOnce(getFragmentLifecycle(), "sessionUI");
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
                .showOnce(getFragmentLifecycle(), "sessionUI");
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
            mSessionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.App_Theme_Red)));
        } else {
            // セッションが開始されていない
            toolbarBuilder.title(R.string.Word_Common_AndridersCentralEngine_Short);
            mSessionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.App_Theme_Blue)));
        }

        mSessionButton.setVisibility(View.VISIBLE);
        toolbarBuilder.build();
    }

    @Override
    public SessionControlBus getSessionControlBus() {
        return mSessionControlBus;
    }
}
