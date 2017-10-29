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
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.gms.client.PlayServiceConnection;
import com.eaglesakura.android.gms.util.PlayServiceUtil;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;
import com.eaglesakura.sloth.view.builder.SnackbarBuilder;
import com.eaglesakura.util.StringUtil;
import com.edmodo.rangebar.RangeBar;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.TimeUnit;

@FragmentLayout(R.layout.profile_fitness)
public class FitnessSettingFragment extends AppFragment {

    private final int MIN_HEARTRATE = 50;

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    @NonNull
    UserProfiles mUserProfile;

    @Bind(R.id.Range_Heartrate)
    RangeBar mHeartrateZone;

    /**
     * 既にGoogle Fitのメッセージを表示していたらtrue
     */
    @BundleState
    boolean mWeightSyncMessageBooted = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserProfile = mAppSettings.getUserProfiles();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mHeartrateZone.setThumbIndices(mUserProfile.getNormalHeartrate() - MIN_HEARTRATE, mUserProfile.getMaxHeartrate() - MIN_HEARTRATE);
        mHeartrateZone.setOnRangeBarChangeListener(mHeartrateRangeListenerImpl);
        return view;
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

        ComponentName componentName = mAppSettings.getConfig().getGoogleFitAppComponent();

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setComponent(componentName);
            startActivityForResult(intent, AppConstants.REQUEST_GOOGLE_FIT_SETTING);

            // 起動成功したから何もしない
            return;
        } catch (Exception e) {
            AppLog.report(e);
        }

        AppDialogBuilder.newAlert(getContext(), R.string.Word_Profile_GoogleFitNotInstalled)
                .positiveButton(R.string.Word_Common_Install, () -> {
                    Intent installIntent = PlayServiceUtil.newGooglePlayInstallIntent(getContext(), componentName.getPackageName());
                    startActivity(installIntent);
                })
                .neutralButton(R.string.Word_Common_OK, null)
                .show(getFragmentLifecycle());
    }


    /**
     * GoogleFitの設定が完了した
     */
    @OnActivityResult(AppConstants.REQUEST_GOOGLE_FIT_SETTING)
    void resultGoogleFitSettings(int result, Intent data) {
        syncFitnessData();
    }

    /**
     * Google Fitのデータと同期を行う
     */
    @UiThread
    void syncFitnessData() {
        asyncQueue((BackgroundTask<Float> task) -> {
            GoogleApiClient.Builder builder = AppUtil.newFullPermissionClient(getActivity());
            SupportCancelCallbackBuilder.CancelChecker checker = SupportCancelCallbackBuilder.from(task).orTimeout(60, TimeUnit.SECONDS).build();
            try (PlayServiceConnection connection = PlayServiceConnection.newInstance(builder, checker)) {
                GoogleApiClient client = connection.getClientIfSuccess();
                float userWeight = GoogleApiUtil.getUserWeightFromFit(client, checker);
                AppLog.system("Sync Weight[%.1f kg]", userWeight);
                if (userWeight > 0) {
                    mUserProfile.setUserWeight(userWeight);
                    mAppSettings.commit();
                } else {
                    throw new IllegalArgumentException("Weight Error");
                }
                return userWeight;
            }
        }).completed((weight, task) -> {
            updateUI();
            if (!mWeightSyncMessageBooted && weight > 0) {
                SnackbarBuilder.from(this)
                        .message(R.string.Message_Profile_WeightSyncCompleted)
                        .show();
                mWeightSyncMessageBooted = true;
            }
        }).failed((err, task) -> {
            AppLog.printStackTrace(err);
            if (!mWeightSyncMessageBooted) {
                SnackbarBuilder.from(this)
                        .message(R.string.Message_Profile_WeightSyncFailed)
                        .show();
                mWeightSyncMessageBooted = true;
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
