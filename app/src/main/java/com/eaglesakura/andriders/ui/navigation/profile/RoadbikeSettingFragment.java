package com.eaglesakura.andriders.ui.navigation.profile;

import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.v2.db.UserProfiles;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.material.widget.MaterialInputDialog;

import android.support.annotation.UiThread;
import android.widget.EditText;
import android.widget.TextView;

public class RoadbikeSettingFragment extends AppBaseFragment {

    @Bind(R.id.Setting_RoadBikeProfile_WheelSetting_Value)
    TextView mWheelOuterLengthValue;

    public RoadbikeSettingFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_setting_roadbike);
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);
        updateUI();
    }

    @UiThread
    void updateUI() {
        mWheelOuterLengthValue.setText(String.valueOf(getSettings().getUserProfiles().getWheelOuterLength()));
    }

    @OnClick(R.id.Setting_RoadBikeProfile_WheelSetting)
    void clickWheelSetting() {
        MaterialInputDialog dialog = new MaterialInputDialog(getActivity()) {
            @Override
            protected void onInitializeViews(TextView header, EditText input, TextView fooder) {
                ViewUtil.setInputIntegerOnly(input);
                fooder.setText(" mm");
                input.setText("" + getSettings().getUserProfiles().getWheelOuterLength());
            }

            @Override
            protected void onCommit(EditText input) {
                UserProfiles userProfiles = getSettings().getUserProfiles();
                int length = (int) ViewUtil.getLongValue(input, userProfiles.getWheelOuterLength());
                if (length > AppUtil.WHEEL_LENGTH_MAX) {
                    toast(getString(R.string.Setting_Roadbike_MaxWheelLength, AppUtil.WHEEL_LENGTH_MAX));
                } else {
                    userProfiles.setWheelOuterLength((int) ViewUtil.getLongValue(input, userProfiles.getWheelOuterLength()));
                    asyncCommitSettings();
                }
                updateUI();
            }
        };
        dialog.setTitle(getString(R.string.Setting_Roadbike_WheelOuterLength));
        dialog.show();
    }

}
