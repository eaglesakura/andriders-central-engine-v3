package com.eaglesakura.andriders.computer.extension.client;

import com.eaglesakura.andriders.central.CentralDataManager;
import com.eaglesakura.andriders.computer.display.DisplayManager;
import com.eaglesakura.andriders.computer.display.DisplayViewData;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.extension.DisplayInformation;
import com.eaglesakura.andriders.extension.ExtensionInformation;
import com.eaglesakura.andriders.extension.internal.CentralDataCommand;
import com.eaglesakura.andriders.extension.internal.DisplayCommand;
import com.eaglesakura.andriders.extension.internal.ExtensionServerImpl;
import com.eaglesakura.andriders.internal.protocol.ExtensionProtocol;
import com.eaglesakura.andriders.sdk.BuildConfig;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.andriders.service.central.CentralService;
import com.eaglesakura.android.service.CommandClient;
import com.eaglesakura.android.service.CommandMap;
import com.eaglesakura.android.service.data.Payload;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;

import java.util.List;
import java.util.UUID;

public class ExtensionClient extends CommandClient {
    ComponentName name;

    ResolveInfo packageInfo;

    final CommandMap cmdMap = new CommandMap();

    ExtensionClientManager mParent;

    /**
     * 拡張内容キャッシュ
     */
    List<ExtensionInformation> mInformations;

    /**
     * ディスプレイキャッシュ
     */
    List<DisplayInformation> mDisplayInformations;

    Drawable mIcon;

    final boolean mCentralServiceMode;

    final String mSessionId;

    ExtensionClient(Context context, ExtensionClientManager parent, String sessionId) {
        super(context, String.format("%s", UUID.randomUUID().toString()));
        mCentralServiceMode = (context instanceof CentralService);
        mSessionId = sessionId;
        mParent = parent;
        buildCentralCommands();
        buildDisplayCommands();
    }

    public CentralDataManager getCentralDataManager() {
        return mParent.mCentralDataManager;
    }

    public DisplayManager getDisplayManager() {
        return mParent.mDisplayManager;
    }

    /**
     * 拡張機能に接続する
     */
    public void connect(ResolveInfo info) {
        this.packageInfo = info;
        name = new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);

        final Intent intent = new Intent(ExtensionServerImpl.ACTION_ACE_EXTENSION_BIND + "@" + mSessionId);
        intent.setComponent(name);
        intent.putExtra(ExtensionServerImpl.EXTRA_SESSION_ID, mSessionId);
        intent.putExtra(ExtensionServerImpl.EXTRA_ACE_IMPL_SDK_VERSION, BuildConfig.ACE_SDK_VERSION);
        intent.putExtra(ExtensionServerImpl.EXTRA_DEBUGABLE, Settings.isDebugable());

        if (mCentralServiceMode) {
            intent.putExtra(ExtensionServerImpl.EXTRA_ACE_COMPONENT, new ComponentName(mContext, CentralService.class));
        }

        UIHandler.postUI(new Runnable() {
            @Override
            public void run() {
                connectToSever(intent);
            }
        });
    }

    /**
     * 表示用アイコンを取得する
     */
    public Drawable loadIcon() {
        if (mIcon == null) {
            mIcon = packageInfo.loadIcon(mContext.getPackageManager());
        }
        return mIcon;
    }

    public String getName() {
        return packageInfo.loadLabel(mContext.getPackageManager()).toString();
    }

    @Override
    protected void onConnected() {
        super.onConnected();
    }

    @Override
    protected void onDisconnected() {
        super.onDisconnected();
    }

    /**
     * 拡張Serviceの情報を取得する
     */
    public synchronized ExtensionInformation getInformation() {
        if (mInformations == null) {
            try {
                Payload payload = requestPostToServer(CentralDataCommand.CMD_getInformations, null);
                mInformations = ExtensionInformation.deserialize(payload.getBuffer());
            } catch (Exception e) {
                LogUtil.log(e);
                return null;
            }
        }

        if (Util.isEmpty(mInformations)) {
            return null;
        } else {
            return mInformations.get(0);
        }
    }

    /**
     * 表示IDから情報を取得する
     */
    public DisplayInformation findDisplayInformation(String id) {
        for (DisplayInformation info : getDisplayInformations()) {
            if (info.getId().equals(id)) {
                return info;
            }
        }

        return null;
    }

    /**
     * サイコン表示内容を取得する
     */
    public synchronized List<DisplayInformation> getDisplayInformations() {
        if (mDisplayInformations == null) {
            try {
                Payload payload = requestPostToServer(CentralDataCommand.CMD_getDisplayInformations, null);
                mDisplayInformations = DisplayInformation.deserialize(payload.getBuffer());
            } catch (Exception e) {
                LogUtil.log(e);
                return null;
            }
        }

        return mDisplayInformations;
    }

    /**
     * 再起動を行う
     */
    public void requestReboot() {
        try {
            requestPostToServer(CentralDataCommand.CMD_requestRebootExtention, null);
        } catch (Exception e) {

        }
    }

    /**
     * 拡張機能のON/OFFを切り替える
     */
    public void setEnable(boolean use) {
        try {
            if (use) {
                requestPostToServer(CentralDataCommand.CMD_onExtensionEnable, null);
            } else {
                requestPostToServer(CentralDataCommand.CMD_onExtensionDisable, null);
            }
        } catch (Exception e) {

        }
    }

    /**
     * 設定画面を開かせる
     */
    public boolean startSettings() {
        try {
            requestPostToServer(CentralDataCommand.CMD_onSettingStart, null);
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
            return false;
        }
    }

    private String sdkVersion = null;

    /**
     * クライアントのSDKバージョンを取得する
     */
    public String getSdkVersion() {
        if (sdkVersion == null) {
            try {
                Payload payload = requestPostToServer(CentralDataCommand.CMD_getSDKVersion, null);
                sdkVersion = Payload.deserializeStringOrNull(payload);
            } catch (Exception e) {
            }
        }

        return sdkVersion;
    }

    @Override
    protected Payload onReceivedData(String cmd, Payload payload) throws RemoteException {
        return cmdMap.execute(this, cmd, payload);
    }

    private void buildDisplayCommands() {
        final DisplayManager displayManager = getDisplayManager();
        if (displayManager == null) {
            return;
        }

        /**
         * ディスプレイ情報を更新する
         */
        cmdMap.addAction(DisplayCommand.CMD_setDisplayValue, new CommandMap.Action() {
            @Override
            public Payload execute(Object sender, String cmd, Payload payload) throws Exception {
                List<DisplayViewData> list = DisplayViewData.deserialize(payload.getBuffer(), DisplayViewData.class);
                displayManager.putValue(ExtensionClient.this, list);
                return null;
            }
        });
    }

    private void buildCentralCommands() {
        final CentralDataManager dataManager = getCentralDataManager();

        if (dataManager == null) {
            return;
        }

        /**
         * 接続先のBLEアドレスを問い合わせる
         */
        cmdMap.addAction(CentralDataCommand.CMD_setBleGadgetAddress, new CommandMap.Action() {
            @Override
            public Payload execute(Object sender, String cmd, Payload payload) throws Exception {
                SensorType sensorType = SensorType.valueOf(Payload.deserializeStringOrNull(payload));

                String sensorAddress;
                if (sensorType == SensorType.HeartrateMonitor) {
                    sensorAddress = Settings.getInstance().getUserProfiles().getBleHeartrateMonitorAddress();
                } else if (sensorType == SensorType.CadenceSensor || sensorType == SensorType.SpeedSensor) {
                    sensorAddress = Settings.getInstance().getUserProfiles().getBleSpeedCadenceSensorAddress();
                } else {
                    return null;
                }

                return Payload.fromString(sensorAddress);
            }
        });

        /**
         * GPS座標を更新する
         */
        cmdMap.addAction(CentralDataCommand.CMD_setLocation, new CommandMap.Action() {
            @Override
            public Payload execute(Object sender, String cmd, Payload payload) throws Exception {
                ExtensionProtocol.SrcLocation idl = payload.deserializePublicField(ExtensionProtocol.SrcLocation.class);
                dataManager.setLocation(idl);
                return null;
            }
        });

        /**
         * 心拍を設定する
         */
        cmdMap.addAction(CentralDataCommand.CMD_setHeartrate, new CommandMap.Action() {
            @Override
            public Payload execute(Object sender, String cmd, Payload payload) throws Exception {
                ExtensionProtocol.SrcHeartrate idl = payload.deserializePublicField(ExtensionProtocol.SrcHeartrate.class);
                dataManager.setHeartrate(idl);
                return null;
            }
        });

        /**
         * S&Cセンサーを設定する
         */
        cmdMap.addAction(CentralDataCommand.CMD_setSpeedAndCadence, new CommandMap.Action() {
            @Override
            public Payload execute(Object sender, String cmd, Payload payload) throws Exception {
                ExtensionProtocol.SrcSpeedAndCadence idl = payload.deserializePublicField(ExtensionProtocol.SrcSpeedAndCadence.class);
                dataManager.setSpeedAndCadence(idl);
                return null;
            }
        });
    }
}
