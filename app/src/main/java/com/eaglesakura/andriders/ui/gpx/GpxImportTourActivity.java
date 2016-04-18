package com.eaglesakura.andriders.ui.gpx;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.info.InformationFragmentMain;
import com.eaglesakura.android.util.ResourceUtil;
import com.vlonjatg.android.apptourlibrary.AppTour;

import android.os.Bundle;
import android.support.annotation.Nullable;

public class GpxImportTourActivity extends AppTour {
    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        {
            InformationFragmentMain fragmentMain = InformationFragmentMain.createInstance(this);
            addSlide(fragmentMain, ResourceUtil.argb(getApplicationContext(), R.color.EsMaterial_Red_400));
        }
        {
            InformationFragmentMain fragmentMain = InformationFragmentMain.createInstance(this);
            addSlide(fragmentMain, ResourceUtil.argb(getApplicationContext(), R.color.EsMaterial_Green_400));
        }
        {
            InformationFragmentMain fragmentMain = InformationFragmentMain.createInstance(this);
            addSlide(fragmentMain, ResourceUtil.argb(getApplicationContext(), R.color.EsMaterial_Blue_400));
        }
        setImmersive(true);
    }

    @Override
    public void onSkipPressed() {
    }

    @Override
    public void onDonePressed() {
        setImmersive(false);
        finish();
    }
}
