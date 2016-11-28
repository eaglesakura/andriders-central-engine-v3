package com.eaglesakura.andriders.ui.navigation.profile;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.model.profile.RoadbikeWheelLength;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.material.widget.SpinnerAdapterBuilder;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

/**
 * ロードバイク基本設定用Fragment
 *
 * タイヤ規格設定
 */
@FragmentLayout(R.layout.profile_roadbike)
public class RoadbikeSettingFragment extends AppFragment {

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        AQuery q = new AQuery(self.getView());


        DataCollection<RoadbikeWheelLength> wheelLengthCollection = mAppSettings.getConfig().listWheelLength();
        SpinnerAdapterBuilder.from(q.id(R.id.Selector_WheelOuterLength).getSpinner(), RoadbikeWheelLength.class)
                .items(wheelLengthCollection.getSource())
                .title((index, item) -> item.getDisplayTitle(getContext()))
                .selection(item -> Math.abs(mAppSettings.getUserProfiles().getWheelOuterLength() - item.getOuterLength()) < 3)  // 近似する周長を初期選択
                .selected((index, item) -> onSelected(item))
                .build();
    }

    @UiThread
    void onSelected(@NonNull RoadbikeWheelLength length) {
        mAppSettings.getUserProfiles().setWheelOuterLength(length.getOuterLength());
        mAppSettings.commit();
    }
}
