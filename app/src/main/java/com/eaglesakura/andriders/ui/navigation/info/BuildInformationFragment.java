package com.eaglesakura.andriders.ui.navigation.info;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.gen.prop.CentralServiceSettings;
import com.eaglesakura.andriders.gen.prop.DebugSettings;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.ui.widget.AppKeyValueView;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.device.external.StorageInfo;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.license.LicenseViewActivity;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.margarine.OnCheckedChanged;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.util.PackageUtil;
import com.eaglesakura.material.widget.MaterialAlertDialog;
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
public class BuildInformationFragment extends AppBaseFragment {

    @NonNull
    DebugSettings mDebugSettings;

    @NonNull
    CentralServiceSettings mServiceSettings;

    public BuildInformationFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_information_build);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDebugSettings = getSettings().getDebugSettings();
        mServiceSettings = getSettings().getCentralSettings();
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        super.onAfterViews(self, flags);

        AQuery q = new AQuery(self.getView());
        q.id(R.id.Information_App_Version).getView(AppKeyValueView.class).setValueText(BuildConfig.VERSION_NAME);
        q.id(R.id.Information_App_BuildDate).getView(AppKeyValueView.class).setValueText(BuildConfig.BUILD_DATE);
        q.id(R.id.Information_App_SDKVersion).getView(AppKeyValueView.class).setValueText(com.eaglesakura.andriders.sdk.BuildConfig.ACE_SDK_VERSION);
        q.id(R.id.Information_App_Debug).checked(mDebugSettings.isDebugEnable());

        if (mDebugSettings.isDebugEnable()) {
            q.id(R.id.Information_DebugSettings).visible();
        }
    }

    @OnClick(R.id.Information_Licenses)
    void clickOssLicense() {
        LicenseViewActivity.startContent(getActivity());
    }

    @OnCheckedChanged(R.id.Information_App_Debug)
    void changedAppDebug(CompoundButton button, boolean enabled) {
        (new AQuery(getView()))
                .id(R.id.Information_DebugSettings).visibility(enabled ? View.VISIBLE : View.GONE);

        mDebugSettings.setDebugEnable(enabled);
        asyncCommitSettings();

        if (enabled) {
            MaterialAlertDialog dialog = new MaterialAlertDialog(getActivity());
            dialog.setTitle(R.string.Common_Worning);
            dialog.setMessage(R.string.Information_Debug_Enabled);
            dialog.setPositiveButton(R.string.Common_OK, null);
            dialog.show();
        }
    }

    /**
     * データをフルバックアップする
     */
    @OnClick(R.id.Debug_Data_Dump)
    void debugClickDataDump() {
        asyncUI(it -> {
            try (ProgressToken token = pushProgress("pull")) {
                File dst = new File(StorageInfo.getExternalStorageRoot(getActivity()), "/debug/" + StringUtil.toString(new Date()));
                PackageUtil.dumpPackageDataDirectory(getActivity(), dst);
            }
            return this;
        }).start();
    }
}
