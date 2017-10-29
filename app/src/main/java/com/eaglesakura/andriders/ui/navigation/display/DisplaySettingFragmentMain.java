package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.ui.navigation.base.AppNavigationFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.sloth.annotation.BindInterface;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.app.FragmentHolder;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;
import com.eaglesakura.sloth.data.SupportCancelCallbackBuilder;
import com.eaglesakura.sloth.ui.progress.ProgressToken;

import android.os.Bundle;
import android.support.annotation.UiThread;

/**
 * 表示値のレイアウト用Fragment
 */
@FragmentLayout(R.layout.display_setup)
public class DisplaySettingFragmentMain extends AppNavigationFragment implements LayoutAppSelectFragment.Callback, LayoutEditFragment.Callback {

    @BindInterface
    Callback mCallback;

    /**
     * アプリ選択Fragment
     */
    FragmentHolder<LayoutAppSelectFragment> mLayoutAppSelectFragment = FragmentHolder.newInstance(this, LayoutAppSelectFragment.class, R.id.Content_Holder_AppSelector);

    /**
     * レイアウト編集
     */
    FragmentHolder<LayoutEditFragment> mLayoutEditFragment = FragmentHolder.newInstance(this, LayoutEditFragment.class, R.id.Content_Holder_DisplayLayout);

    DisplayLayoutController mDisplayLayoutController;

    DisplayLayoutController.Bus mDisplayLayoutControllerBus;

    @Override
    protected void onCreateLifecycle(FragmentLifecycle lifecycle) {
        super.onCreateLifecycle(lifecycle);
        mLayoutAppSelectFragment.bind(lifecycle);
        mLayoutEditFragment.bind(lifecycle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDisplayLayoutController = new DisplayLayoutController(getContext());
        mDisplayLayoutControllerBus = new DisplayLayoutController.Bus(mDisplayLayoutController);
        mDisplayLayoutControllerBus.bind(getFragmentLifecycle(), mLayoutAppSelectFragment.get());
        mDisplayLayoutControllerBus.bind(getFragmentLifecycle(), mLayoutEditFragment.get());
        loadDisplayController();
    }

    @UiThread
    void loadDisplayController() {
        asyncQueue(task -> {
            SupportCancelCallbackBuilder.CancelChecker checker = SupportCancelCallbackBuilder.from(task).build();
            try (ProgressToken token = pushProgress(R.string.Word_Common_DataLoad)) {
                mDisplayLayoutController.load(checker);
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
        asyncQueue(task -> {
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
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getFragmentLifecycle());
        }).start();
    }

    @Override
    public void onRequestDeleteLayout(LayoutAppSelectFragment fragment, DisplayLayoutApplication app) {
        // アプリ削除
        asyncQueue(task -> {
            try (ProgressToken token = pushProgress(R.string.Word_Common_Save)) {
                mDisplayLayoutController.remove(app.getPackageName());
                return this;
            }
        }).completed((result, task) -> {
            // 切り替えを許可する
            mDisplayLayoutControllerBus.onSelected(mDisplayLayoutController.getDefaultApplication());
        }).failed((error, task) -> {
            AppLog.report(error);
            AppDialogBuilder.newError(getContext(), error)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getFragmentLifecycle());
        }).start();
    }

    @Override
    public void onUpdateLayout(LayoutEditFragment self) {
        // レイアウト情報の保存を行う
        asyncQueue(task -> {
            try (ProgressToken token = pushProgress(R.string.Word_Common_Save)) {
                mDisplayLayoutController.commit();
                return this;
            }
        }).failed((error, task) -> {
            AppLog.report(error);
            AppDialogBuilder.newError(getContext(), error)
                    .positiveButton(R.string.Word_Common_OK, null)
                    .show(getFragmentLifecycle());
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
