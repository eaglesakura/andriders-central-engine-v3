package com.eaglesakura.andriders.ui.widget;

import com.eaglesakura.andriders.data.res.AppImageLoader;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;

import android.content.Context;
import android.support.v4.app.FragmentTransaction;

/**
 * ImageLoaderを保持・管理するFragment
 */
public class ImageLoaderFragment extends AppFragment implements AppImageLoader.Holder {
    AppImageLoader mAppImageLoader;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mAppImageLoader == null) {
            mAppImageLoader = new AppImageLoader(context);
        }
    }

    @Override
    public AppImageLoader getImageLoader() {
        return mAppImageLoader;
    }

    public static FragmentTransaction attach(FragmentTransaction transaction) {
        transaction.add(new ImageLoaderFragment(), ImageLoaderFragment.class.getName());
        return transaction;
    }
}
