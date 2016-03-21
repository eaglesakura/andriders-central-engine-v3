package com.eaglesakura.andriders.ui.navigation.info;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.v2.db.CentralServiceSettings;
import com.eaglesakura.andriders.v2.db.DebugSettings;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.device.external.StorageInfo;
import com.eaglesakura.android.framework.ui.license.LicenseViewActivity;
import com.eaglesakura.android.margarine.OnCheckedChanged;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.util.PackageUtil;
import com.eaglesakura.material.widget.MaterialAlertDialog;
import com.eaglesakura.util.StringUtil;

import android.view.View;
import android.widget.CompoundButton;

import java.io.File;
import java.util.Date;

public class BuildInformationFragment extends AppBaseFragment {

    DebugSettings debugSettings = Settings.getInstance().getDebugSettings();

    CentralServiceSettings serviceSettings = Settings.getInstance().getCentralSettings();

    public BuildInformationFragment() {
        requestInjection(R.layout.fragment_information_build);
    }

    @Override
    protected void onAfterViews() {
        super.onAfterViews();

        AQuery q = new AQuery(getView());
        q.id(R.id.Information_App_Version).text(BuildConfig.VERSION_NAME);
        q.id(R.id.Information_App_BuildDate).text(BuildConfig.BUILD_DATE);
        q.id(R.id.Information_App_SDKVersion).text(com.eaglesakura.andriders.sdk.BuildConfig.ACE_SDK_VERSION);
        q.id(R.id.Information_App_Debug).checked(debugSettings.getDebugEnable());

        if (debugSettings.getDebugEnable()) {
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

        if (enabled == debugSettings.getDebugEnable()) {
            return;
        }

        debugSettings.setDebugEnable(enabled);
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
     * debug rendering
     */
    @OnCheckedChanged(R.id.Debug_ACEs_DebugRendering)
    void debugCheckAcesDebugRendering(CompoundButton button, boolean checked) {
        debugSettings.setAcesRenderDebugInfo(checked);
        asyncCommitSettings();
    }

    /**
     * loc rendering
     */
    @OnCheckedChanged(R.id.Debug_Location_Rendering)
    void debugCheckLocationRendering(CompoundButton button, boolean checked) {
        debugSettings.setRenderLocation(checked);
        asyncCommitSettings();
    }

    /**
     * データをフルバックアップする
     */
    @OnClick(R.id.Debug_Data_Dump)
    void debugClickDataDump() {
        pushProgress("pull");
        asyncUI(it -> {
            try {
                File dst = new File(StorageInfo.getExternalStorageRoot(getActivity()), "/debug/" + StringUtil.toString(new Date()));
                PackageUtil.dumpPackageDataDirectory(getActivity(), dst);
            } finally {
                popProgress();
            }
            return this;
        }).start();
    }
}
