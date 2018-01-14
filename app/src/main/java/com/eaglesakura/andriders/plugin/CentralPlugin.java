package com.eaglesakura.andriders.plugin;

import com.eaglesakura.andriders.AceSdkUtil;
import com.eaglesakura.andriders.central.CentralDataUtil;
import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.data.display.DisplayBindManager;
import com.eaglesakura.andriders.data.display.DisplayKeyCollection;
import com.eaglesakura.andriders.data.notification.CentralNotificationManager;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.gen.prop.DebugSettings;
import com.eaglesakura.andriders.gen.prop.UserProfiles;
import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.plugin.internal.CentralDataCommand;
import com.eaglesakura.andriders.plugin.internal.DisplayCommand;
import com.eaglesakura.andriders.plugin.internal.PluginServerImpl;
import com.eaglesakura.andriders.sdk.BuildConfig;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.andriders.serialize.PluginProtocol;
import com.eaglesakura.andriders.service.CentralSessionService;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.service.CommandClient;
import com.eaglesakura.android.service.CommandMap;
import com.eaglesakura.android.service.data.Payload;
import com.eaglesakura.cerberus.error.TaskCanceledException;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.CollectionUtil;
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
    private DisplayKeyCollection mDisplayKeyList;

    /**
     * コネクションごとの一意に識別するID
     */
    private final String mConnectionId;

    /**
     * 拡張機能のSDKバージョン
     */
    private String mSdkVersion = null;

    private CentralDataManager.Holder mCentralDataManagerHolder;

    private CentralNotificationManager.Holder mNotificationManagerHolder;

    private DisplayBindManager.Holder mDisplayBindManagerHolder;

    @NonNull
    private final UserProfiles mUserProfiles;

    @NonNull
    private final DebugSettings mDebugSettings;

    /**
     * Plugin本体との通信を行う
     */
    private CommandClientImpl mClientImpl;

    CentralPlugin(Context context, ResolveInfo info, UserProfiles profiles, DebugSettings debugSettings) {
        mConnectionId = newConnectionId();
        mContext = context;
        mPackageInfo = info;
        mUserProfiles = profiles;
        mDebugSettings = debugSettings;

        buildCentralCommands();
        buildDisplayCommands();
    }

    public void setCentralDataManager(CentralDataManager.Holder centralDataManagerHolder) {
        mCentralDataManagerHolder = centralDataManagerHolder;
    }

    public void setNotificationManager(CentralNotificationManager.Holder notificationManagerHolder) {
        mNotificationManagerHolder = notificationManagerHolder;
    }

    public void setDisplayBindManager(DisplayBindManager.Holder displayBindManagerHolder) {
        mDisplayBindManagerHolder = displayBindManagerHolder;
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
        intent.putExtra(PluginServerImpl.EXTRA_DEBUGGABLE, mDebugSettings.isDebugEnable());
        intent.putExtra(PluginServerImpl.EXTRA_ACE_COMPONENT, new ComponentName(mContext, CentralSessionService.class));

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
         * ACE本体との接続として扱う場合はtrue
         */
        public boolean centralConnection = false;
    }

    private Drawable mIcon;

    private String mName;

    /**
     * 表示用アイコンを取得する
     */
    public Drawable loadIcon() {
        if (mIcon == null) {
            mIcon = mPackageInfo.loadIcon(mContext.getPackageManager());
        }
        return mIcon;
    }

    public String getName() {
        if (mName == null) {
            mName = mPackageInfo.loadLabel(mContext.getPackageManager()).toString();
        }
        return mName;
    }

    /**
     * プラグイン識別IDを取得する
     */
    public String getId() {
        return getInformation().getId();
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
                AppLog.report(e);
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
        for (DisplayKey info : listDisplayKeys().getSource()) {
            if (info.getId().equals(id)) {
                return info;
            }
        }

        return null;
    }

    /**
     * サイコン表示内容を取得する
     */
    public synchronized DisplayKeyCollection listDisplayKeys() {
        if (mDisplayKeyList == null) {
            try {
                Payload payload = mClientImpl.requestPostToServer(CentralDataCommand.CMD_getDisplayInformations, null);
                mDisplayKeyList = new DisplayKeyCollection(DisplayKey.deserialize(payload.getBuffer()));
            } catch (Exception e) {
                AppLog.report(e);
                return new DisplayKeyCollection();
            }
        }

        return mDisplayKeyList;
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
     * ハンドリングに成功した場合はtrueを返却する。
     */
    public boolean startSettings() {
        try {
            mClientImpl.requestPostToServer(CentralDataCommand.CMD_onSettingStart, null);
            return true;
        } catch (Exception e) {
            AppLog.printStackTrace(e);
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
        /**
         * ディスプレイ情報を更新する
         */
        mCmdMap.addAction(DisplayCommand.CMD_setDisplayValue, (Object sender, String cmd, Payload payload) -> {
            List<DisplayData> listCommands = DisplayData.deserialize(payload.getBuffer(), DisplayData.class);
            // 表示内容を更新する
            CentralDataUtil.execute(mDisplayBindManagerHolder, it -> it.putValue(listCommands));
            return null;
        });
//
        /**
         * 通知を行う
         */
        mCmdMap.addAction(DisplayCommand.CMD_queueNotification, (Object sender, String cmd, Payload payload) -> {
            NotificationData notificationData = new NotificationData(mContext, payload.getBuffer());
            // 通知を送信する
            CentralDataUtil.execute(mNotificationManagerHolder, it -> it.queue(notificationData));
            return null;
        });
    }

    private void buildCentralCommands() {
        /**
         * 接続先のBLEアドレスを問い合わせる
         */
        mCmdMap.addAction(CentralDataCommand.CMD_setBleGadgetAddress, (sender, cmd, payload) -> {
            SensorType sensorType = SensorType.valueOf(Payload.deserializeStringOrNull(payload));

            String sensorAddress;
            if (sensorType == SensorType.HeartrateMonitor) {
                sensorAddress = mUserProfiles.getBleHeartrateMonitorAddress();
            } else if (sensorType == SensorType.CadenceSensor || sensorType == SensorType.SpeedSensor) {
                sensorAddress = mUserProfiles.getBleSpeedCadenceSensorAddress();
            } else {
                return null;
            }

            return Payload.fromString(sensorAddress);

        });

        /**
         * ホイールの外周サイズを問い合わせる
         */
        mCmdMap.addAction(CentralDataCommand.CMD_getWheelOuterLength, (sender, cmd, payload) -> {
            return Payload.fromString(String.valueOf(mUserProfiles.getWheelOuterLength()));

        });

        /**
         * GPS座標を更新する
         */
        mCmdMap.addAction(CentralDataCommand.CMD_setLocation, (Object sender, String cmd, Payload payload) -> {
            PluginProtocol.SrcLocation idl = AceSdkUtil.deserializeFromByteArray(PluginProtocol.SrcLocation.class, payload.getBuffer());
            CentralDataUtil.execute(mCentralDataManagerHolder, it -> it.setLocation(idl.latitude, idl.longitude, idl.altitude, idl.accuracyMeter));
            return null;
        });

        /**
         * 心拍を設定する
         */
        mCmdMap.addAction(CentralDataCommand.CMD_setHeartrate, (Object sender, String cmd, Payload payload) -> {
            PluginProtocol.SrcHeartrate idl = AceSdkUtil.deserializeFromByteArray(PluginProtocol.SrcHeartrate.class, payload.getBuffer());
            CentralDataUtil.execute(mCentralDataManagerHolder, it -> it.setHeartrate(idl.bpm));
            return null;
        });

        /**
         * S&Cセンサーを設定する
         */
        mCmdMap.addAction(CentralDataCommand.CMD_setSpeedAndCadence, (Object sender, String cmd, Payload payload) -> {
            PluginProtocol.SrcSpeedAndCadence idl = AceSdkUtil.deserializeFromByteArray(PluginProtocol.SrcSpeedAndCadence.class, payload.getBuffer());
            CentralDataUtil.execute(mCentralDataManagerHolder, it -> it.setSpeedAndCadence(idl.crankRpm, idl.crankRevolution, idl.wheelRpm, idl.wheelRevolution));
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
