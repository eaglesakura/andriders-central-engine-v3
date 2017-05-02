package com.eaglesakura.andriders.ui.navigation;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.log.DateSessions;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationActivity;
import com.eaglesakura.andriders.ui.navigation.log.SessionModifyListener;
import com.eaglesakura.andriders.ui.navigation.log.TotalLogFragmentMain;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.util.FragmentUtil;
import com.eaglesakura.sloth.app.delegate.ContentHolderActivityDelegate;
import com.eaglesakura.sloth.ui.progress.SupportProgressFragment;
import com.eaglesakura.util.CollectionUtil;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.List;

/**
 * 全体ログサマリ表示用Activity
 */
public class TotalLogActivity extends AppNavigationActivity implements TotalLogFragmentMain.Callback {

    static final int REQUEST_SHOW_SESSIONS = 0x1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            SupportProgressFragment.attach(this, R.id.Root);
        }
    }

    @Override
    public int getContentLayout(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.system_activity_with_toolbar;
    }

    @NonNull
    @Override
    public Fragment newContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new TotalLogFragmentMain();
    }

    @Override
    public void onSessionNotFound(TotalLogFragmentMain self) {

    }

    @Override
    public void onSessionLoadFailed(TotalLogFragmentMain self, Throwable error) {
        AppDialogBuilder.newError(this, error)
                .positiveButton(R.string.Word_Common_OK, () -> finish())
                .cancelable(false)
                .show(getLifecycle());
    }

    @Override
    public void requestShowDetail(TotalLogFragmentMain self, DateSessions sessions) {
        Intent intent = LogDetailActivity.Builder.from(this)
                .session(sessions)
                .build();
        startActivityForResult(intent, REQUEST_SHOW_SESSIONS);
    }

    /**
     * セッション詳細の表示を完了した
     */
    @OnActivityResult(REQUEST_SHOW_SESSIONS)
    void resultShowSessions(int result, Intent data) {
        List<Long> deletedSessions = LogDetailActivity.getDeletedSessions(data);
        if (CollectionUtil.isEmpty(deletedSessions)) {
            return;
        }

        // 削除命令を伝える
        for (SessionModifyListener listener : FragmentUtil.listInterfaces(getSupportFragmentManager(), SessionModifyListener.class)) {
            listener.onDeleteSession(deletedSessions);
        }
    }
}
