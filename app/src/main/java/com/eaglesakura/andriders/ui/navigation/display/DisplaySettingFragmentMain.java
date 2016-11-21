package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppLog;
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
public class DisplaySettingFragmentMain extends AppNavigationFragment implements LayoutAppSelectFragment.Callback {

    @BindInterface
    Callback mCallback;

    /**
     * アプリ選択Fragment
     */
    FragmentHolder<LayoutAppSelectFragment> mLayoutAppSelectFragment = FragmentHolder.newInstance(this, LayoutAppSelectFragment.class, R.id.Content_Holder_AppSelector).bind(mLifecycleDelegate);

    /**
     * レイアウト編集
     */
    FragmentHolder<LayoutEditFragment> mLayoutEditFragment = FragmentHolder.newInstance(this, LayoutEditFragment.class, R.id.Content_Holder_DisplayLayout).bind(mLifecycleDelegate);

    DisplayLayoutController mDisplayLayoutController;

    DisplayLayoutController.Bus mDisplayLayoutControllerBus;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDisplayLayoutController = new DisplayLayoutController(getContext());
        mDisplayLayoutControllerBus = new DisplayLayoutController.Bus(mDisplayLayoutController);
        mDisplayLayoutControllerBus.bind(mLifecycleDelegate, mLayoutAppSelectFragment.get());
        mDisplayLayoutControllerBus.bind(mLifecycleDelegate, mLayoutEditFragment.get());
        loadDisplayController();
    }

    @UiThread
    void loadDisplayController() {
        asyncUI(task -> {
            CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task);
            try (ProgressToken token = pushProgress(R.string.Widget_Common_Load)) {
                mDisplayLayoutController.load(cancelCallback);
            }
            return this;
        }).completed((result, task) -> {
            if (!mDisplayLayoutController.hasDisplays()) {
                // 表示すべき内容が1個もない
                mCallback.onPluginNotEnabled(this);
            } else {
                // デフォルトアプリを選択済みにする
                DisplayLayoutApplication defaultApp = mDisplayLayoutController.getDefaultApplication();
                mDisplayLayoutControllerBus.onSelected(defaultApp);
            }
        }).failed((error, task) -> {
            mCallback.onInitializeFailed(this, error);
        }).start();
    }

    @Override
    public void onApplicationSelected(LayoutAppSelectFragment fragment, DisplayLayoutApplication selected) {
        // アプリ切り替えの送信を行う
        asyncUI(task -> {
            try (ProgressToken token = pushProgress(R.string.Word_Common_Working)) {
                mDisplayLayoutController.getLayoutGroup(selected.getPackageName());
                mDisplayLayoutController.commit();
                return this;
            }
        }).completed((result, task) -> {
            // 切り替えを許可する
            mDisplayLayoutControllerBus.onSelected(selected);
        }).failed((error, task) -> {
            AppLog.report(error);
            AppDialogBuilder.newError(getContext(), error)
                    .positiveButton(R.string.Common_OK, null)
                    .show(mLifecycleDelegate);
        }).start();
    }

    @Override
    public void onRequestDeleteLayout(LayoutAppSelectFragment fragment, DisplayLayoutApplication app) {
        // アプリ削除
        asyncUI(task -> {
            try (ProgressToken token = pushProgress(R.string.Word_Common_Working)) {
                mDisplayLayoutController.remove(app.getPackageName());
                return this;
            }
        }).completed((result, task) -> {
            // 切り替えを許可する
            mDisplayLayoutControllerBus.onSelected(mDisplayLayoutController.getDefaultApplication());
        }).failed((error, task) -> {
            AppLog.report(error);
            AppDialogBuilder.newError(getContext(), error)
                    .positiveButton(R.string.Common_OK, null)
                    .show(mLifecycleDelegate);
        }).start();
    }

    public interface Callback {

        /**
         * プラグインが1件も有効化されていない
         */
        void onPluginNotEnabled(DisplaySettingFragmentMain self);

        void onInitializeFailed(DisplaySettingFragmentMain self, Throwable error);
    }
}
