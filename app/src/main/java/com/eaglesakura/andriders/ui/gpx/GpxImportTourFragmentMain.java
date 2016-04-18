package com.eaglesakura.andriders.ui.gpx;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;
import com.eaglesakura.andriders.ui.navigation.info.InformationFragmentMain;
import com.eaglesakura.android.util.ResourceUtil;
import com.eaglesakura.android.apptour.AppTourDelegate;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GpxImportTourFragmentMain extends BaseNavigationFragment implements AppTourDelegate.AppTourCompat {

    AppTourDelegate mTourDelegate = new AppTourDelegate(this);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mTourDelegate.getView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTourDelegate.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Activity getActivity(@NonNull AppTourDelegate self) {
        return getActivity();
    }

    @NonNull
    @Override
    public FragmentManager getFragmentManager(@NonNull AppTourDelegate self) {
        return getChildFragmentManager();
    }

    @NonNull
    @Override
    public LayoutInflater getLayoutInflater(@NonNull AppTourDelegate self) {
        return getActivity().getLayoutInflater();
    }

    @Override
    public void onTourInitialize(@NonNull AppTourDelegate self, @Nullable Bundle savedInstanceState) {
        {
            InformationFragmentMain fragmentMain = InformationFragmentMain.createInstance(getActivity());
            mTourDelegate.addSlide(fragmentMain, ResourceUtil.argb(getContext(), R.color.EsMaterial_Red_400));
        }
        {
            InformationFragmentMain fragmentMain = InformationFragmentMain.createInstance(getActivity());
            mTourDelegate.addSlide(fragmentMain, ResourceUtil.argb(getContext(), R.color.EsMaterial_Green_400));
        }
        {
            InformationFragmentMain fragmentMain = InformationFragmentMain.createInstance(getActivity());
            mTourDelegate.addSlide(fragmentMain, ResourceUtil.argb(getContext(), R.color.EsMaterial_Blue_400));
        }
//        mTourDelegate.setImmersive(true);
        mTourDelegate.setSwipeLock(false);
        mTourDelegate.hideSkip();
    }

    @Override
    public void onClickTourSkip(@NonNull AppTourDelegate self, int tourPosition) {

    }

    @Override
    public void onClickTourDone(@NonNull AppTourDelegate self) {
        mTourDelegate.rollbackThemeColor();
        getActivity().finish();
    }

    public static GpxImportTourFragmentMain newInstance(Context context) {
        return new GpxImportTourFragmentMain();
    }
}
