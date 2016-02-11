package com.eaglesakura.andriders.ui.navigation.profile;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

/**
 * ユーザープロファイル画面を管理する。
 * <p/>
 * * ロードバイクデータ
 * <p/>
 * * ゾーン設定
 * <p/>
 * * センサー設定
 * <p/>
 * * パーソナルデータ
 */
public class ProfileFragmentMain extends BaseNavigationFragment {

    public ProfileFragmentMain() {
        requestInjection(R.layout.fragment_simiple_main);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            {
                RoadbikeSettingFragment fragment = new RoadbikeSettingFragment();
                transaction.add(R.id.Content_List_Root, fragment, fragment.createSimpleTag());
            }
            {
                UserZoneSettingFragment fragment = new UserZoneSettingFragment();
                transaction.add(R.id.Content_List_Root, fragment, fragment.createSimpleTag());
            }
            {
                FitnessSettingFragment fragment = new FitnessSettingFragment();
                transaction.add(R.id.Content_List_Root, fragment, fragment.createSimpleTag());
            }
            transaction.commit();
        }
    }

    public static ProfileFragmentMain createInstance(Context context) {
        return new ProfileFragmentMain();
    }
}
