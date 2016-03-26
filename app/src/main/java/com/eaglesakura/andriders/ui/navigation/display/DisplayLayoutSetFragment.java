package com.eaglesakura.andriders.ui.navigation.display;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.extension.ExtensionClient;
import com.eaglesakura.andriders.extension.ExtensionClientManager;
import com.eaglesakura.andriders.display.data.DataLayoutManager;
import com.eaglesakura.andriders.display.data.LayoutSlot;
import com.eaglesakura.andriders.extension.DisplayInformation;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.material.widget.MaterialButton;
import com.eaglesakura.util.LogUtil;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
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

    String appPackageName;

    ExtensionClientManager mExtensionClientManager;

    List<DisplayInformation> displayValues = new ArrayList<>();

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
        displayValues.clear();
        async(SubscribeTarget.Pipeline, ObserveTarget.FireAndForget, it -> {
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
        displayValues.clear();
        async(SubscribeTarget.Pipeline, ObserveTarget.CurrentForeground, (RxTask<ExtensionClientManager> it) -> {
            ExtensionClientManager clientManager = new ExtensionClientManager(getContext());
            try {
                pushProgress(R.string.Common_File_Load);
                clientManager.connect(ExtensionClientManager.ConnectMode.Enabled);
            } finally {
                popProgress();
            }
            return clientManager;
        }).completed((manager, task) -> {
            mExtensionClientManager = manager;
            loadDisplayDatas(appPackageName);
        }).start();
    }

    /**
     * ディスプレイ表示内容を読み込む
     */
    private void loadDisplayDatas(final String newPackageName) {
        asyncUI((RxTask<DataLayoutManager> it) -> {
            DataLayoutManager slotManager = null;
            pushProgress(R.string.Common_File_Load);
            // 拡張機能のアイコンを読み込む
            displayValues.clear();
            for (ExtensionClient client : mExtensionClientManager.listDisplayClients()) {
                client.loadIcon();
                for (DisplayInformation info : client.getDisplayInformations()) {
                    displayValues.add(info);
                }
            }

            slotManager = new DataLayoutManager(getActivity());
            slotManager.load(DataLayoutManager.Mode.Edit, newPackageName);
            return slotManager;
        }).completed((slotManager, task) -> {
            LogUtil.log("display load completed :: %s", newPackageName);
            mDisplaySlotManager = slotManager;
            for (LayoutSlot slot : mDisplaySlotManager.listSlots()) {
                updateSlotPreview(mDisplaySlotManager, slot);
            }
        }).finalized(task -> {
            popProgress();
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
        MaterialButton button = new MaterialButton(getContext());
        if (slot.hasLink()) {
            // TODO 値のタイトルを入れる
            DisplayInformation information = mExtensionClientManager.findDisplayInformation(slot.getExtensionId(), slot.getDisplayValueId());
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
        List<ExtensionClient> displayClients = mExtensionClientManager.listDisplayClients();

        final BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.bottomsheet_root, null);

        // 非表示を加える
        {
            View view = inflater.inflate(R.layout.card_displayinfo_remove, null);
            view.setOnClickListener(it -> {
                onSelectedDisplay(manager, slot, null, null);
                dialog.dismiss();
            });
            layout.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        for (final ExtensionClient client : displayClients) {
            LogUtil.log("Display Extension name(%s)", client.getName());

            View extensionView = inflater.inflate(R.layout.card_displayinfo_root, null);

            AQuery q = new AQuery(extensionView);
            q.id(R.id.Extension_ItemSelector_ExtensionName).text(client.getName());
            q.id(R.id.Extension_ItemSelector_Icon).image(client.loadIcon());

            ViewGroup insertRoot = q.id(R.id.Extension_ItemSelector_Root).getView(ViewGroup.class);

            // Extensionごとの表示内容を並べる
            for (final DisplayInformation info : client.getDisplayInformations()) {
                View item = inflater.inflate(R.layout.card_displayinfo_item, null);
                ((TextView) item.findViewById(R.id.Extension_ItemSelector_Name)).setText(info.getTitle());
                insertRoot.addView(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                item.setOnClickListener(it -> {
                    onSelectedDisplay(manager, slot, client, info);
                    dialog.dismiss();
                });
            }

            layout.addView(extensionView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
    private void onSelectedDisplay(DataLayoutManager manager, LayoutSlot slot, ExtensionClient client, DisplayInformation displayInfo) {

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


    private String getUniqueId(ExtensionClient client, DisplayInformation display) {
        return String.format("%s|%s", client.getId(), display.getId());
    }
}
