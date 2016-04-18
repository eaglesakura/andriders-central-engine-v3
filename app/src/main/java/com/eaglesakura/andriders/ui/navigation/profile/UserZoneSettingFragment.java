package com.eaglesakura.andriders.ui.navigation.profile;


import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.v2.db.UserProfiles;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.ui.delegate.SupportFragmentDelegate;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.util.ResourceUtil;
import com.edmodo.rangebar.RangeBar;

public class UserZoneSettingFragment extends AppBaseFragment {

    final int MIN_CADENCE = 70;

    final int MIN_CRUISE_SPEED = 20;

    @Bind(R.id.Setting_RoadBikeProfile_CadenceZone_ZoneBar)
    RangeBar cadenceZoneBar;

    @Bind(R.id.Setting_RoadBikeProfile_CruiseZone_ZoneBar)
    RangeBar cruiseZoneBar;

    public UserZoneSettingFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_setting_userzone);
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        // 巡航速度設定
        {
            cruiseZoneBar.setBarColor(ResourceUtil.argb(getContext(), R.color.EsMaterial_LightGreen_Button_Normal));
            cruiseZoneBar.setConnectingLineColor(ResourceUtil.argb(getContext(), R.color.EsMaterial_LightGreen_Button_Focused));
            cruiseZoneBar.setThumbColorNormal(ResourceUtil.argb(getContext(), R.color.EsMaterial_LightGreen_Button_Normal));
            cruiseZoneBar.setThumbColorPressed(ResourceUtil.argb(getContext(), R.color.EsMaterial_LightGreen_Button_Pressed));
            cruiseZoneBar.setTickHeight(0);
            cruiseZoneBar.setTickCount(30);
        }
        {
            cadenceZoneBar.setBarColor(ResourceUtil.argb(getContext(), R.color.EsMaterial_LightGreen_Button_Normal));
            cadenceZoneBar.setConnectingLineColor(ResourceUtil.argb(getContext(), R.color.EsMaterial_LightGreen_Button_Focused));
            cadenceZoneBar.setThumbColorNormal(ResourceUtil.argb(getContext(), R.color.EsMaterial_LightGreen_Button_Normal));
            cadenceZoneBar.setThumbColorPressed(ResourceUtil.argb(getContext(), R.color.EsMaterial_LightGreen_Button_Pressed));
            cadenceZoneBar.setTickHeight(0);
            cadenceZoneBar.setTickCount(60);
        }

        UserProfiles profile = getSettings().getUserProfiles();
        cruiseZoneBar.setThumbIndices(profile.getSpeedZoneCruise() - MIN_CRUISE_SPEED, profile.getSpeedZoneSprint() - MIN_CRUISE_SPEED);
        cadenceZoneBar.setThumbIndices(profile.getCadenceZoneIdeal() - MIN_CADENCE, profile.getCadenceZoneHigh() - MIN_CADENCE);
        updateUI();

        cruiseZoneBar.setOnRangeBarChangeListener(cruiseZoneListener);
        cadenceZoneBar.setOnRangeBarChangeListener(cadenceZoneListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        asyncCommitSettings();
    }

    final RangeBar.OnRangeBarChangeListener cruiseZoneListener = (rangeBar, minValue, maxValue) -> {
        AppLog.widget("cruiseZone updated(%d <--> %d)", minValue, maxValue);
        minValue = Math.max(minValue, 1);
        maxValue = Math.max(maxValue, minValue + 1);

        UserProfiles profile = getSettings().getUserProfiles();

        profile.setSpeedZoneCruise(MIN_CRUISE_SPEED + minValue);
        profile.setSpeedZoneSprint(MIN_CRUISE_SPEED + maxValue);

        updateUI();
    };

    final RangeBar.OnRangeBarChangeListener cadenceZoneListener = (rangeBar, minValue, maxValue) -> {
        AppLog.widget("cadenceZone updated(%d -> %d)", minValue, maxValue);
        minValue = Math.max(minValue, 1);
        maxValue = Math.max(maxValue, minValue + 1);

        UserProfiles profile = getSettings().getUserProfiles();

        profile.setCadenceZoneIdeal(MIN_CADENCE + minValue);
        profile.setCadenceZoneHigh(MIN_CADENCE + maxValue);

        updateUI();

    };

    /**
     * UIを同期する
     */
    void updateUI() {
        UserProfiles profile = getSettings().getUserProfiles();

        AQuery q = new AQuery(getView());
        q.id(R.id.Setting_RoadBikeProfile_CruiseZone_MinValue).text(String.format("%d km/h", profile.getSpeedZoneCruise()));
        q.id(R.id.Setting_RoadBikeProfile_CruiseZone_MaxValue).text(String.format("%d km/h", profile.getSpeedZoneSprint()));

        q.id(R.id.Setting_RoadBikeProfile_CadenceZone_MinValue).text(String.format("%d rpm", profile.getCadenceZoneIdeal()));
        q.id(R.id.Setting_RoadBikeProfile_CadenceZone_MaxValue).text(String.format("%d rpm", profile.getCadenceZoneHigh()));
    }
}
