package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.databinding.FragmentUserLogSynthesisBinding;
import com.eaglesakura.andriders.db.session.SessionLogDatabase;
import com.eaglesakura.andriders.db.session.SessionTotal;
import com.eaglesakura.andriders.db.session.SessionTotalCollection;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.material.widget.adapter.CardAdapter;

import android.content.Context;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class UserLogDailyFragment extends AppBaseFragment {
    @Bind(R.id.UserActivity_Date_Root)
    RecyclerView mDailyActivities;

    UserLogFragmentParent mParent;

    public UserLogDailyFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_user_log_daily);
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
        async(SubscribeTarget.Pipeline, ObserveTarget.CurrentForeground, (RxTask<SessionTotalCollection> task) -> {
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
            return Card;
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

