package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.databinding.FragmentUserLogSynthesisBinding;
import com.eaglesakura.andriders.db.session.SessionLogDatabase;
import com.eaglesakura.andriders.db.session.SessionTotal;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.ui.binding.UserLogSynthesis;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.util.StringUtil;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.FileNotFoundException;

public class UserLogSynthesisFragment extends AppBaseFragment {
    FragmentUserLogSynthesisBinding mBinding;

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
        async(SubscribeTarget.Pipeline, ObserveTarget.CurrentForeground, (RxTask<SessionTotal> task) -> {
            SessionLogDatabase db = new SessionLogDatabase(getContext());
            try {
                db.openReadOnly();
                SessionTotal result = db.loadTotal();
                if (result == null) {
                    throw new FileNotFoundException("Log Not Found");
                }
                return result;
            } finally {
                db.close();
            }
        }).completed((result, task) -> {
            onLoadTotal(result);
        }).failed((error, task) -> {
            onLoadFailed(error);
        }).finalized(task -> {

        }).start();
    }

    @UiThread
    void onLoadTotal(@NonNull SessionTotal total) {
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
                return StringUtil.format("No DATA");
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
                return StringUtil.format("No DATA");
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

