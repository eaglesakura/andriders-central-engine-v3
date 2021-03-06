package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.databinding.UserLogDailySummaryRowBinding;
import com.eaglesakura.andriders.db.session.SessionTotal;
import com.eaglesakura.andriders.db.session.SessionTotalCollection;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.material.widget.support.SupportRecyclerView;

import android.content.Context;
import android.support.annotation.UiThread;
import android.view.View;
import android.view.ViewGroup;

public class UserLogDailyFragment extends AppBaseFragment {
    @Bind(R.id.UserActivity_Date_Root)
    SupportRecyclerView mDailyActivities;

    UserLogFragmentParent mParent;

    public UserLogDailyFragment() {
        mFragmentDelegate.setLayoutId(R.layout.user_log_daily);
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        mDailyActivities.setAdapter(mSessionCards, true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mParent = getParentOrThrow(UserLogFragmentParent.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActivities();
    }

    @UiThread
    void loadActivities() {
        async(ExecuteTarget.LocalQueue, CallbackTime.CurrentForeground, (BackgroundTask<SessionTotalCollection> task) -> {
            while (!task.isCanceled()) {
                SessionTotalCollection result = mParent.getUserLogCollection();
                if (result != null) {
                    return result;
                }
            }
            throw new TaskCanceledException();
        }).completed((result, task) -> {
            onLoadSessions(result);
        }).failed((error, task) -> {
            onLoadFailed(error);
        }).finalized(task -> {
        }).start();
    }

    CardAdapter<SessionTotal> mSessionCards = new CardAdapter<SessionTotal>() {
        @Override
        protected View onCreateCard(ViewGroup parent, int viewType) {
            return UserLogDailySummaryRowBinding.inflate(getActivity().getLayoutInflater(), null, false).getRoot();
        }

        @Override
        protected void onBindCard(CardBind<SessionTotal> bind, int position) {

        }
    };

    @UiThread
    void onLoadSessions(SessionTotalCollection sessions) {
        mSessionCards.getCollection().addAll(sessions.getTotals());
    }

    @UiThread
    void onLoadFailed(Throwable error) {
        error.printStackTrace();
    }
}

