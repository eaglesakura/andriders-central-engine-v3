package com.eaglesakura.andriders.basicui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.basicui.display.HeartrateDisplayUpdater;
import com.eaglesakura.andriders.central.SensorDataReceiver;
import com.eaglesakura.andriders.display.ZoneColor;
import com.eaglesakura.andriders.extension.DisplayInformation;
import com.eaglesakura.andriders.extension.ExtensionCategory;
import com.eaglesakura.andriders.extension.ExtensionInformation;
import com.eaglesakura.andriders.extension.ExtensionSession;
import com.eaglesakura.andriders.extension.IExtensionService;
import com.eaglesakura.andriders.extension.display.BasicValue;
import com.eaglesakura.andriders.extension.display.DisplayData;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.android.margarine.BindString;
import com.eaglesakura.android.margarine.BindStringArray;
import com.eaglesakura.android.thread.loop.HandlerLoopController;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.util.LogUtil;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.util.ArrayList;
import java.util.List;

public class BasicExtensionService extends Service implements IExtensionService {

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

    @BindStringArray(R.array.Display_Heartrate_ZoneName)
    String[] mHeartrateZoneNames;

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
        result.add(HeartrateDisplayUpdater.newInformation(this));
        {
            DisplayInformation info = new DisplayInformation(this, DISPLAY_ID_CURRENT_SPEED);
            info.setTitle(getString(R.string.Display_Common_Speed));

            result.add(info);
        }
        {
            DisplayInformation info = new DisplayInformation(this, DISPLAY_ID_CURRENT_CADENCE);
            info.setTitle(getString(R.string.Display_Common_Cadence));

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

        ZoneColor zoneColor = new ZoneColor(this);
        new HeartrateDisplayUpdater(session, zoneColor).bind();

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

    @UiThread
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
