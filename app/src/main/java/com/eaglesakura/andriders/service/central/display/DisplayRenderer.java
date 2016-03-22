package com.eaglesakura.andriders.service.central.display;

import com.eaglesakura.andriders.computer.display.DisplayViewManager;
import com.eaglesakura.andriders.computer.extension.client.ExtensionClient;
import com.eaglesakura.andriders.computer.extension.client.ExtensionClientManager;
import com.eaglesakura.andriders.display.LayoutSlot;
import com.eaglesakura.andriders.display.DisplayLayoutManager;
import com.eaglesakura.andriders.extension.DisplayInformation;
import com.eaglesakura.andriders.service.central.CentralContext;
import com.eaglesakura.android.framework.service.BaseService;
import com.eaglesakura.android.thread.loop.HandlerLoopController;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.view.View;
import android.view.ViewGroup;

/**
 * Serviceで表示するサイコン情報を管理するマネージャ
 */
public class DisplayRenderer {
    final BaseService mService;

    /**
     * ディスプレイの表示更新間隔
     */
    final int DISPLAY_REFRESH_SEC = 1;

    final CentralContext mCentralContext;

    HandlerLoopController mLoopController;

    /**
     * ディスプレイのスロット管理
     */
    DisplayLayoutManager mDisplaySlotManager;

    /**
     * 表示対象のStub格納先
     */
    ViewGroup mDisplayStub;

    public DisplayRenderer(BaseService service, CentralContext centralContext) {
        mService = service;
        mCentralContext = centralContext;
    }

    /**
     * 表示対象のViewを作成する
     */
    public void setDisplayStub(ViewGroup displayStub) {
        if (displayStub.getChildCount() != 0) {
            throw new IllegalArgumentException();
        }
        mDisplayStub = displayStub;
    }


    /**
     * スロット情報のリロードを行う
     */
    public void reloadSlots(final String appPackageName) {
        if (mDisplaySlotManager != null) {
            if (mDisplaySlotManager.getAppPackageName().equals(appPackageName)) {
                // 同じアプリ名なので何もしなくて良い
                return;
            }
        }

        throw new IllegalAccessError("not impl");
//        FrameworkCentral.getTaskController().pushBack(new IAsyncTask<DisplaySlotManager>() {
//            @Override
//            public DisplaySlotManager doInBackground(AsyncTaskResult<DisplaySlotManager> result) throws Exception {
//                DisplaySlotManager slotManager = new DisplaySlotManager(mService, appPackageName, DisplaySlotManager.Mode.ReadOnly);
//                slotManager.load();
//                return slotManager;
//            }
//        }).setListener(new AsyncTaskResult.CompletedListener<DisplaySlotManager>() {
//            @Override
//            public void onTaskCompleted(AsyncTaskResult<DisplaySlotManager> task, DisplaySlotManager result) {
//                if (mDisplaySlotManager != null &&
//                        mDisplaySlotManager.getAppPackageName().equals(result.getAppPackageName())) {
//                    LogUtil.log("no change package(%s)", result.getAppPackageName());
//                    return;
//                }
//
//                mDisplaySlotManager = result;
//            }
//        });
    }

    /**
     * 拡張機能のディスプレイ機能とUIを結びつける
     */
    private void bindExtensionDisplay() {
        if (mDisplayStub == null) {
            // 表示対象が無い
            return;
        }

        if (mDisplayStub.getChildCount() == 0) {
            ViewGroup root = DisplayLayoutManager.newStubLayout(mService);
            mDisplayStub.addView(root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        ExtensionClientManager extensionClientManager = mCentralContext.getExtensionClientManager();
        DisplayViewManager displayManager = mCentralContext.getDisplayManager();

        for (LayoutSlot slot : mDisplaySlotManager.listSlots()) {
            ViewGroup viewSlot = (ViewGroup) mDisplayStub.findViewById(slot.getId());
            DisplayInformation information = null;
            if (slot.hasLink()) {
                // 値にリンクされている場合、インフォメーションを取得する
                information = extensionClientManager.findDisplayInformation(slot.getExtensionId(), slot.getDisplayValueId());
            }

            if (information == null) {
                // 表示内容が無いので何もしない
                viewSlot.setVisibility(View.INVISIBLE);
                continue;
            } else {
                ExtensionClient client = extensionClientManager.findClient(slot.getExtensionId());
                // ディスプレイに対して表示内容を更新させる
                displayManager.bindUI(client, information, viewSlot);
            }
        }
    }

    /**
     * 処理を開始する
     */
    public void connect() {
        mLoopController = new HandlerLoopController(UIHandler.getInstance()) {
            @Override
            protected void onUpdate() {
                onDisplayRefresh();
            }
        };
        mLoopController.setFrameRate(DISPLAY_REFRESH_SEC);
        mLoopController.connect();

        reloadSlots(mService.getPackageName());
    }

    /**
     * メモリを開放する
     */
    public void dispose() {
        mLoopController.disconnect();
        mLoopController.dispose();
        mLoopController = null;

        // 子のViewを全て開放する
        if (mDisplayStub != null) {
            mDisplayStub.removeAllViews();
            mDisplayStub = null;
        }
    }

    /**
     * 通知のレンダリングを行う
     */
    private void onDisplayRefresh() {
        ExtensionClientManager extensionClientManager = mCentralContext.getExtensionClientManager();

        if (mDisplaySlotManager == null || !extensionClientManager.isConnected()) {
            return;
        }
        // 毎フレーム更新をかける
        bindExtensionDisplay();
    }
}
