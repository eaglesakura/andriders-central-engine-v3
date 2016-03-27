package com.eaglesakura.andriders.ui.navigation.log;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.BaseNavigationFragment;

import android.content.Context;

/**
 * ログ表示画面のメインFragment
 */
public class UserLogMain extends BaseNavigationFragment {
    public UserLogMain() {
        requestInjection(R.layout.fragment_userlog_main);
    }

    public static UserLogMain createInstance(Context context) {
        return new UserLogMain();
    }
}
