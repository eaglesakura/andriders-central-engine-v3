package com.eaglesakura.andriders.ui.navigation.display;

import com.cocosw.bottomsheet.BottomSheet;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.computer.extension.client.ExtensionClient;
import com.eaglesakura.andriders.computer.extension.client.ExtensionClientManager;
import com.eaglesakura.andriders.display.DisplaySlot;
import com.eaglesakura.andriders.display.DisplaySlotManager;
import com.eaglesakura.andriders.extension.DisplayInformation;
import com.eaglesakura.andriders.ui.base.AppBaseFragment;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.thread.async.IAsyncTask;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.material.widget.MaterialButton;
import com.eaglesakura.util.LogUtil;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * サイコンの表示内容を並べるFragment
 */
public class DisplayLayoutSetFragment extends AppBaseFragment {

    DisplaySlotManager mDisplaySlotManager;

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
        View view = DisplaySlotManager.newStubLayout(inflater.getContext());
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
        mExtensionClientManager.disconnect();
        mExtensionClientManager = null;
    }

    /**
     * 拡張機能を読み込む
     */
    private void loadExtensionClients() {
        pushProgress(R.string.Common_File_Load);
        mExtensionClientManager = new ExtensionClientManager(getContext());
        displayValues.clear();
        mExtensionClientManager.connect(ExtensionClientManager.ConnectMode.Enabled).setListener(new AsyncTaskResult.Listener<ExtensionClientManager>() {
            @Override
            public void onTaskCompleted(AsyncTaskResult<ExtensionClientManager> task, ExtensionClientManager result) {
                loadDisplayDatas(appPackageName);
            }

            @Override
            public void onTaskCanceled(AsyncTaskResult<ExtensionClientManager> task) {

            }

            @Override
            public void onTaskFailed(AsyncTaskResult<ExtensionClientManager> task, Exception error) {

            }

            @Override
            public void onTaskFinalize(AsyncTaskResult<ExtensionClientManager> task) {
                popProgress();
            }
        });
    }

    /**
     * ディスプレイ表示内容を読み込む
     */
    private void loadDisplayDatas(final String newPackageName) {
        runBackgroundTask(new IAsyncTask<DisplaySlotManager>() {
            @Override
            public DisplaySlotManager doInBackground(AsyncTaskResult<DisplaySlotManager> result) throws Exception {
                DisplaySlotManager slotManager = null;
                try {
                    pushProgress(R.string.Common_File_Load);

                    // 拡張機能のアイコンを読み込む
                    displayValues.clear();
                    for (ExtensionClient client : mExtensionClientManager.listDisplayClients()) {
                        client.loadIcon();
                        for (DisplayInformation info : client.getDisplayInformations()) {
                            displayValues.add(info);
                        }
                    }

                    slotManager = new DisplaySlotManager(getActivity(), newPackageName, DisplaySlotManager.Mode.Edit);
                    slotManager.load();
                } finally {
                    popProgress();
                }
                return slotManager;
            }
        }).setListener(new AsyncTaskResult.CompletedListener<DisplaySlotManager>() {
            @Override
            public void onTaskCompleted(AsyncTaskResult<DisplaySlotManager> task, DisplaySlotManager result) {
                initializeLayouts(result);
            }
        });
    }

    private void initializeLayouts(DisplaySlotManager slotManager) {
        AndroidThreadUtil.assertUIThread();

        LogUtil.log("display load completed :: %s", slotManager.getAppPackageName());
        mDisplaySlotManager = slotManager;
        for (DisplaySlot slot : mDisplaySlotManager.listSlots()) {
            updateSlotPreview(mDisplaySlotManager, slot);
        }
    }

    /**
     * ディスプレイスロットを更新する
     */
    void updateSlotPreview(final DisplaySlotManager slotManager, final DisplaySlot slot) {
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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDisplaySelector(slotManager, slot);
            }
        });

        stub.addView(button, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    /**
     * 表示内容の選択ダイアログをブート
     */
    private void showDisplaySelector(final DisplaySlotManager manager, final DisplaySlot slot) {
        List<ExtensionClient> displayClients = mExtensionClientManager.listDisplayClients();

        BottomSheet.Builder builder = new BottomSheet.Builder(getActivity());
        int index = 0;

        // 最初は非表示
        builder.sheet(-1, "非表示");

        for (ExtensionClient client : displayClients) {
            LogUtil.log("Display Extension name(%s)", client.getName());

            builder.divider();
            for (DisplayInformation info : client.getDisplayInformations()) {
                builder.sheet(index, client.loadIcon(), info.getTitle());
                ++index;
            }
        }

        builder.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which < 0) {
                    // 非表示にする
                    manager.removeLayout(slot);
                } else {
                    // スロットの値を上書き
                    DisplayInformation information = displayValues.get(which);
                    ExtensionClient client = mExtensionClientManager.findDisplayClient(information);
                    manager.setLayout(slot, client.getInformation(), information);
                }

                // 内容を保存
                manager.commitAsync();

                // 再表示
                updateSlotPreview(manager, slot);
            }
        });
        builder.show();
    }


    private String getUniqueId(ExtensionClient client, DisplayInformation display) {
        return String.format("%s|%s", client.getId(), display.getId());
    }
}
