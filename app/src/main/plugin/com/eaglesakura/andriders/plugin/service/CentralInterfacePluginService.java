package com.eaglesakura.andriders.plugin.service;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.display.ZoneColor;
import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.plugin.AcePluginService;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;
import com.eaglesakura.andriders.plugin.service.ui.CadenceDisplaySender;
import com.eaglesakura.andriders.plugin.service.ui.DisplayDataSender;
import com.eaglesakura.andriders.plugin.service.ui.HeartrateDisplaySender;
import com.eaglesakura.andriders.plugin.service.ui.SpeedDisplaySender;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.thread.loop.HandlerLoopController;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CentralInterfacePluginService extends Service implements AcePluginService {
    /**
     * 現在ケイデンス
     */
    static final String DEBUG_RANDOM_HEARTRATE = "debug.DEBUG_RANDOM_HEARTRATE";

    HandlerLoopController mDisplayCommitLoop;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AppLog.system("onBind(%s)", toString());
        PluginConnection session = PluginConnection.onBind(this, intent);
        if (session == null) {
            return null;
        }

        return session.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AppLog.system("onUnbind(%s)", toString());
        PluginConnection.onUnbind(this, intent);
        return super.onUnbind(intent);
    }

    @Override
    public PluginInformation getExtensionInformation(PluginConnection connection) {
        PluginInformation info = new PluginInformation(this, "basic_extension");
        info.setSummary("Andriders Central Engine 標準機能");
        info.setCategory(Category.CATEGORY_OTHERS);
        return info;
    }

    @Override
    public List<DisplayKey> getDisplayInformation(PluginConnection connection) {
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
    public void onAceServiceConnected(final PluginConnection connection) {
        if (mDisplayCommitLoop != null) {
            return;
        }

        AppLog.system("Wheel Length[%.1f]", connection.getCentralData().getWheelOuterLength());

        ZoneColor zoneColor = new ZoneColor(this);
        List<DisplayDataSender> senders = new ArrayList<>();
        senders.add(new HeartrateDisplaySender(connection, zoneColor).bind());
        senders.add(new CadenceDisplaySender(connection, zoneColor).bind());
        senders.add(new SpeedDisplaySender(connection, zoneColor).bind());

        mDisplayCommitLoop = new HandlerLoopController(UIHandler.getInstance()) {
            @Override
            protected void onUpdate() {
                double delta = mDisplayCommitLoop.getDeltaTime();
                for (DisplayDataSender sender : senders) {
                    sender.onUpdate(delta);
                }
            }
        };
        mDisplayCommitLoop.setFrameRate(1);
        mDisplayCommitLoop.connect();


        // 通知を送る
        connection.getDisplay().queueNotification(
                new NotificationData.Builder(this, NotificationData.ID_CENTRAL_SERVICE_BOOT)
                        .icon(R.mipmap.ic_launcher)
                        .message("A.C.E. セッション開始").getNotification()
        );
    }

    @Override
    public void onAceServiceDisconnected(PluginConnection connection) {
        if (mDisplayCommitLoop == null) {
            return;
        }
        mDisplayCommitLoop.disconnect();
        mDisplayCommitLoop = null;
    }

    @Override
    public void onEnable(PluginConnection connection) {

    }

    @Override
    public void onDisable(PluginConnection connection) {

    }

    @Override
    public void startSetting(PluginConnection connection) {

    }
}
