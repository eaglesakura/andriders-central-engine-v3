package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.databinding.FragmentUserLogSynthesisBinding;
import com.eaglesakura.andriders.db.session.SessionTotalCollection;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.ui.binding.UserLogSynthesis;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.util.DateUtil;
import com.eaglesakura.util.StringUtil;
import com.eaglesakura.util.Timer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.TimeZone;

public class UserLogSynthesisFragment extends AppBaseFragment {
    FragmentUserLogSynthesisBinding mBinding;

    UserLogFragmentParent mParent;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mParent = getParentOrThrow(UserLogFragmentParent.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentUserLogSynthesisBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSynthesisLog();
    }

    /**
     * 統計情報を取得する
     */
    @UiThread
    void loadSynthesisLog() {
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

    @UiThread
    void onLoadSessions(@NonNull SessionTotalCollection total) {
        mBinding.setValue(new UserLogSynthesis() {
            @NonNull
            @Override
            public String getSumCyclingDistanceInfo() {
                return StringUtil.format("%.1f km", total.getSumDistanceKm());
            }

            @NonNull
            @Override
            public String getSumAltitudeInfo() {
                return StringUtil.format("%d m", (int) total.getSumAltitude());
            }

            @NonNull
            @Override
            public String getLongestDateDistanceInfo() {
                return StringUtil.format("%.1f km", total.getLongestDateDistanceKm());
            }

            @NonNull
            @Override
            public String getMaxSpeedInfo() {
                return StringUtil.format("%.1f km/h", total.getMaxSpeedKmh());
            }

            @NonNull
            @Override
            public String getMaxCadenceInfo() {
                return StringUtil.format("%d RPM", (int) total.getMaxCadence());
            }

            @NonNull
            @Override
            public String getExerciseInfo() {
                Date todayStart = DateUtil.getDateStart(new Date(), TimeZone.getDefault());
                long ONE_DAY = Timer.toMilliSec(1, 0, 0, 0, 0);
                long TODAY_END = todayStart.getTime() + ONE_DAY;
                return StringUtil.format("%.2f Ex", total.getRangeExercise(TODAY_END - (ONE_DAY * 7), TODAY_END));
            }
        });
    }

    @UiThread
    void onLoadFailed(Throwable e) {
        e.printStackTrace();
        String DATA_TEXT = "No DATA";
        mBinding.setValue(new UserLogSynthesis() {
            @NonNull
            @Override
            public String getSumCyclingDistanceInfo() {
                return DATA_TEXT;
            }

            @NonNull
            @Override
            public String getSumAltitudeInfo() {
                return DATA_TEXT;
            }

            @NonNull
            @Override
            public String getLongestDateDistanceInfo() {
                return DATA_TEXT;
            }

            @NonNull
            @Override
            public String getMaxSpeedInfo() {
                return DATA_TEXT;
            }

            @NonNull
            @Override
            public String getMaxCadenceInfo() {
                return DATA_TEXT;
            }

            @NonNull
            @Override
            public String getExerciseInfo() {
                return DATA_TEXT;
            }
        });
    }
}

