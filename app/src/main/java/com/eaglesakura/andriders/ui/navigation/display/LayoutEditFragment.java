package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.data.display.DisplayLayoutManager;
import com.eaglesakura.andriders.databinding.DisplaySetupSelectorBinding;
import com.eaglesakura.andriders.databinding.DisplaySetupSelectorRowBinding;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.model.display.DisplayLayoutGroup;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.material.widget.support.SupportRecyclerView;
import com.squareup.otto.Subscribe;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 実際のディスプレイレイアウト変更を行うFragment
 */
public class LayoutEditFragment extends AppFragment {

    DisplayLayoutController mDisplayLayoutController;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = DisplayLayoutManager.newStubLayout(inflater.getContext());
        return view;
    }

    /**
     * アプリが切り替えられた
     */
    @Subscribe
    void onSelectedApp(DisplayLayoutController.Bus bus) {
        mDisplayLayoutController = bus.getData();
        DisplayLayoutApplication selectedApp = mDisplayLayoutController.getSelectedApp();
        DisplayLayoutGroup layoutGroup = mDisplayLayoutController.getLayoutGroup(selectedApp.getPackageName());

        // レイアウトを更新する
        for (DisplayLayout layout : layoutGroup.list()) {
            updateLayout(layout);
        }
    }

    @UiThread
    void updateLayout(DisplayLayout layout) {
        ViewGroup stub = ViewUtil.findViewByMatcher(getView(), view -> Integer.valueOf(layout.getSlotId()).equals(view.getTag(R.id.Tag_SlotId)));

        // 一旦個をすべて削除する
        AppCompatButton button;
        if (stub.getChildCount() == 0) {
            // ボタンを生成する
            button = new AppCompatButton(getContext());
            stub.addView(button);
        } else {
            // ボタンを取得する
            button = ViewUtil.findViewByMatcher(stub, it -> it instanceof AppCompatButton);
        }

        // キーを検索する
        DisplayKey information = mDisplayLayoutController.getDisplayKey(layout);
        if (information == null) {
            if (layout.hasValue()) {
                // 情報を見失った
                button.setText(R.string.Word_Display_ErrorSlot);
            }
        } else {
            button.setText(information.getTitle());
        }

        // リスナを指定する
        button.setOnClickListener(view -> showSelectorDialog(layout));
    }

    /**
     * View選択用ダイアログを開く
     */
    @UiThread
    void showSelectorDialog(DisplayLayout layout) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.display_setup_appselect_dialog, null, false);
        Dialog dialog = AppDialogBuilder.newCustomContent(getContext(), getString(R.string.Title_Launcher_ChooseApp), view)
                .fullScreen(true)
                .show(mLifecycleDelegate);

        SupportRecyclerView supportRecyclerView = ViewUtil.findViewByMatcher(view, it -> (it instanceof SupportRecyclerView));
        CardAdapter<CentralPlugin> adapter = newSelectorAdapter((plugin, displayKey) -> {
            dialog.dismiss();
            setDisplayLayout(layout, plugin, displayKey);
        });
        adapter.getCollection().addAll(mDisplayLayoutController.listPlugins().list());
        supportRecyclerView.setAdapter(adapter, false);
    }


    /**
     * 表示する値が選択された
     *
     * @param slot   保存対象スロット
     * @param plugin プラグイン
     * @param value  表示対象値
     */
    @UiThread
    private void setDisplayLayout(DisplayLayout slot, CentralPlugin plugin, DisplayKey value) {
        // 新しいレイアウト設定を構築する
        DisplayLayout layout = new DisplayLayout.Builder(slot)
                .application(mDisplayLayoutController.getSelectedApp().getPackageName())
                .bind(plugin.getInformation().getId(), value.getId())
                .build();

        // レイアウト構成を反映
        updateLayout(layout);
    }

    /**
     * DisplayKey選択用のアダプタを生成する
     */
    CardAdapter<CentralPlugin> newSelectorAdapter(OnDisplayKeyClickListener listener) {
        return new CardAdapter<CentralPlugin>() {
            @Override
            protected View onCreateCard(ViewGroup parent, int viewType) {
                return DisplaySetupSelectorBinding.inflate(LayoutInflater.from(getContext()), null, false).getRoot();
            }

            @Override
            protected void onBindCard(CardBind<CentralPlugin> bind, int position) {
                CentralPlugin item = bind.getItem();
                DisplaySetupSelectorBinding rootBinding = bind.getBinding();
                rootBinding.setItem(new PluginBind() {
                    @Override
                    public Drawable getIcon() {
                        return item.loadIcon();
                    }

                    @Override
                    public String getTitle() {
                        return item.getName();
                    }
                });

                for (DisplayKey key : item.listDisplayKeys().list()) {
                    attachKeySelectorView(rootBinding.Content, item, key);
                }
            }

            /**
             * DisplayKey選択用UIを生成する。
             * タップができるのはDisplayKey単位であるので、コレがRootとなる
             * @param key
             * @return
             */
            void attachKeySelectorView(ViewGroup attach, CentralPlugin plugin, DisplayKey key) {
                DisplaySetupSelectorRowBinding inflate = DisplaySetupSelectorRowBinding.inflate(LayoutInflater.from(getContext()), attach, true);
                inflate.setItem(new DisplayKeyBind() {
                    @Override
                    public String getTitle() {
                        return key.getTitle();
                    }

                    @Override
                    public String getSummary() {
                        return key.getSummary();
                    }
                });
                inflate.Button.setOnClickListener(view -> {
                    listener.onSelected(plugin, key);
                });
            }
        };
    }

    private interface OnDisplayKeyClickListener {
        void onSelected(CentralPlugin plugin, DisplayKey selectedKey);
    }

    public interface PluginBind {
        Drawable getIcon();

        String getTitle();
    }

    /**
     * 表示するためのDisplayKey選択
     */
    public interface DisplayKeyBind {
        String getTitle();

        String getSummary();
    }
}
