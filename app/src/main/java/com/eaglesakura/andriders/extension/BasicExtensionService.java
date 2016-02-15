package com.eaglesakura.andriders.extension;

import com.eaglesakura.andriders.extension.display.BasicValue;
import com.eaglesakura.andriders.extension.display.DisplayData;
import com.eaglesakura.android.framework.service.BaseService;
import com.eaglesakura.android.thread.loop.HandlerLoopController;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.util.LogUtil;

import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BasicExtensionService extends BaseService implements IExtensionService {

    /**
     * 心拍
     */
    static final String DISPLAY_ID_HEARTRATE = "DISPLAY_ID_HEARTRATE";

    /**
     * 現在速度
     */
    static final String DISPLAY_ID_CURRENT_SPEED = "DISPLAY_ID_CURRENT_SPEED";

    /**
     * 現在ケイデンス
     */
    static final String DISPLAY_ID_CURRENT_CADENCE = "DISPLAY_ID_CURRENT_CADENCE";


    /**
     * 現在ケイデンス
     */
    static final String DEBUG_RANDOM_HEARTRATE = "debug.DEBUG_RANDOM_HEARTRATE";

    HandlerLoopController mDisplayCommitLoop;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.log("onBind(%s)", toString());
        ExtensionSession session = ExtensionSession.onBind(this, intent);
        if (session == null) {
            return null;
        }

        return session.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.log("onUnbind(%s)", toString());
        ExtensionSession.onUnbind(this, intent);
        return super.onUnbind(intent);
    }


    @Override
    public ExtensionInformation getExtensionInformation(ExtensionSession session) {
        ExtensionInformation info = new ExtensionInformation(this, "basic_extension");
        info.setSummary("Andriders Central Engine 標準機能");
        info.setCategory(ExtensionCategory.CATEGORY_OTHERS);
        return info;
    }

    @Override
    public List<DisplayInformation> getDisplayInformation(ExtensionSession session) {
        List<DisplayInformation> result = new ArrayList<>();
        {
            DisplayInformation info = new DisplayInformation(this, DISPLAY_ID_HEARTRATE);
            info.setTitle("心拍");

            result.add(info);
        }
        {
            DisplayInformation info = new DisplayInformation(this, DISPLAY_ID_CURRENT_SPEED);
            info.setTitle("速度");

            result.add(info);
        }
        {
            DisplayInformation info = new DisplayInformation(this, DISPLAY_ID_CURRENT_CADENCE);
            info.setTitle("ケイデンス");

            result.add(info);
        }
        if (session.isDebugable()) {
            {
                DisplayInformation info = new DisplayInformation(this, DEBUG_RANDOM_HEARTRATE);
                info.setTitle("DBG:ダミー心拍");

                result.add(info);
            }
        }

        return result;
    }

    @Override
    public void onAceServiceConnected(final ExtensionSession session) {
        if (mDisplayCommitLoop != null) {
            return;
        }

        mDisplayCommitLoop = new HandlerLoopController(UIHandler.getInstance()) {
            @Override
            protected void onUpdate() {
                postDisplayData(session);
            }
        };
        mDisplayCommitLoop.setFrameRate(1);
        mDisplayCommitLoop.connect();
    }

    @Override
    public void onAceServiceDisconnected(ExtensionSession session) {
        if (mDisplayCommitLoop == null) {
            return;
        }
        mDisplayCommitLoop.disconnect();
        mDisplayCommitLoop = null;
    }

    @Override
    public void onEnable(ExtensionSession session) {

    }

    @Override
    public void onDisable(ExtensionSession session) {

    }

    @Override
    public void startSetting(ExtensionSession session) {

    }

    void postDisplayData(ExtensionSession session) {
        postDummyHeartrate(session);
    }

    /**
     * ダミーの心拍データを書き込む
     * FIXME 将来的に、DisplayDataではなく心拍そのものをダミー書き込みするようにする
     */
    private void postDummyHeartrate(ExtensionSession session) {
        DisplayData data = new DisplayData(this, DEBUG_RANDOM_HEARTRATE);
        BasicValue value = new BasicValue();
        value.setTitle("DBG: 心拍");
        value.setValue(String.format("%d", 90 + (int) (Math.random() * 10)));
        value.setBarColorARGB(Math.random() > 0.5 ? Color.RED : Color.TRANSPARENT);
        value.setZoneText("Zone" + (System.currentTimeMillis() % 10));
        data.setValue(value);

        session.getDisplayExtension().setValue(data);
    }
}
