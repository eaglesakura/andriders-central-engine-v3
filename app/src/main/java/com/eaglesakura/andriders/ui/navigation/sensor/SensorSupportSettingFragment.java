package com.eaglesakura.andriders.ui.navigation.sensor;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.OnCheckedChanged;

/**
 * センサーの補助設定用Fragment
 */
@FragmentLayout(R.layout.sensor_gadget_support)
public class SensorSupportSettingFragment extends AppFragment {

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        AQuery q = new AQuery(self.getView());
        q.id(R.id.Button_KillWiFi).checked(mAppSettings.getCentralSettings().isWifiDisable());

    }

    /**
     * Wi-Fi強制OFFオプションが変更された
     *
     * @param checked 強制OFFにする場合tru
     */
    @OnCheckedChanged(R.id.Button_KillWiFi)
    void checkedKillWiFi(boolean checked) {
        mAppSettings.getCentralSettings().setWifiDisable(checked);
        mAppSettings.commit();
    }
}
