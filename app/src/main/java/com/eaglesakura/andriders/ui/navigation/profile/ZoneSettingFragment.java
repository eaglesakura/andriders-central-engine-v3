package com.eaglesakura.andriders.ui.navigation.profile;


import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.gen.prop.UserProfiles;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.edmodo.rangebar.RangeBar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * ゾーン設定を行う
 * ・巡航速度ゾーン
 * ・ケイデンスゾーン
 */
@FragmentLayout(R.layout.profile_userzone)
public class ZoneSettingFragment extends AppFragment {

    final int MIN_CADENCE = 70;

    final int MIN_CRUISE_SPEED = 20;

    @Bind(R.id.Range_Cadence)
    RangeBar mCadenceZoneBar;

    @Bind(R.id.Range_CruiseSpeed)
    RangeBar mCruiseZoneBar;

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        UserProfiles profile = mAppSettings.getUserProfiles();
        mCruiseZoneBar.setThumbIndices(profile.getSpeedZoneCruise() - MIN_CRUISE_SPEED, profile.getSpeedZoneSprint() - MIN_CRUISE_SPEED);
        mCruiseZoneBar.setOnRangeBarChangeListener(mCruiseZoneListenerImpl);

        mCadenceZoneBar.setThumbIndices(profile.getCadenceZoneIdeal() - MIN_CADENCE, profile.getCadenceZoneHigh() - MIN_CADENCE);
        mCadenceZoneBar.setOnRangeBarChangeListener(mCadenceZoneListenerImpl);

        return view;
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
        q.id(R.id.Item_SpeedMin).text(String.format("%d km/h", profile.getSpeedZoneCruise()));
        q.id(R.id.Item_SpeedMax).text(String.format("%d km/h", profile.getSpeedZoneSprint()));

        q.id(R.id.Item_CadenceMin).text(String.format("%d rpm", profile.getCadenceZoneIdeal()));
        q.id(R.id.Item_CadenceMax).text(String.format("%d rpm", profile.getCadenceZoneHigh()));
    }
}
