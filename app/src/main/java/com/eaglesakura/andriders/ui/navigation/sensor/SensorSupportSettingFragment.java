package com.eaglesakura.andriders.ui.navigation.sensor;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.margarine.OnCheckedChanged;
import com.eaglesakura.material.widget.SpinnerAdapterBuilder;

import android.widget.Spinner;

/**
 * センサーの補助設定用Fragment
 */
@FragmentLayout(R.layout.sensor_gadget_support)
public class SensorSupportSettingFragment extends AppFragment {

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    /**
     * GPS精度設定
     */
    @Bind(R.id.Selector_GpsAccuracy)
    Spinner mGpsAccuracy;

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        AQuery q = new AQuery(self.getView());
        q.id(R.id.Button_KillWiFi).checked(mAppSettings.getCentralSettings().isWifiDisable());
        q.id(R.id.Button_GpsSpeedEnable).checked(mAppSettings.getCentralSettings().isGpsSpeedEnable());

        // GPS精度選択を行う
        SpinnerAdapterBuilder.from(getContext(), mGpsAccuracy, Integer.class)
                .items(mAppSettings.getConfig().getSensor().getGpsAccuracyMeterList())
                .title((index, value) -> getString(R.string.Word_Gadget_GpsAccuracySelector, value))
                .selection(value -> Math.abs(value - (int) mAppSettings.getCentralSettings().getGpsAccuracy()) < 5)
                .selected(value -> {
                    mAppSettings.getCentralSettings().setGpsAccuracy(value);
                    mAppSettings.commit();
                })
                .build();
    }

    /**
     * Wi-Fi強制OFFオプションが変更された
     *
     * @param checked 強制OFFにする場合true
     */
    @OnCheckedChanged(R.id.Button_KillWiFi)
    void checkedKillWiFi(boolean checked) {
        mAppSettings.getCentralSettings().setWifiDisable(checked);
        mAppSettings.commit();
    }

    /**
     * GPS速度オプションが変更された
     *
     * @param checked GPS速度を有効化する場合true
     */
    @OnCheckedChanged(R.id.Button_GpsSpeedEnable)
    void checkedGpsSpeed(boolean checked) {
        mAppSettings.getCentralSettings().setGpsSpeedEnable(checked);
        mAppSettings.commit();
    }
}
