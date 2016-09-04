package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.BuildConfig;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.display.data.DataLayoutManager;
import com.eaglesakura.andriders.display.data.LayoutSlot;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginConnector;
import com.eaglesakura.andriders.plugin.PluginManager;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.saver.BundleState;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * サイコンの表示内容を並べるFragment
 */
public class DisplayLayoutSetFragment extends AppBaseFragment {

    DataLayoutManager mDisplaySlotManager;

    PluginManager mExtensionClientManager;

    List<DisplayKey> mDisplayValues = new ArrayList<>();

    @BundleState
    String mAppPackageName = BuildConfig.APPLICATION_ID;

    public DisplayLayoutSetFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = DataLayoutManager.newStubLayout(inflater.getContext());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExtensionClients();
    }

    @Override
    public void onPause() {
        super.onPause();
        mDisplayValues.clear();
        async(ExecuteTarget.LocalQueue, CallbackTime.FireAndForget, it -> {
            if (mExtensionClientManager != null) {
                mExtensionClientManager.disconnect();
            }
            return this;
        }).start();
    }

    /**
     * 拡張機能を読み込む
     */
    private void loadExtensionClients() {
        mDisplayValues.clear();
        async(ExecuteTarget.LocalQueue, CallbackTime.CurrentForeground, (BackgroundTask<PluginManager> it) -> {
            PluginManager clientManager = new PluginManager(getContext());
            try (ProgressToken token = pushProgress(R.string.Common_File_Load)) {
                clientManager.connect(PluginManager.ConnectMode.ActiveOnly);
            }
            return clientManager;
        }).completed((manager, task) -> {
            mExtensionClientManager = manager;
            loadDisplayData(mAppPackageName);
        }).failed((error, task) -> {
            error.printStackTrace();
        }).start();
    }

    /**
     * ディスプレイ表示内容を読み込む
     */
    public void loadDisplayData(final String newPackageName) {
        if (newPackageName.equals(mAppPackageName)) {
            return;
        }

        mAppPackageName = newPackageName;
        asyncUI((BackgroundTask<DataLayoutManager> it) -> {
            DataLayoutManager slotManager = null;

            try (ProgressToken token = pushProgress(R.string.Common_File_Load)) {
                // 拡張機能のアイコンを読み込む
                mDisplayValues.clear();
                for (PluginConnector client : mExtensionClientManager.listDisplayClients()) {
                    client.loadIcon();
                    for (DisplayKey info : client.getDisplayInformationList()) {
                        mDisplayValues.add(info);
                    }
                }

                slotManager = new DataLayoutManager(getActivity());
                slotManager.load(DataLayoutManager.Mode.Edit, newPackageName);
                return slotManager;
            }
        }).completed((slotManager, task) -> {
            AppLog.system("display_setup load completed :: %s", newPackageName);
            mDisplaySlotManager = slotManager;
            for (LayoutSlot slot : mDisplaySlotManager.listSlots()) {
                updateSlotPreview(mDisplaySlotManager, slot);
            }
        }).start();
    }

    /**
     * ディスプレイスロットを更新する
     */
    void updateSlotPreview(final DataLayoutManager slotManager, final LayoutSlot slot) {
        ViewGroup stub = findViewById(ViewGroup.class, slot.getId());

        // 一旦個をすべて削除する
        stub.removeAllViews();

        // ボタンを生成する
        AppCompatButton button = new AppCompatButton(getContext());
        if (slot.hasLink()) {
            // TODO 値のタイトルを入れる
            DisplayKey information = mExtensionClientManager.findDisplayInformation(slot.getExtensionId(), slot.getDisplayValueId());
            if (information == null) {
                // 情報を見失った
                button.setText("無効な表示内容");
            } else {
                button.setText(information.getTitle());
            }
        } else {
        }

        button.setOnClickListener(view -> showDisplaySelector(slotManager, slot));

        stub.addView(button, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    /**
     * 表示内容の選択ダイアログをブート
     */
    private void showDisplaySelector(final DataLayoutManager manager, final LayoutSlot slot) {
        List<PluginConnector> displayClients = mExtensionClientManager.listDisplayClients();

        final BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.widget_bottomsheet_root, null);

        // 非表示を加える
        {
            View view = inflater.inflate(R.layout.display_setup_selector_remove, null);
            view.setOnClickListener(it -> {
                onSelectedDisplay(manager, slot, null, null);
                dialog.dismiss();
            });
            ((ViewGroup) layout.findViewById(R.id.Widget_BottomSheet_Root)).addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        for (final PluginConnector client : displayClients) {
            AppLog.system("Display Extension name(%s)", client.getName());

            View extensionView = inflater.inflate(R.layout.display_setup_selector, null);

            AQuery q = new AQuery(extensionView);
            q.id(R.id.Extension_ItemSelector_ExtensionName).text(client.getName());
            q.id(R.id.Extension_ItemSelector_Icon).image(client.loadIcon());

            ViewGroup insertRoot = q.id(R.id.Extension_ItemSelector_Root).getView(ViewGroup.class);

            // Extensionごとの表示内容を並べる
            for (final DisplayKey info : client.getDisplayInformationList()) {
                View item = inflater.inflate(R.layout.display_setup_selector_row, null);
                ((TextView) item.findViewById(R.id.Extension_ItemSelector_Name)).setText(info.getTitle());
                insertRoot.addView(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                item.setOnClickListener(it -> {
                    onSelectedDisplay(manager, slot, client, info);
                    dialog.dismiss();
                });
            }

            ((ViewGroup) layout.findViewById(R.id.Widget_BottomSheet_Root)).addView(extensionView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.setContentView(layout);
        dialog.show();
    }

    /**
     * ディスプレイ表示内容が選択された
     *
     * @param slot        表示対象スロット
     * @param client      拡張機能
     * @param displayInfo 表示内容
     */
    private void onSelectedDisplay(DataLayoutManager manager, LayoutSlot slot, PluginConnector client, DisplayKey displayInfo) {

        if (client == null || displayInfo == null) {
            // 非表示にする
            manager.removeLayout(slot);
        } else {
            // スロットの値を上書き
            manager.setLayout(slot, client.getInformation(), displayInfo);
        }

        // 内容を保存
        asyncUI(it -> {
            manager.commit();
            return manager;
        }).start();

        // 再表示
        updateSlotPreview(manager, slot);
    }


    private String getUniqueId(PluginConnector client, DisplayKey display) {
        return String.format("%s|%s", client.getId(), display.getId());
    }
}
