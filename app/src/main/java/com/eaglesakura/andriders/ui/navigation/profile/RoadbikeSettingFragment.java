package com.eaglesakura.andriders.ui.navigation.profile;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.model.profile.RoadbikeWheelLength;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.view.builder.SpinnerAdapterBuilder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * ロードバイク基本設定用Fragment
 *
 * タイヤ規格設定
 */
@FragmentLayout(R.layout.profile_roadbike)
public class RoadbikeSettingFragment extends AppFragment {

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        AQuery q = new AQuery(view);
        DataCollection<RoadbikeWheelLength> wheelLengthCollection = mAppSettings.getConfig().listWheelLength();
        SpinnerAdapterBuilder.from(q.id(R.id.Selector_WheelOuterLength).getSpinner(), RoadbikeWheelLength.class)
                .items(wheelLengthCollection.getSource())
                .title((index, item) -> item.getDisplayTitle(getContext()))
                .selection(item -> Math.abs(mAppSettings.getUserProfiles().getWheelOuterLength() - item.getOuterLength()) < 3)  // 近似する周長を初期選択
                .selected((index, item) -> onSelected(item))
                .build();
        return view;
    }

    @UiThread
    void onSelected(@NonNull RoadbikeWheelLength length) {
        mAppSettings.getUserProfiles().setWheelOuterLength(length.getOuterLength());
        mAppSettings.commit();
    }
}
