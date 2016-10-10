package com.eaglesakura.andriders.ui.navigation.session;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;

/**
 * ユーザーの簡易ナビゲートを行う
 */
public class NavigationMapFragment extends AppFragment {

//    FragmentHolder<SupportMapFragment> mMapFragment = new FragmentHolder<SupportMapFragment>(this, R.id.ViewHolder_GoogleMap, SupportMapFragment.class.getSimpleName()) {
//        @NonNull
//        @Override
//        protected SupportMapFragment newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception {
//            return null;
//        }
//    }.bind(mLifecycleDelegate);

    public NavigationMapFragment() {
        mFragmentDelegate.setLayoutId(R.layout.session_info_navigation);
    }
}
