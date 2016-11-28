package com.eaglesakura.andriders.ui.navigation.info;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;


/**
 * アプリ情報画面を管理する。
 * <p/>
 * * Central設定
 * <p/>
 * * ビルド情報
 */
@FragmentLayout(R.layout.system_fragment_stack)
public class InformationFragmentMain extends AppFragment {

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
}
