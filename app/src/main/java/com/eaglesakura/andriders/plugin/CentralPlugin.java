package com.eaglesakura.andriders.plugin;

import com.eaglesakura.andriders.central.CentralDataHolder;
import com.eaglesakura.andriders.central.CentralDataUtil;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.gen.prop.UserProfiles;
import com.eaglesakura.andriders.plugin.internal.CentralDataCommand;
import com.eaglesakura.andriders.plugin.internal.PluginServerImpl;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.sdk.BuildConfig;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.andriders.serialize.PluginProtocol;
import com.eaglesakura.andriders.system.context.AppSettings;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.android.service.CommandClient;
import com.eaglesakura.android.service.CommandMap;
import com.eaglesakura.android.service.data.Payload;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.RandomUtil;
import com.eaglesakura.util.StringUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.Comparator;
import java.util.List;

/**
 * 外部プロセスとの連携を行うための入口となるClass
 */
public class CentralPlugin {

    private Context mContext;

    private ResolveInfo mPackageInfo;

    final CommandMap mCmdMap = new CommandMap();

    /**
     * 拡張内容キャッシュ
     */
    private List<PluginInformation> mInformationList;

    /**
     * ディスプレイキャッシュ
     */
    private List<DisplayKey> mDisplayInformationList;

    /**
     * コネクションごとの一意に識別するID
     */
    private final String mConnectionId;

    /**
     * 拡張機能のSDKバージョン
     */
    private String mSdkVersion = null;

    private CentralDataHolder mCentralDataHolder;

    @Inject(AppContextProvider.class)
    private AppSettings mSettings;

    /**
     * Plugin本体との通信を行う
     */
    private CommandClientImpl mClientImpl;

    CentralPlugin(Context context, ResolveInfo info) {
        mConnectionId = newConnectionId();
        mContext = context;
        mPackageInfo = info;

        Garnet.inject(this);
        buildCentralCommands();
        buildDisplayCommands();
    }

    public ComponentName getComponentName() {
        return new ComponentName(mPackageInfo.serviceInfo.packageName, mPackageInfo.serviceInfo.name);
    }

    /**
     * 接続済みである場合true
     */
    public boolean isConnected() {
        return mClientImpl != null;
    }

    /**
     * 拡張機能に接続する
     */
    public synchronized void connect(@NonNull ConnectOption option, CancelCallback cancelCallback) throws TaskCanceledException {
        if (mClientImpl != null) {
            throw new IllegalStateException("Client != null");
        }

        final Intent intent = new Intent(PluginServerImpl.ACTION_ACE_EXTENSION_BIND + "@" + mConnectionId);
        intent.setComponent(getComponentName());
        intent.putExtra(PluginServerImpl.EXTRA_CONNECTION_ID, mConnectionId);
        intent.putExtra(PluginServerImpl.EXTRA_ACE_IMPL_SDK_VERSION, BuildConfig.ACE_SDK_VERSION);
        intent.putExtra(PluginServerImpl.EXTRA_DEBUGGABLE, mSettings.isDebuggable());

        if (option.centralConnection) {
            // FIXME: CentralServiceのComponentNameを設定する
//            intent.putExtra(PluginServerImpl.EXTRA_ACE_COMPONENT, new ComponentName(mContext, CentralService.class));
        }

        CommandClientImpl commandClient = new CommandClientImpl(mContext, mConnectionId);
        commandClient.connect(intent, cancelCallback);

        mClientImpl = commandClient;
    }

    public synchronized void disconnect(CancelCallback cancelCallback) {
        CommandClientImpl clientImpl = mClientImpl;
        mClientImpl = null;

        clientImpl.disconnect(cancelCallback);
    }

    public static class ConnectOption {
        /**
         * ACE本体との接続を行う場合true
         */
        public boolean centralConnection = false;
    }

    /**
     * 表示用アイコンを取得する
     */
    public Drawable loadIcon() {
        return mPackageInfo.loadIcon(mContext.getPackageManager());
    }

    public String getName() {
        return mPackageInfo.loadLabel(mContext.getPackageManager()).toString();
    }

    /**
     * 拡張Serviceの情報を取得する
     */
    public synchronized PluginInformation getInformation() {
        if (mInformationList == null) {
            try {
                Payload payload = mClientImpl.requestPostToServer(CentralDataCommand.CMD_getInformations, null);
                mInformationList = PluginInformation.deserialize(payload.getBuffer());
            } catch (Exception e) {
                LogUtil.log(e);
                return null;
            }
        }

        if (CollectionUtil.isEmpty(mInformationList)) {
            return null;
        } else {
            return mInformationList.get(0);
        }
    }

    /**
     * プラグインカテゴリを取得する
     */
    public Category getCategory() {
        return getInformation().getCategory();
    }

    /**
     * 表示IDから情報を取得する
     */
    public DisplayKey findDisplayInformation(String id) {
        for (DisplayKey info : getDisplayInformationList()) {
            if (info.getId().equals(id)) {
                return info;
            }
        }

        return null;
    }

    /**
     * サイコン表示内容を取得する
     */
    public synchronized List<DisplayKey> getDisplayInformationList() {
        if (mDisplayInformationList == null) {
            try {
                Payload payload = mClientImpl.requestPostToServer(CentralDataCommand.CMD_getDisplayInformations, null);
                mDisplayInformationList = DisplayKey.deserialize(payload.getBuffer());
            } catch (Exception e) {
                AppLog.report(e);
                return null;
            }
        }

        return mDisplayInformationList;
    }

    /**
     * Centralの起動が完了した
     */
    public void onCentralBootCompleted() {
        try {
            mClientImpl.requestPostToServer(CentralDataCommand.CMD_onCentralBootCompleted, null);
        } catch (Exception e) {
            AppLog.report(e);
        }
    }

    /**
     * プロセスの強制再起動を行う
     *
     * MEMO: 明示的なProcess Killはステートの混乱を生むので、これを行わないように実装する。
     */
    @Deprecated
    public void requestReboot() {
        try {
            mClientImpl.requestPostToServer(CentralDataCommand.CMD_requestRebootPlugin, null);
        } catch (Exception e) {

        }
    }

    public ResolveInfo getPackageInfo() {
        return mPackageInfo;
    }

    /**
     * 拡張機能のON/OFFを切り替える
     */
    public void onActive(boolean use) throws AppException {
        try {
            if (use) {
                mClientImpl.requestPostToServer(CentralDataCommand.CMD_onExtensionEnable, null);
            } else {
                mClientImpl.requestPostToServer(CentralDataCommand.CMD_onExtensionDisable, null);
            }
        } catch (Throwable e) {
            AppException.throwAppException(e);
        }
    }

    /**
     * 設定画面を開かせる
     */
    public boolean startSettings() {
        try {
            mClientImpl.requestPostToServer(CentralDataCommand.CMD_onSettingStart, null);
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
                Payload payload = mClientImpl.requestPostToServer(CentralDataCommand.CMD_getSDKVersion, null);
                mSdkVersion = Payload.deserializeStringOrNull(payload);
            } catch (Exception e) {
                AppLog.report(e);
            }
        }

        return mSdkVersion;
    }

    private void buildDisplayCommands() {
//        /**
//         * ディスプレイ情報を更新する
//         */
//        mCmdMap.addAction(DisplayCommand.CMD_setDisplayValue, (Object sender, String cmd, Payload payload) -> {
//            List<DisplayData> list = DisplayData.deserialize(payload.getBuffer(), DisplayData.class);
//            // 表示内容を更新する
//            mDataDisplayManagerWorker.request(it -> it.putValue(PluginConnector.this, list));
//            return null;
//        });
//
//        /**
//         * 通知を行う
//         */
//        mCmdMap.addAction(DisplayCommand.CMD_queueNotification, (Object sender, String cmd, Payload payload) -> {
//
//            NotificationData notificationData = new NotificationData(mContext, payload.getBuffer());
//            // 通知を送信する
//            mNotificationDisplayManagerWorker.request(it -> it.queue(notificationData));
//
//            return null;
//        });
    }

    private void buildCentralCommands() {
        /**
         * 接続先のBLEアドレスを問い合わせる
         */
        mCmdMap.addAction(CentralDataCommand.CMD_setBleGadgetAddress, (sender, cmd, payload) -> {
            SensorType sensorType = SensorType.valueOf(Payload.deserializeStringOrNull(payload));

            UserProfiles userProfiles = mSettings.getUserProfiles();
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
         * ホイールの外周サイズを問い合わせる
         */
        mCmdMap.addAction(CentralDataCommand.CMD_getWheelOuterLength, (sender, cmd, payload) -> {
            UserProfiles userProfiles = mSettings.getUserProfiles();
            return Payload.fromString(String.valueOf(userProfiles.getWheelOuterLength()));

        });

        /**
         * GPS座標を更新する
         */
        mCmdMap.addAction(CentralDataCommand.CMD_setLocation, (Object sender, String cmd, Payload payload) -> {
            PluginProtocol.SrcLocation idl = payload.deserializePublicField(PluginProtocol.SrcLocation.class);
            CentralDataUtil.execute(mCentralDataHolder, it -> it.setLocation(idl.latitude, idl.longitude, idl.altitude, idl.accuracyMeter));
            return null;
        });

        /**
         * 心拍を設定する
         */
        mCmdMap.addAction(CentralDataCommand.CMD_setHeartrate, (Object sender, String cmd, Payload payload) -> {
            PluginProtocol.SrcHeartrate idl = payload.deserializePublicField(PluginProtocol.SrcHeartrate.class);
            CentralDataUtil.execute(mCentralDataHolder, it -> it.setHeartrate(idl.bpm));
            return null;
        });

        /**
         * S&Cセンサーを設定する
         */
        mCmdMap.addAction(CentralDataCommand.CMD_setSpeedAndCadence, (Object sender, String cmd, Payload payload) -> {
            PluginProtocol.SrcSpeedAndCadence idl = payload.deserializePublicField(PluginProtocol.SrcSpeedAndCadence.class);
            CentralDataUtil.execute(mCentralDataHolder, it -> it.setSpeedAndCadence(idl.crankRpm, idl.crankRevolution, idl.wheelRpm, idl.wheelRevolution));
            return null;
        });
    }

    class CommandClientImpl extends CommandClient {
        public CommandClientImpl(Context context, String uid) {
            super(context, uid);
        }


        @Override
        protected Payload onReceivedData(String cmd, Payload payload) throws RemoteException {
            return mCmdMap.execute(this, cmd, payload);
        }

        @WorkerThread
        void connect(Intent intent, CancelCallback cancelCallback) throws TaskCanceledException {
            if (!connectToSever(intent, cancelCallback)) {
                // キャンセルされているなら、失敗
                throw new TaskCanceledException();
            }
        }

        @Override
        protected void onDisconnected(int flags) {
            super.onDisconnected(flags);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CentralPlugin plugin = (CentralPlugin) o;

        return getComponentName().equals(plugin.getComponentName());

    }

    @Override
    public int hashCode() {
        return getComponentName().hashCode();
    }

    /**
     * コネクションを一意に識別するためのIDを生成する
     */
    public static String newConnectionId() {
        return RandomUtil.randShortString();
    }

    /**
     * ソート順を確定する
     */
    public static final Comparator<CentralPlugin> COMPARATOR_ASC = (a, b) -> StringUtil.compareString(a.getComponentName().toString(), b.getComponentName().toString());
}
