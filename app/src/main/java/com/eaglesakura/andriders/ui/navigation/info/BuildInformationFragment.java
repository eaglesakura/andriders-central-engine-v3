package com.eaglesakura.andriders.ui.navigation.info;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.gen.prop.CentralServiceSettings;
import com.eaglesakura.andriders.gen.prop.DebugSettings;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.ui.widget.AppKeyValueView;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.device.external.Storage;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.license.LicenseViewActivity;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.margarine.OnCheckedChanged;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.util.PackageUtil;
import com.eaglesakura.util.StringUtil;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CompoundButton;

import java.io.File;
import java.util.Date;

/**
 * アプリのビルド時情報の表示を行う
 */
@FragmentLayout(R.layout.system_info_build)
public class BuildInformationFragment extends AppFragment {

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    @NonNull
    DebugSettings mDebugSettings;

    @NonNull
    CentralServiceSettings mServiceSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDebugSettings = mAppSettings.getDebugSettings();
        mServiceSettings = mAppSettings.getCentralSettings();
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        AQuery q = new AQuery(self.getView());
        q.id(R.id.Item_AppVersion).getView(AppKeyValueView.class).setValueText(BuildConfig.VERSION_NAME);
        q.id(R.id.Item_BuildDate).getView(AppKeyValueView.class).setValueText(BuildConfig.BUILD_DATE);
        q.id(R.id.Item_AceSDKVersion).getView(AppKeyValueView.class).setValueText(com.eaglesakura.andriders.sdk.BuildConfig.ACE_SDK_VERSION);
        q.id(R.id.Information_App_Debug).checked(mDebugSettings.isDebugEnable());

        if (mDebugSettings.isDebugEnable()) {
            q.id(R.id.Information_DebugSettings).visible();
        }
    }

    @OnClick(R.id.Button_Licenses)
    void clickOssLicense() {
        LicenseViewActivity.startContent(getActivity());
    }

    @OnCheckedChanged(R.id.Information_App_Debug)
    void changedAppDebug(CompoundButton button, boolean enabled) {
        if (mDebugSettings.isDebugEnable() == enabled) {
            return;
        }

        (new AQuery(getView()))
                .id(R.id.Information_DebugSettings).visibility(enabled ? View.VISIBLE : View.GONE);

        mDebugSettings.setDebugEnable(enabled);
        mAppSettings.commit();

        if (enabled) {
            AppDialogBuilder.newAlert(getContext(), R.string.Message_Debug_Enable)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(mLifecycleDelegate);
        }
    }

    /**
     * データをフルバックアップする
     */
    @OnClick(R.id.Debug_Data_Dump)
    void debugClickDataDump() {
        asyncUI(it -> {
            try (ProgressToken token = pushProgress(R.string.Word_Common_DataWrite)) {
                File dst = new File(Storage.getExternalDataStorage(getActivity()).getPath(), "/debug/" + StringUtil.toString(new Date()));
                PackageUtil.dumpPackageDataDirectory(getActivity(), dst);
            }
            return this;
        }).start();
    }
}
