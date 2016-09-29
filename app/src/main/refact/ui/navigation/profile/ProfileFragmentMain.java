package com.eaglesakura.andriders.ui.navigation.profile;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.NavigationBaseFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

/**
 * ユーザープロファイル画面を管理する。
 * <p/>
 * * ロードバイクデータ
 * <p/>
 * * ゾーン設定
 * <p/>
 * * パーソナルデータ
 */
public class ProfileFragmentMain extends NavigationBaseFragment {

    public ProfileFragmentMain() {
        mFragmentDelegate.setLayoutId(R.layout.content_fragment_simiple);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getChildFragmentManager();
            {
                RoadbikeSettingFragment fragment = new RoadbikeSettingFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.Content_List_Root, fragment, fragment.getClass().getName())
                        .commit();
            }
            {
                UserZoneSettingFragment fragment = new UserZoneSettingFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.Content_List_Root, fragment, fragment.getClass().getName())
                        .commit();
            }
            {
                FitnessSettingFragment fragment = new FitnessSettingFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.Content_List_Root, fragment, fragment.getClass().getName())
                        .commit();
            }
        }
    }

    @Nullable
    @Override
    public CharSequence getTitle() {
        return getString(R.string.Main_Menu_Profile);
    }

    public static ProfileFragmentMain createInstance(Context context) {
        return new ProfileFragmentMain();
    }
}
