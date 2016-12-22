package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.log.DateSessions;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.log.DailyLogFragmentMain;
import com.eaglesakura.andriders.ui.navigation.log.LogSummaryBinding;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.material.widget.support.SupportProgressFragment;
import com.eaglesakura.util.CollectionUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 詳細ログ表示用Activity
 */
public class LogDetailActivity extends AppNavigationActivity implements DailyLogFragmentMain.Callback {

    /**
     * サンプリング対象のセッションID
     */
    private static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";

    /**
     * 削除されたセッション一覧
     */
    private static final String EXTRA_DELETED_SESSIONS = "EXTRA_DELETED_SESSIONS";

    /**
     * 削除されたセッション一覧
     */
    @BundleState
    ArrayList<Long> mDeletedSessions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            SupportProgressFragment.attach(this, R.id.Root);
        }
    }

    /**
     * 表示対象のセッションIDを取得する
     */
    long getSessionId() {
        return getIntent().getLongExtra(EXTRA_SESSION_ID, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // タイトルを再設定
        getSupportActionBar().setTitle(LogSummaryBinding.DEFAULT_DAY_FORMATTER.format(new Date(getSessionId())));
    }

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        DailyLogFragmentMain fragmentMain = new DailyLogFragmentMain();
        fragmentMain.setSampleSessionId(getSessionId());
        return fragmentMain;
    }

    @Override
    public void onSessionDeleted(DailyLogFragmentMain self, SessionHeader header) {
        CollectionUtil.addUnique(mDeletedSessions, header.getSessionId());
    }

    @Override
    public void onAllSessionDeleted(DailyLogFragmentMain self) {
        AppDialogBuilder.newInformation(this, R.string.Message_Log_AllSessionDeleted)
                .positiveButton(R.string.Word_Common_OK, () -> finish())
                .cancelable(false)
                .show(mLifecycleDelegate);
    }

    @Override
    public void onLogLoadFailed(DailyLogFragmentMain self, Throwable error) {
        AppDialogBuilder.newError(this, error)
                .positiveButton(R.string.Word_Common_OK, () -> finish())
                .cancelable(false)
                .show(mLifecycleDelegate);
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        long[] values = new long[mDeletedSessions.size()];
        for (int i = 0; i < mDeletedSessions.size(); ++i) {
            values[i] = mDeletedSessions.get(i);
        }
        data.putExtra(EXTRA_DELETED_SESSIONS, values);
        setResult(RESULT_OK, data);
        super.finish();
    }

    /**
     * 削除されたセッション一覧を取得する
     */
    @NonNull
    public static List<Long> getDeletedSessions(Intent data) {
        if (data == null) {
            return new ArrayList<>();
        }

        long[] sessions = data.getLongArrayExtra(EXTRA_DELETED_SESSIONS);
        if (sessions == null) {
            return new ArrayList<>();
        }
        List<Long> result = new ArrayList<>();
        for (long value : sessions) {
            result.add(value);
        }
        return result;
    }

    public static class Builder {
        Intent mIntent;

        public static Builder from(Context context) {
            Builder result = new Builder();
            result.mIntent = new Intent(context, LogDetailActivity.class);
            return result;
        }

        public Builder session(DateSessions sessions) {
            mIntent.putExtra(EXTRA_SESSION_ID, sessions.getStartTime());
            return this;
        }

        public Intent build() {
            return mIntent;
        }
    }
}
