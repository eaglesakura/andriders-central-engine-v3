package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.android.framework.ui.FragmentHolder;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.annotation.BindInterface;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.lambda.CancelCallback;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.UiThread;

/**
 * 表示値のレイアウト用Fragment
 */
@FragmentLayout(R.layout.display_setup)
public class DisplaySettingFragmentMain extends AppNavigationFragment implements DisplayLayoutController.Holder, LayoutAppSelectFragment.Callback {

    @BindInterface
    Callback mCallback;

    /**
     * アプリ選択Fragment
     */
    FragmentHolder<LayoutAppSelectFragment> mLayoutAppSelectFragment = FragmentHolder.newInstance(this, LayoutAppSelectFragment.class, R.id.Content_Holder_AppSelector).bind(mLifecycleDelegate);

    DisplayLayoutController mDisplayLayoutController;

    DisplayLayoutApplication.Bus mSelectedAppBus = new DisplayLayoutApplication.Bus();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDisplayLayoutController = new DisplayLayoutController(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedAppBus.bind(mLifecycleDelegate, mLayoutAppSelectFragment.get());
        loadDisplayContrller();
    }

    @UiThread
    void loadDisplayContrller() {
        asyncUI(task -> {
            CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task);
            try (ProgressToken token = pushProgress(R.string.Widget_Common_Load)) {
                mDisplayLayoutController.load(cancelCallback);
            }
            return this;
        }).completed((result, task) -> {
            // デフォルトアプリを選択済みにする
            DisplayLayoutApplication defaultApp = mDisplayLayoutController.listSortedApplications().get(0);
            mSelectedAppBus.modified(defaultApp);
        }).failed((error, task) -> {
            mCallback.onInitializeFailed(this, error);
        }).start();
    }

    @Override
    public DisplayLayoutController getDisplayLayoutController() {
        return mDisplayLayoutController;
    }

    @Override
    public void onApplicationSelected(LayoutAppSelectFragment fragment, DisplayLayoutApplication selected) {
        // アプリ切り替えの送信を行う
        mSelectedAppBus.modified(selected);
    }

    @Override
    public void onRequestDeleteLayout(LayoutAppSelectFragment fragment, DisplayLayoutApplication packageName) {
        // TODO アプリ削除
    }

    public interface Callback {
        void onInitializeFailed(DisplaySettingFragmentMain self, Throwable error);
    }
}
