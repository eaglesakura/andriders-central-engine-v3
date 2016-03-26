package com.eaglesakura.andriders.extension;

import com.eaglesakura.andriders.central.CentralDataManager;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.display.DataDisplayManager;
import com.eaglesakura.andriders.extension.display.DisplayData;
import com.eaglesakura.andriders.extension.internal.CentralDataCommand;
import com.eaglesakura.andriders.extension.internal.DisplayCommand;
import com.eaglesakura.andriders.extension.internal.ExtensionServerImpl;
import com.eaglesakura.andriders.internal.protocol.ExtensionProtocol;
import com.eaglesakura.andriders.sdk.BuildConfig;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.andriders.service.central.CentralService;
import com.eaglesakura.andriders.v2.db.UserProfiles;
import com.eaglesakura.android.service.CommandClient;
import com.eaglesakura.android.service.CommandMap;
import com.eaglesakura.android.service.data.Payload;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.LogUtil;

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

    /**
     * 拡張機能のアイコン
     */
    Drawable mIcon;

    final boolean mCentralServiceMode;

    /**
     * コネクションごとの一意に識別するID
     */
    final String mSessionId;

    /**
     * 拡張機能のSDKバージョン
     */
    private String mSdkVersion = null;

    /**
     * サイコン情報を設定するためのコールバック
     * デフォルトでは何もしない。
     */
    private Worker<CentralDataManager> mCycleComputerDataWorker = it -> {
    };

    /**
     * ディスプレイ情報を設定するためのコールバック
     *
     * デフォルトでは何もしない
     */
    private Worker<DataDisplayManager> mDataDisplayManagerWorker = it -> {
    };

    ExtensionClient(Context context, ExtensionClientManager parent, String sessionId) {
        super(context, String.format("%s", UUID.randomUUID().toString()));
        mCentralServiceMode = (context instanceof CentralService);
        mSessionId = sessionId;
        mParent = parent;
        buildCentralCommands();
        buildDisplayCommands();
    }

    public void setCentralWorker(Worker<CentralDataManager> cycleComputerDataWorker) {
        mCycleComputerDataWorker = cycleComputerDataWorker;
    }

    public void setDisplayWorker(Worker<DataDisplayManager> worker) {
        mDataDisplayManagerWorker = worker;
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

        UIHandler.postUI(() -> connectToSever(intent));
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

        if (CollectionUtil.isEmpty(mInformations)) {
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

    /**
     * クライアントのSDKバージョンを取得する
     */
    public String getSdkVersion() {
        if (mSdkVersion == null) {
            try {
                Payload payload = requestPostToServer(CentralDataCommand.CMD_getSDKVersion, null);
                mSdkVersion = Payload.deserializeStringOrNull(payload);
            } catch (Exception e) {
            }
        }

        return mSdkVersion;
    }

    @Override
    protected Payload onReceivedData(String cmd, Payload payload) throws RemoteException {
        return cmdMap.execute(this, cmd, payload);
    }

    private void buildDisplayCommands() {
        /**
         * ディスプレイ情報を更新する
         */
        cmdMap.addAction(DisplayCommand.CMD_setDisplayValue, (Object sender, String cmd, Payload payload) -> {
            List<DisplayData> list = DisplayData.deserialize(payload.getBuffer(), DisplayData.class);
            // 表示内容を更新する
            mDataDisplayManagerWorker.request(it -> {
                it.putValue(ExtensionClient.this, list);
            });
            return null;
        });
    }

    private void buildCentralCommands() {
        /**
         * 接続先のBLEアドレスを問い合わせる
         */
        cmdMap.addAction(CentralDataCommand.CMD_setBleGadgetAddress, (sender, cmd, payload) -> {
            SensorType sensorType = SensorType.valueOf(Payload.deserializeStringOrNull(payload));

            UserProfiles userProfiles = Settings.getInstance().getUserProfiles();
            String sensorAddress;
            if (sensorType == SensorType.HeartrateMonitor) {
                sensorAddress = userProfiles.getBleHeartrateMonitorAddress();
            } else if (sensorType == SensorType.CadenceSensor || sensorType == SensorType.SpeedSensor) {
                sensorAddress = userProfiles.getBleSpeedCadenceSensorAddress();
            } else {
                return null;
            }

            return Payload.fromString(sensorAddress);

        });

        /**
         * GPS座標を更新する
         */
        cmdMap.addAction(CentralDataCommand.CMD_setLocation, (Object sender, String cmd, Payload payload) -> {
            ExtensionProtocol.SrcLocation idl = payload.deserializePublicField(ExtensionProtocol.SrcLocation.class);
            mCycleComputerDataWorker.request(it -> it.setLocation(idl.latitude, idl.longitude, idl.altitude, idl.accuracyMeter));
            return null;
        });

        /**
         * 心拍を設定する
         */
        cmdMap.addAction(CentralDataCommand.CMD_setHeartrate, (Object sender, String cmd, Payload payload) -> {
            ExtensionProtocol.SrcHeartrate idl = payload.deserializePublicField(ExtensionProtocol.SrcHeartrate.class);
            mCycleComputerDataWorker.request(it -> it.setHeartrate(idl.bpm));
            return null;
        });

        /**
         * S&Cセンサーを設定する
         */
        cmdMap.addAction(CentralDataCommand.CMD_setSpeedAndCadence, (Object sender, String cmd, Payload payload) -> {
            ExtensionProtocol.SrcSpeedAndCadence idl = payload.deserializePublicField(ExtensionProtocol.SrcSpeedAndCadence.class);
            mCycleComputerDataWorker.request(it -> it.setSpeedAndCadence(idl.crankRpm, idl.crankRevolution, idl.wheelRpm, idl.wheelRevolution));
            return null;
        });
    }

    /**
     * 実際の処理を記述する
     */
    public interface Action<T> {
        void callback(T it);
    }

    /**
     * 処理を行わせるためのコールバックを登録する
     */
    public interface Worker<T> {
        void request(Action<T> action);
    }

}
