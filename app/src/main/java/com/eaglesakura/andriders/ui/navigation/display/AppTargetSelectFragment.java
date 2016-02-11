package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;

import android.content.Context;
import android.graphics.Bitmap;

import icepick.State;

/**
 * 表示対象のアプリを選択するFragment
 */
public class AppTargetSelectFragment extends AppBaseFragment {

    @State
    String appPackageName;

    Bitmap icon;

    Callback mCallback;

    public AppTargetSelectFragment() {
        requestInjection(R.layout.fragment_setting_display_appselect);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(getParentFragment() instanceof Callback)) {
            throw new IllegalStateException();
        } else {
            mCallback = (Callback) getParentFragment();
        }
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
