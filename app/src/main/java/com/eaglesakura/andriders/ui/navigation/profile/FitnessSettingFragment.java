package com.eaglesakura.andriders.ui.navigation.profile;

import com.google.android.gms.common.api.GoogleApiClient;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.google.GoogleApiUtil;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.util.AppConstants;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.andriders.v2.db.UserProfiles;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.gms.client.PlayServiceConnection;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.MaterialInputDialog;
import com.eaglesakura.util.LogUtil;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class FitnessSettingFragment extends AppBaseFragment {
    UserProfiles mPersonalDataSettings;

    public FitnessSettingFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_setting_fitness);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPersonalDataSettings = getSettings().getUserProfiles();
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePersonalUI();
        syncFitnessData();
    }

    /**
     * 個人設定を更新する
     */
    @UiThread
    void updatePersonalUI() {
        AQuery q = new AQuery(getView());

        // 体重設定
        q.id(R.id.Setting_Personal_WeightValue).text(String.format("%.1f", mPersonalDataSettings.getUserWeight()));
        // 心拍設定
        q.id(R.id.Setting_Personal_NormalHeartrateValue).text(String.valueOf(mPersonalDataSettings.getNormalHeartrate()));
        q.id(R.id.Setting_Personal_MaxHeartrateValue).text(String.valueOf(mPersonalDataSettings.getMaxHeartrate()));
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
            LogUtil.log(e);
        }

        MaterialInputDialog dialog = new MaterialInputDialog(getActivity()) {
            @Override
            protected void onInitializeViews(TextView header, EditText input, TextView fooder) {
                ViewUtil.setInputDecimal(input);
                input.setHint(R.string.Setting_Fitness_Weight_DialogHiht);
                fooder.setText(R.string.Setting_Fitness_Weight_Unit);
            }

            @Override
            protected void onCommit(EditText input) {
                mPersonalDataSettings.setUserWeight(ViewUtil.getDoubleValue(input, mPersonalDataSettings.getUserWeight()));
                updatePersonalUI();

                asyncCommitSettings();
            }
        };
        dialog.setTitle(R.string.Setting_Fitness_Weight_DialogTitle);
        dialog.show();
    }


    /**
     * GoogleFitの設定が完了した
     */
    @OnActivityResult(AppConstants.REQUEST_GOOGLE_FIT_SETTING)
    void resultGoogleFitSettings(int result, Intent data) {
        syncFitnessData();
    }

    interface HeartrateInputListener {
        void onInputHeartrate(int bpm);
    }

    /**
     * 既にGoogle Fitのメッセージを表示していたらtrue
     */
    @BundleState
    boolean mGoogleFitFailedMessageBooted = false;

    /**
     * Google Fitのデータと同期を行う
     */
    void syncFitnessData() {
        asyncUI((BackgroundTask<Float> task) -> {
            GoogleApiClient.Builder builder = AppUtil.newFullPermissionClient(getActivity());

            CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task, 60, TimeUnit.SECONDS);
            try (PlayServiceConnection connection = PlayServiceConnection.newInstance(builder, cancelCallback)) {
                GoogleApiClient client = connection.getClientIfSuccess();
                float userWeight = GoogleApiUtil.getUserWeightFromFit(client, cancelCallback);
                if (userWeight > 0 && userWeight != mPersonalDataSettings.getUserWeight()) {
                    mPersonalDataSettings.setUserWeight(userWeight);
                    mPersonalDataSettings.commit();
                    toast(R.string.Setting_Fitness_Weight_SyncCompleted);
                } else if (userWeight <= 0) {
                    toast(R.string.Setting_Fitness_Weight_SyncFailed);
                }
                return userWeight;
            }
        }).completed((weight, task) -> {
            updatePersonalUI();
        }).failed((err, task) -> {
            AppLog.printStackTrace(err);

            if (!mGoogleFitFailedMessageBooted) {
                toast(R.string.Setting_Fitness_Weight_SyncFailed);
                mGoogleFitFailedMessageBooted = true;
            }
        }).start();
    }


    @OnClick(R.id.CycleComputer_Personal_MaxHeartrate)
    void clickPersonalMaxHeartrate() {
        showHeartrateInputDialog(
                R.string.Setting_Fitness_MaxHeartrate_DialogTitle, R.string.Setting_Fitness_MaxHeartrate_DialogHiht,
                mPersonalDataSettings.getMaxHeartrate(),
                // 心拍受信ハンドリング
                bpm -> {
                    mPersonalDataSettings.setMaxHeartrate(bpm);
                    asyncCommitSettings();
                }
        );
    }

    @OnClick(R.id.CycleComputer_Personal_NormalHeartrate)
    void clickPersonalNormalHeartrate() {
        showHeartrateInputDialog(
                R.string.Setting_Fitness_NormalHeartrate_DialogTitle, R.string.Setting_Fitness_MaxHeartrate_DialogHiht,
                mPersonalDataSettings.getNormalHeartrate(), (bpm) -> {
                    mPersonalDataSettings.setNormalHeartrate(bpm);
                    asyncCommitSettings();
                }
        );
    }

    void showHeartrateInputDialog(final @StringRes int titleRes, final @StringRes int hintRes, final int defaultValue, final HeartrateInputListener listener) {
        MaterialInputDialog dialog = new MaterialInputDialog(getActivity()) {
            @Override
            protected void onInitializeViews(TextView header, EditText input, TextView fooder) {
                ViewUtil.setInputIntegerOnly(input);
                input.setHint(hintRes);
                fooder.setText(R.string.Setting_Fitness_MaxHeartrate_Unit);
            }

            @Override
            protected void onCommit(EditText input) {
                listener.onInputHeartrate((int) ViewUtil.getLongValue(input, defaultValue));
                updatePersonalUI();

                asyncCommitSettings();
            }
        };
        dialog.setTitle(titleRes);
        dialog.show();
    }


}
