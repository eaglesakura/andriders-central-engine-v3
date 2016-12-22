package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.log.DateSessions;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.log.DailyLogFragmentMain;
import com.eaglesakura.andriders.ui.navigation.log.LogSummaryBinding;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;
import com.eaglesakura.material.widget.support.SupportProgressFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.Date;

/**
 * 詳細ログ表示用Activity
 */
public class LogDetailActivity extends AppNavigationActivity implements DailyLogFragmentMain.Callback {

    /**
     * サンプリング対象のセッションID
     */
    private static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";

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
        setTitle(LogSummaryBinding.DEFAULT_DAY_FORMATTER.format(new Date(getSessionId())));
    }

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        DailyLogFragmentMain fragmentMain = new DailyLogFragmentMain();
        fragmentMain.setSampleSessionId(getIntent().getLongExtra(EXTRA_SESSION_ID, 0));
        return fragmentMain;
    }

    @Override
    public void onSessionDeleted(DailyLogFragmentMain self) {
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
