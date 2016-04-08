package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

/**
 * ログ表示画面のメインFragment
 */
public class UserLogMain extends BaseNavigationFragment {
    public UserLogMain() {
        requestInjection(R.layout.fragment_userlog_main);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            // メニューを追加する
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            {
                LogImportFragment fragment = new LogImportFragment();
                transaction.add(fragment, fragment.createSimpleTag());
            }
            transaction.commit();
        }
    }

    public static UserLogMain newInstance(Context context) {
        return new UserLogMain();
    }
}
