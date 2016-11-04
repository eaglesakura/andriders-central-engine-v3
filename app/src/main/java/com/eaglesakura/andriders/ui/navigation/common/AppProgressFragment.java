package com.eaglesakura.andriders.ui.navigation.common;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.ui.progress.ProgressStackManager;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.material.widget.MaterialProgressView;

import android.support.annotation.NonNull;
import android.view.View;

/**
 *
 */
@FragmentLayout(R.layout.widget_progress)
public class AppProgressFragment extends AppFragment {

    @NonNull
    private final ProgressStackManager mProgressStackManager = new ProgressStackManager(mLifecycleDelegate.getCallbackQueue());

    public AppProgressFragment() {
        mProgressStackManager.setListener(mStackManagerListener);
    }

    public ProgressStackManager getProgressStackManager() {
        return mProgressStackManager;
    }

    private ProgressStackManager.Listener mStackManagerListener = new ProgressStackManager.Listener() {
        @Override
        public void onProgressStarted(@NonNull ProgressStackManager self, @NonNull ProgressToken token) {
            AQuery q = new AQuery(getView());
            q.id(R.id.App_Progress)
                    .ifPresent(MaterialProgressView.class, it -> {
                        it.setText(token.getMessage());
                        it.setVisibility(View.VISIBLE);
                    });
        }

        @Override
        public void onProgressTopChanged(@NonNull ProgressStackManager self, @NonNull ProgressToken topToken) {
            AQuery q = new AQuery(getView());
            q.id(R.id.App_Progress)
                    .ifPresent(MaterialProgressView.class, it -> {
                        it.setText(topToken.getMessage());
                        it.setVisibility(View.VISIBLE);
                    });
        }

        @Override
        public void onProgressFinished(@NonNull ProgressStackManager self) {
            AQuery q = new AQuery(getView());
            q.id(R.id.App_Progress)
                    .ifPresent(MaterialProgressView.class, it -> {
                        it.setVisibility(View.INVISIBLE);
                    });
        }
    };
}
