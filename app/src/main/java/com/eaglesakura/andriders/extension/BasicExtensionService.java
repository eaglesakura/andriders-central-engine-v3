package com.eaglesakura.andriders.extension;

import com.eaglesakura.andriders.extension.data.CentralDataExtension;
import com.eaglesakura.android.framework.service.BaseService;
import com.eaglesakura.util.LogUtil;

import android.content.Intent;
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
    public ExtensionInformation getExtensionInformation() {
        ExtensionInformation info = new ExtensionInformation(this, "basic_extension");
        info.setText("Andriders Central Engine 標準機能");
        info.setCategory(ExtensionCategory.CATEGORY_OTHERS);
        return info;
    }

    @Override
    public List<DisplayInformation> getDisplayInformation() {
        List<DisplayInformation> result = new ArrayList<>();
        {
            DisplayInformation info = new DisplayInformation(this, DISPLAY_ID_HEARTRATE);
            info.setTitle("心拍");
            info.setText("現在の心拍を表示します。");

            result.add(info);
        }
        {
            DisplayInformation info = new DisplayInformation(this, DISPLAY_ID_CURRENT_SPEED);
            info.setTitle("速度");
            info.setText("現在の速度を表示します");

            result.add(info);
        }
        {
            DisplayInformation info = new DisplayInformation(this, DISPLAY_ID_CURRENT_CADENCE);
            info.setTitle("ケイデンス");
            info.setText("ケイデンスを表示します。");

            result.add(info);
        }
        return result;
    }

    @Override
    public void onAceServiceConnected(ExtensionSession session) {

    }

    @Override
    public void onAceServiceDisconnected(ExtensionSession session) {

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
}
