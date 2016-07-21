package com.eaglesakura.andriders.basicui;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.basicui.display.CadenceDisplaySender;
import com.eaglesakura.andriders.basicui.display.DisplayDataSender;
import com.eaglesakura.andriders.basicui.display.HeartrateDisplaySender;
import com.eaglesakura.andriders.basicui.display.SpeedDisplaySender;
import com.eaglesakura.andriders.display.ZoneColor;
import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.plugin.AcePluginService;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.CentralEngineConnection;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.plugin.display.BasicValue;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.thread.loop.HandlerLoopController;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BasicExtensionService extends Service implements AcePluginService {
    /**
     * 現在ケイデンス
     */
    static final String DEBUG_RANDOM_HEARTRATE = "debug.DEBUG_RANDOM_HEARTRATE";

    HandlerLoopController mDisplayCommitLoop;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AppLog.system("onBind(%s)", toString());
        CentralEngineConnection session = CentralEngineConnection.onBind(this, intent);
        if (session == null) {
            return null;
        }

        return session.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AppLog.system("onUnbind(%s)", toString());
        CentralEngineConnection.onUnbind(this, intent);
        return super.onUnbind(intent);
    }

    @Override
    public PluginInformation getExtensionInformation(CentralEngineConnection connection) {
        PluginInformation info = new PluginInformation(this, "basic_extension");
        info.setSummary("Andriders Central Engine 標準機能");
        info.setCategory(Category.CATEGORY_OTHERS);
        return info;
    }

    @Override
    public List<DisplayKey> getDisplayInformation(CentralEngineConnection connection) {
        List<DisplayKey> result = new ArrayList<>();

        result.add(HeartrateDisplaySender.newInformation(this));
        result.add(CadenceDisplaySender.newInformation(this));
        result.add(SpeedDisplaySender.newInformation(this));

        if (connection.isDebuggable()) {
            {
                DisplayKey info = new DisplayKey(this, DEBUG_RANDOM_HEARTRATE);
                info.setTitle("DBG:ダミー心拍");

                result.add(info);
            }
        }

        return result;
    }

    @Override
    public void onAceServiceConnected(final CentralEngineConnection connection) {
        if (mDisplayCommitLoop != null) {
            return;
        }

        ZoneColor zoneColor = new ZoneColor(this);
        List<DisplayDataSender> senders = new ArrayList<>();
        senders.add(new HeartrateDisplaySender(connection, zoneColor).bind());
        senders.add(new CadenceDisplaySender(connection, zoneColor).bind());
        senders.add(new SpeedDisplaySender(connection, zoneColor).bind());

        mDisplayCommitLoop = new HandlerLoopController(UIHandler.getInstance()) {
            @Override
            protected void onUpdate() {
                postDummyHeartrate(connection);
                double delta = mDisplayCommitLoop.getDeltaTime();
                for (DisplayDataSender sender : senders) {
                    sender.onUpdate(delta);
                }
            }
        };
        mDisplayCommitLoop.setFrameRate(1);
        mDisplayCommitLoop.connect();


        // 通知を送る
        connection.getDisplayExtension().queueNotification(
                new NotificationData.Builder(this, NotificationData.ID_CENTRAL_SERVICE_BOOT)
                        .icon(R.mipmap.ic_launcher)
                        .message("Andriders Central Engineを起動しました").getNotification()
        );
    }

    @Override
    public void onAceServiceDisconnected(CentralEngineConnection connection) {
        if (mDisplayCommitLoop == null) {
            return;
        }
        mDisplayCommitLoop.disconnect();
        mDisplayCommitLoop = null;
    }

    @Override
    public void onEnable(CentralEngineConnection connection) {

    }

    @Override
    public void onDisable(CentralEngineConnection connection) {

    }

    @Override
    public void startSetting(CentralEngineConnection connection) {

    }

    /**
     * ダミーの心拍データを書き込む
     * FIXME 将来的に、DisplayDataではなく心拍そのものをダミー書き込みするようにする
     */
    private void postDummyHeartrate(CentralEngineConnection session) {
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
