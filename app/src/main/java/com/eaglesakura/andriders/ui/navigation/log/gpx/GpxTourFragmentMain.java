package com.eaglesakura.andriders.ui.navigation.log.gpx;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseActivity;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.apptour.AppTourDelegate;
import com.eaglesakura.android.framework.ui.BackStackManager;
import com.eaglesakura.android.util.ResourceUtil;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import icepick.State;

public class GpxTourFragmentMain extends BaseNavigationFragment implements AppTourDelegate.AppTourCompat, BackStackManager.BackStackFragment,
        TourFileSelectFragment.Listener, GpxTourImportProgressFragment.Listener {
    AppTourDelegate mAppTourDelegate = new AppTourDelegate(this);

    /**
     * 戻るボタンを封印する場合はtrue
     */
    boolean mBackButtonLock = false;

    @State
    Uri mSelectedFile;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mAppTourDelegate.getView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppTourDelegate.onCreate(savedInstanceState);
        getActivity(AppBaseActivity.class).getBackStackManager().push(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAppTourDelegate.rollbackThemeColor();
        getActivity(AppBaseActivity.class).getBackStackManager().pop(this);
    }

    @Override
    public void onTourInitialize(@NonNull AppTourDelegate self, @Nullable Bundle savedInstanceState) {
        self.addSlide(new TourFileSelectFragment(), ResourceUtil.argb(getContext(), R.color.EsMaterial_Red_300));
        self.addSlide(new GpxTourImportProgressFragment(), ResourceUtil.argb(getContext(), R.color.EsMaterial_Blue_300));

        self.setSeparatorColor(Color.WHITE);
        self.setDoneButtonTextColor(Color.WHITE);
        self.setNextButtonColorToWhite();

        self.setSlideChangeListener((it, index, nextFragment) -> {
            if (nextFragment instanceof GpxTourImportProgressFragment) {
                ((GpxTourImportProgressFragment) nextFragment).startImport();
            }
        });

        self.setOnDoneClickListener((it) -> {
            mFragmentDelegate.detatchSelf(true);
        });

        self.hideSkip();
        self.hideDone();
        self.hideNext();
        self.setSwipeLock(true);
    }

    @Override
    public void onSelectedFile(TourFileSelectFragment self, @NonNull Uri path) {
        mSelectedFile = path;
        // 次へ行く
        mAppTourDelegate.showNext();
    }

    @NonNull
    @Override
    public Uri getGpxFilePath(GpxTourImportProgressFragment self) {
        return mSelectedFile;
    }

    @NonNull
    @Override
    public void onImportStart(GpxTourImportProgressFragment self) {
        mBackButtonLock = true;
    }

    @Override
    public void onImportComplete(GpxTourImportProgressFragment self) {
        mBackButtonLock = false;
        mAppTourDelegate.showDone();
    }

    @Override
    public void onImportFailed(GpxTourImportProgressFragment self) {
        mBackButtonLock = false;
        mAppTourDelegate.setCurrentSlide(mAppTourDelegate.getCurrentSlide() - 1);
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
    public boolean onBackPressed(KeyEvent event) {
        if (mBackButtonLock) {
            AppLog.widget("BackButton Locked");
            return true;
        }

        if (mAppTourDelegate.onBackPressed()) {
            return true;
        }
        return false;
    }
}
