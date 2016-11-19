package com.eaglesakura.andriders.ui.navigation.profile;

import com.google.android.gms.common.api.GoogleApiClient;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.gen.prop.UserProfiles;
import com.eaglesakura.andriders.google.GoogleApiUtil;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.gms.client.PlayServiceConnection;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.SnackbarBuilder;
import com.eaglesakura.util.StringUtil;
import com.edmodo.rangebar.RangeBar;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.UiThread;

import java.util.concurrent.TimeUnit;

@FragmentLayout(R.layout.profile_fitness)
public class FitnessSettingFragment extends AppFragment {

    private final int MIN_HEARTRATE = 50;

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    UserProfiles mUserProfile;

    @Bind(R.id.Range_Heartrate)
    RangeBar mHeartrateZone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserProfile = mAppSettings.getUserProfiles();
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
        mHeartrateZone.setThumbIndices(mUserProfile.getNormalHeartrate() - MIN_HEARTRATE, mUserProfile.getMaxHeartrate() - MIN_HEARTRATE);
        mHeartrateZone.setOnRangeBarChangeListener(mHeartrateRangeListenerImpl);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        syncFitnessData();
    }

    /**
     * 個人設定を更新する
     */
    @UiThread
    void updateUI() {
        AQuery q = new AQuery(getView());

        // 体重設定
        q.id(R.id.Setting_Personal_WeightValue).text(String.valueOf(mUserProfile.getUserWeight()));
        q.id(R.id.Item_HeartrateMin).text(StringUtil.format("%d bpm", mUserProfile.getNormalHeartrate()));
        q.id(R.id.Item_HeartrateMax).text(StringUtil.format("%d bpm", mUserProfile.getMaxHeartrate()));
    }

    /**
     * 体重をクリックした
     */
    @OnClick(R.id.CycleComputer_Personal_Weight)
    void clickPersonalWeigth() {
        try {
            ComponentName componentName = new ComponentName("com.google.android.apps.fitness", "com.google.android.apps.fitness.preferences.settings.SettingsActivity");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setComponent(componentName);
            startActivityForResult(intent, AppConstants.REQUEST_GOOGLE_FIT_SETTING);

            // 起動成功したから何もしない
            return;
        } catch (Exception e) {
            AppLog.printStackTrace(e);
        }

        AppDialogBuilder.newAlert(getContext(), R.string.Word_Profile_GoogleFitNotInstalled)
                .positiveButton(R.string.Word_Common_Install, () -> {
                })
                .neutralButton(R.string.Common_OK, null)
                .show(mLifecycleDelegate);
    }


    /**
     * GoogleFitの設定が完了した
     */
    @OnActivityResult(AppConstants.REQUEST_GOOGLE_FIT_SETTING)
    void resultGoogleFitSettings(int result, Intent data) {
        syncFitnessData();
    }

    /**
     * 既にGoogle Fitのメッセージを表示していたらtrue
     */
    @BundleState
    boolean mGoogleFitFailedMessageBooted = false;

    /**
     * Google Fitのデータと同期を行う
     */
    @UiThread
    void syncFitnessData() {
        asyncUI((BackgroundTask<Float> task) -> {
            GoogleApiClient.Builder builder = AppUtil.newFullPermissionClient(getActivity());
            CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task, 60, TimeUnit.SECONDS);
            try (PlayServiceConnection connection = PlayServiceConnection.newInstance(builder, cancelCallback)) {
                GoogleApiClient client = connection.getClientIfSuccess();
                float userWeight = GoogleApiUtil.getUserWeightFromFit(client, cancelCallback);
                if (userWeight > 0) {
                    mUserProfile.setUserWeight(userWeight);
                    mAppSettings.commit();
                }
                return userWeight;
            }
        }).completed((weight, task) -> {
            updateUI();
        }).failed((err, task) -> {
            AppLog.printStackTrace(err);

            if (!mGoogleFitFailedMessageBooted) {
                SnackbarBuilder.from(this)
                        .message(R.string.Setting_Fitness_Weight_SyncFailed)
                        .show();
                mGoogleFitFailedMessageBooted = true;
            }
        }).start();
    }

    /**
     * 心拍ゾーンを設定する
     */
    final RangeBar.OnRangeBarChangeListener mHeartrateRangeListenerImpl = (rangeBar, minValue, maxValue) -> {
        AppLog.widget("Heartrate Range(%d <--> %d)", minValue, maxValue);
        minValue = Math.max(minValue, 0);
        maxValue = Math.max(maxValue, minValue);

        UserProfiles profile = mAppSettings.getUserProfiles();

        profile.setNormalHeartrate(MIN_HEARTRATE + minValue);
        profile.setMaxHeartrate(MIN_HEARTRATE + maxValue);

        updateUI();
    };


}
