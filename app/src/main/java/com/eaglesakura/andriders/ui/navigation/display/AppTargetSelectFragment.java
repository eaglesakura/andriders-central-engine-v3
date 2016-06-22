package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.saver.BundleState;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * 表示対象のアプリを選択するFragment
 */
public class AppTargetSelectFragment extends AppBaseFragment {

    @BundleState
    String mAppPackageName;

    Bitmap mIcon;

    Callback mCallback;

    public AppTargetSelectFragment() {
        mFragmentDelegate.setLayoutId(R.layout.fragment_setting_display_appselect);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = getParentOrThrow(Callback.class);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public interface Callback {
        /**
         * 表示対象のアプリが選択された
         */
        void onApplicationSelected(AppTargetSelectFragment fragment, AppInfo selected);
    }

    public static class AppInfo {
        private final Bitmap icon;
        private final String title;

        public AppInfo(Bitmap icon, String title) {
            this.icon = icon;
            this.title = title;
        }

        public Bitmap getIcon() {
            return icon;
        }

        public String getTitle() {
            return title;
        }
    }
}
