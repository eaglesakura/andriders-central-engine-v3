package com.eaglesakura.andriders.ui.navigation.profile;


import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.gen.prop.UserProfiles;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.edmodo.rangebar.RangeBar;

import android.support.annotation.UiThread;


/**
 * ゾーン設定を行う
 * ・巡航速度ゾーン
 * ・ケイデンスゾーン
 */
@FragmentLayout(R.layout.profile_userzone)
public class UserZoneSettingFragment extends AppFragment {

    final int MIN_CADENCE = 70;

    final int MIN_CRUISE_SPEED = 20;

    @Bind(R.id.Setting_RoadBikeProfile_CadenceZone_ZoneBar)
    RangeBar cadenceZoneBar;

    @Bind(R.id.Setting_RoadBikeProfile_CruiseZone_ZoneBar)
    RangeBar cruiseZoneBar;

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        UserProfiles profile = mAppSettings.getUserProfiles();
        cruiseZoneBar.setThumbIndices(profile.getSpeedZoneCruise() - MIN_CRUISE_SPEED, profile.getSpeedZoneSprint() - MIN_CRUISE_SPEED);
        cadenceZoneBar.setThumbIndices(profile.getCadenceZoneIdeal() - MIN_CADENCE, profile.getCadenceZoneHigh() - MIN_CADENCE);

        cruiseZoneBar.setOnRangeBarChangeListener(mCruiseZoneListenerImpl);
        cadenceZoneBar.setOnRangeBarChangeListener(mCadenceZoneListenerImpl);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAppSettings.commit();
    }

    /**
     * 巡航速度ゾーンを設定する
     */
    final RangeBar.OnRangeBarChangeListener mCruiseZoneListenerImpl = (rangeBar, minValue, maxValue) -> {
        AppLog.widget("cruiseZone updated(%d <--> %d)", minValue, maxValue);
        minValue = Math.max(minValue, 1);
        maxValue = Math.max(maxValue, minValue + 1);

        UserProfiles profile = mAppSettings.getUserProfiles();

        profile.setSpeedZoneCruise(MIN_CRUISE_SPEED + minValue);
        profile.setSpeedZoneSprint(MIN_CRUISE_SPEED + maxValue);

        updateUI();
    };

    /**
     * ケイデンスゾーンを設定する
     */
    final RangeBar.OnRangeBarChangeListener mCadenceZoneListenerImpl = (rangeBar, minValue, maxValue) -> {
        AppLog.widget("cadenceZone updated(%d -> %d)", minValue, maxValue);
        minValue = Math.max(minValue, 1);
        maxValue = Math.max(maxValue, minValue + 1);

        UserProfiles profile = mAppSettings.getUserProfiles();

        profile.setCadenceZoneIdeal(MIN_CADENCE + minValue);
        profile.setCadenceZoneHigh(MIN_CADENCE + maxValue);

        updateUI();
    };

    /**
     * UIを同期する
     */
    @UiThread
    void updateUI() {
        UserProfiles profile = mAppSettings.getUserProfiles();

        AQuery q = new AQuery(getView());
        q.id(R.id.Setting_RoadBikeProfile_CruiseZone_MinValue).text(String.format("%d km/h", profile.getSpeedZoneCruise()));
        q.id(R.id.Setting_RoadBikeProfile_CruiseZone_MaxValue).text(String.format("%d km/h", profile.getSpeedZoneSprint()));

        q.id(R.id.Setting_RoadBikeProfile_CadenceZone_MinValue).text(String.format("%d rpm", profile.getCadenceZoneIdeal()));
        q.id(R.id.Setting_RoadBikeProfile_CadenceZone_MaxValue).text(String.format("%d rpm", profile.getCadenceZoneHigh()));
    }
}
