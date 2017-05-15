package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.data.display.DisplayLayoutManager;
import com.eaglesakura.andriders.databinding.DisplaySetupSelectorRowBinding;
import com.eaglesakura.andriders.model.display.DisplayLayout;
import com.eaglesakura.andriders.model.display.DisplayLayoutGroup;
import com.eaglesakura.andriders.plugin.CentralPlugin;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.ui.widget.AppDialogBuilder;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.sloth.annotation.BindInterface;
import com.eaglesakura.sloth.view.adapter.CardAdapter;
import com.eaglesakura.util.StringUtil;
import com.squareup.otto.Subscribe;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 実際のディスプレイレイアウト変更を行うFragment
 */
public class LayoutEditFragment extends AppFragment {

    DisplayLayoutController mDisplayLayoutController;

    @BindInterface
    Callback mCallback;

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
        ViewGroup stub = (ViewGroup) getView().findViewById(layout.getSlotId());

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
            } else {
                // テキストを空にする
                button.setText("");
            }
        } else {
            // タイトルを設定する
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
        Dialog dialog = AppDialogBuilder.newCustomContent(getContext(), getString(R.string.Title_Display_Choose), view)
                .fullScreen(true)
                .show(getLifecycle());

        RecyclerView recyclerView = ViewUtil.findViewByMatcher(view, it -> (it instanceof RecyclerView));
        CardAdapter<DisplayKeyBind> adapter = newSelectorAdapter((plugin, displayKey) -> {
            setDisplayLayout(layout, plugin, displayKey);
            mCallback.onUpdateLayout(this);
            dialog.dismiss();
        });
        adapter.getCollection().add(null);
        for (CentralPlugin plugin : mDisplayLayoutController.listPlugins().list()) {
            for (DisplayKey key : plugin.listDisplayKeys().list()) {
                adapter.getCollection().add(new DisplayKeyBindImpl(plugin, key));
            }
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);
    }


    /**
     * 表示する値を更新する
     *
     * @param slot   保存対象スロット
     * @param plugin プラグイン
     * @param value  表示対象値
     */
    @UiThread
    private void setDisplayLayout(DisplayLayout slot, CentralPlugin plugin, DisplayKey value) {
        // 新しいレイアウト設定を構築する
        DisplayLayout layout;
        if (plugin != null && value != null) {
            // プラグインにバインド
            layout = new DisplayLayout.Builder(slot)
                    .application(mDisplayLayoutController.getSelectedApp().getPackageName())
                    .bind(plugin.getInformation().getId(), value.getId())
                    .build();
        } else {
            // 削除する
            layout = new DisplayLayout.Builder(slot)
                    .application(mDisplayLayoutController.getSelectedApp().getPackageName())
                    .build();
        }
        mDisplayLayoutController.setLayout(layout);
        // レイアウト構成を反映
        updateLayout(layout);
    }

    /**
     * DisplayKey選択用のアダプタを生成する
     */
    private CardAdapter<DisplayKeyBind> newSelectorAdapter(OnDisplayKeyClickListener listener) {
        return new CardAdapter<DisplayKeyBind>() {

            @Override
            public int getItemViewType(int position) {
                if (position == 0) {
                    return 0;
                } else {
                    return DisplaySetupSelectorRowBinding.class.hashCode();
                }
            }

            @Override
            protected View onCreateCard(ViewGroup parent, int viewType) {
                if (viewType == DisplaySetupSelectorRowBinding.class.hashCode()) {
                    return DisplaySetupSelectorRowBinding.inflate(LayoutInflater.from(getContext()), null, false).getRoot();
                } else {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.display_setup_selector_remove, null, false);
                    view.findViewById(R.id.Button_Delete).setOnClickListener(it -> listener.onSelected(null, null));
                    return view;
                }
            }

            @Override
            protected void onBindCard(CardBind<DisplayKeyBind> bind, int position) {
                DisplayKeyBind item = bind.getItem();
                if (item != null) {
                    DisplaySetupSelectorRowBinding binding = bind.getBinding();
                    binding.setItem(item);
                    binding.Button.setOnClickListener(view -> {
                        DisplayKeyBindImpl impl = (DisplayKeyBindImpl) item;
                        listener.onSelected(impl.mPlugin, impl.mDisplayKey);
                    });
                }
            }
        };
    }

    private interface OnDisplayKeyClickListener {
        void onSelected(CentralPlugin plugin, DisplayKey selectedKey);
    }

    public interface Callback {
        /**
         * レイアウトが更新された
         */
        void onUpdateLayout(LayoutEditFragment self);
    }

    /**
     * 表示するためのDisplayKey選択
     */
    public interface DisplayKeyBind {
        String getPluginTitle();

        Drawable getPluginIcon();

        String getDisplayTitle();

        String getDisplaySummary();
    }

    class DisplayKeyBindImpl implements DisplayKeyBind {
        @NonNull
        final CentralPlugin mPlugin;

        @NonNull
        final DisplayKey mDisplayKey;

        DisplayKeyBindImpl(@NonNull CentralPlugin plugin, @NonNull DisplayKey displayKey) {
            mPlugin = plugin;
            mDisplayKey = displayKey;
        }

        @Override
        public String getPluginTitle() {
            return mPlugin.getName();
        }

        @Override
        public Drawable getPluginIcon() {
            return mPlugin.loadIcon();
        }

        @Override
        public String getDisplayTitle() {
            return mDisplayKey.getTitle();
        }

        @Override
        public String getDisplaySummary() {
            return StringUtil.trimSpacesOrEmpty(mDisplayKey.getSummary());
        }
    }
}
