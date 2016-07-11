package com.eaglesakura.andriders.ui.navigation.info;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.NavigationBaseFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;


/**
 * アプリ情報画面を管理する。
 * <p/>
 * * Central設定
 * <p/>
 * * ビルド情報
 */
public class InformationFragmentMain extends NavigationBaseFragment {

    public InformationFragmentMain() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_simiple_main);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            {
                BuildInformationFragment fragment = new BuildInformationFragment();
                transaction.add(R.id.Content_List_Root, fragment, fragment.getClass().getName());
            }
            transaction.commit();
        }
    }

    @Nullable
    @Override
    public CharSequence getTitle() {
        return getString(R.string.Main_Menu_Information);
    }

    public static InformationFragmentMain newInstance(Context context) {
        return new InformationFragmentMain();
    }
}
