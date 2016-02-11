package com.eaglesakura.andriders.ui.navigation.profile;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.v2.db.UserProfiles;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.material.widget.MaterialInputDialog;

import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.OnClick;

public class RoadbikeSettingFragment extends AppBaseFragment {

    public RoadbikeSettingFragment() {
        requestInjection(R.layout.fragment_setting_roadbike);
    }

    @Bind(R.id.Setting_RoadBikeProfile_WheelSetting_Value)
    TextView wheelOuterLengthValue;

    @Override
    protected void onAfterViews() {
        super.onAfterViews();
        updateUI();
    }

    void updateUI() {
        wheelOuterLengthValue.setText(String.valueOf(getSettings().getUserProfiles().getWheelOuterLength()));
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
                if (length > AceUtils.WHEEL_LENGTH_MAX) {
                    toast(getString(R.string.Setting_Roadbike_MaxWheelLength, AceUtils.WHEEL_LENGTH_MAX));
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
