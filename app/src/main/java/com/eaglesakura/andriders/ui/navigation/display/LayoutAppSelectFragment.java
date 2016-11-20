package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.android.framework.ui.support.annotation.BindInterface;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;

/**
 * レイアウト対象のアプリを選択するFragment
 */
@FragmentLayout(R.layout.display_setup_appselect)
public class LayoutAppSelectFragment extends AppFragment {

    @BindInterface
    Callback mCallback;


    public interface Callback {
        /**
         * 表示対象のアプリが選択された
         */
        void onApplicationSelected(LayoutAppSelectFragment fragment, DisplayLayoutApplication selected);

        /**
         * 削除がリクエストされた
         *
         * @param packageName 削除対象のパッケージ名
         */
        void onRequestDeleteLayout(LayoutAppSelectFragment fragment, DisplayLayoutApplication packageName);
    }
}
