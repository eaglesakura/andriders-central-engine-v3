package com.eaglesakura.andriders.ui.navigation.profile;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.RequestCodes;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.google.GoogleApiUtil;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.v2.db.UserProfiles;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.ui.delegate.SupportFragmentDelegate;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.playservice.GoogleApiClientToken;
import com.eaglesakura.android.playservice.GoogleApiTask;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.material.widget.MaterialInputDialog;
import com.eaglesakura.util.LogUtil;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import icepick.State;

public class FitnessSettingFragment extends AppBaseFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    UserProfiles mPersonalDataSettings;

    static final int REQUEST_GOOGLEFIT_SETTING = RequestCodes.GOOGLEFIT_SETTING;

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
        updatePersonalUI();
    }

    @Override
    public void onResume() {
        super.onResume();

        // client add
        getGoogleApiClientToken().registerConnectionCallbacks(this);
        getGoogleApiClientToken().registerConnectionFailedListener(this);

        syncFitnessData();
    }

    /**
     * 個人設定を更新する
     */
    void updatePersonalUI() {
        runOnUiThread(() -> {
            AQuery q = new AQuery(getView());

            // 体重設定
            q.id(R.id.Setting_Personal_WeightValue).text(String.format("%.1f", mPersonalDataSettings.getUserWeight()));
            // 心拍設定
            q.id(R.id.Setting_Personal_NormalHeartrateValue).text(String.valueOf(mPersonalDataSettings.getNormalHeartrate()));
            q.id(R.id.Setting_Personal_MaxHeartrateValue).text(String.valueOf(mPersonalDataSettings.getMaxHeartrate()));
        });
    }

    /**
     * 体重をクリックした
     */
    @OnClick(R.id.CycleComputer_Personal_Weight)
    void clickPersonalWeigth() {
        if (getGoogleApiClientToken().isLoginCompleted()) {
            try {
                ComponentName componentName = new ComponentName("com.google.android.apps.fitness", "com.google.android.apps.fitness.preferences.settings.SettingsActivity");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setComponent(componentName);
                startActivityForResult(intent, REQUEST_GOOGLEFIT_SETTING);

                // 起動成功したから何もしない
                return;
            } catch (Exception e) {
                LogUtil.log(e);
            }
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
    @OnActivityResult(REQUEST_GOOGLEFIT_SETTING)
    void resultGoogleFitSettings(int result, Intent data) {
        syncFitnessData();
    }

    interface HeartrateInputListener {
        void onInputHeartrate(int bpm);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // ログインボタンを非表示にする
        new AQuery(getView()).id(R.id.Setting_Fitness_ConnectGoogleFit).visibility(View.GONE);

        // Google Fitの同期を行う
        syncFitnessData();
    }


    @OnClick(R.id.Setting_Fitness_ConnectGoogleFit)
    void clickConnectGoogleFit() {
        startGooglePlayServiceLogin();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * 既にGoogle Fitのメッセージを表示していたらtrue
     */
    @State
    boolean googleFitFailedMessageBooted = false;

    /**
     * Google Fitのデータと同期を行う
     */
    void syncFitnessData() {
        asyncUI((RxTask<Float> task) -> {
            GoogleApiClientToken token = getGoogleApiClientToken();
            if (token == null) {
                throw new IllegalStateException();
            }

            return token.executeGoogleApi(new GoogleApiTask<Float>() {
                @Override
                public Float executeTask(GoogleApiClient client) throws Exception {
                    float userWeight = GoogleApiUtil.getUserWeightFromFit(client);
                    if (userWeight > 0 && userWeight != mPersonalDataSettings.getUserWeight()) {
                        mPersonalDataSettings.setUserWeight(userWeight);
                        mPersonalDataSettings.commit();
                        toast(R.string.Setting_Fitness_Weight_SyncCompleted);
                    } else if (userWeight <= 0) {
                        toast(R.string.Setting_Fitness_Weight_SyncFailed);
                    }
                    return userWeight;
                }

                @Override
                public Float connectedFailed(GoogleApiClient client, ConnectionResult connectionResult) {
                    throw new IllegalStateException();
                }

                @Override
                public boolean isCanceled() {
                    return task.isCanceled();
                }
            });
        }).completed((weight, task) -> {
            updatePersonalUI();
        }).failed((err, task) -> {
            if (!googleFitFailedMessageBooted) {
                toast(R.string.Setting_Fitness_Weight_SyncFailed);
                googleFitFailedMessageBooted = true;
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
                mPersonalDataSettings.getNormalHeartrate(),
                new HeartrateInputListener() {
                    @Override
                    public void onInputHeartrate(int bpm) {
                        mPersonalDataSettings.setNormalHeartrate(bpm);
                        asyncCommitSettings();
                    }
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
