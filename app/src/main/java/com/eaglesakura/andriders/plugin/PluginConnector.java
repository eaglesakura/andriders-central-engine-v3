package com.eaglesakura.andriders.plugin;

import com.eaglesakura.andriders.central.CentralDataManager;
import com.eaglesakura.andriders.db.AppSettings;
import com.eaglesakura.andriders.db.plugin.PluginDatabase;
import com.eaglesakura.andriders.display.data.DataDisplayManager;
import com.eaglesakura.andriders.display.notification.NotificationDisplayManager;
import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.plugin.internal.CentralDataCommand;
import com.eaglesakura.andriders.plugin.internal.DisplayCommand;
import com.eaglesakura.andriders.plugin.internal.PluginServerImpl;
import com.eaglesakura.andriders.provider.StorageProvider;
import com.eaglesakura.andriders.serialize.PluginProtocol;
import com.eaglesakura.andriders.sdk.BuildConfig;
import com.eaglesakura.andriders.sensor.SensorType;
import com.eaglesakura.andriders.service.central.CentralContext;
import com.eaglesakura.andriders.service.central.CentralService;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.v2.db.UserProfiles;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
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
import android.support.annotation.NonNull;

import java.util.List;
import java.util.UUID;

/**
 * 外部プロセスとの連携を行うための入口となるClass
 */
public class PluginConnector extends CommandClient {
    ComponentName mName;

    ResolveInfo mPackageInfo;

    final CommandMap mCmdMap = new CommandMap();

    PluginManager mParent;

    /**
     * 拡張内容キャッシュ
     */
    List<PluginInformation> mInformations;

    /**
     * ディスプレイキャッシュ
     */
    List<DisplayKey> mDisplayInformations;

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

    /**
     * 通知処理を行う
     */
    private Worker<NotificationDisplayManager> mNotificationDisplayManagerWorker = it -> {
    };

    @Inject(StorageProvider.class)
    AppSettings mSettings;

    PluginConnector(Context context, PluginManager parent, String sessionId) {
        super(context, String.format("%s", UUID.randomUUID().toString()));
        mCentralServiceMode = (context instanceof CentralService);
        mSessionId = sessionId;
        mParent = parent;
        buildCentralCommands();
        buildDisplayCommands();

        Garnet.create(this)
                .depend(Context.class, context)
                .inject();

    }

    /**
     * プラグインが有効な状態であればtrue
     */
    public boolean isActive() {
        PluginDatabase db = new PluginDatabase(mContext);
        try {
            db.openReadOnly();
            return db.isActive(mPackageInfo);
        } finally {
            db.close();
        }
    }

    public void setCentralWorker(Worker<CentralDataManager> cycleComputerDataWorker) {
        mCycleComputerDataWorker = cycleComputerDataWorker;
    }

    public void setDisplayWorker(Worker<DataDisplayManager> worker) {
        mDataDisplayManagerWorker = worker;
    }

    public void setNotificationWorker(Worker<NotificationDisplayManager> worker) {
        mNotificationDisplayManagerWorker = worker;
    }

    /**
     * 拡張機能に接続する
     */
    public void connect(ResolveInfo info) {
        this.mPackageInfo = info;
        mName = new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);

        final Intent intent = new Intent(PluginServerImpl.ACTION_ACE_EXTENSION_BIND + "@" + mSessionId);
        intent.setComponent(mName);
        intent.putExtra(PluginServerImpl.EXTRA_SESSION_ID, mSessionId);
        intent.putExtra(PluginServerImpl.EXTRA_ACE_IMPL_SDK_VERSION, BuildConfig.ACE_SDK_VERSION);
        intent.putExtra(PluginServerImpl.EXTRA_DEBUGGABLE, mSettings.isDebuggable());

        if (mCentralServiceMode) {
            intent.putExtra(PluginServerImpl.EXTRA_ACE_COMPONENT, new ComponentName(mContext, CentralService.class));
        }

        UIHandler.postUI(() -> connectToSever(intent));
    }

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
        return mPackageInfo.loadLabel(mContext.getPackageManager()).toString();
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
    public synchronized PluginInformation getInformation() {
        if (mInformations == null) {
            try {
                Payload payload = requestPostToServer(CentralDataCommand.CMD_getInformations, null);
                mInformations = PluginInformation.deserialize(payload.getBuffer());
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
        if (mDisplayInformations == null) {
            try {
                Payload payload = requestPostToServer(CentralDataCommand.CMD_getDisplayInformations, null);
                mDisplayInformations = DisplayKey.deserialize(payload.getBuffer());
            } catch (Exception e) {
                LogUtil.log(e);
                return null;
            }
        }

        return mDisplayInformations;
    }

    /**
     * Centralの起動が完了した
     */
    public void onCentralBootCompleted(@NonNull CentralContext centralContext) {
        try {
            requestPostToServer(CentralDataCommand.CMD_onCentralBootCompleted, null);
        } catch (Exception e) {
            AppLog.report(e);
        }
    }

    /**
     * 再起動を行う
     */
    public void requestReboot() {
        try {
            requestPostToServer(CentralDataCommand.CMD_requestRebootPlugin, null);
        } catch (Exception e) {

        }
    }

    /**
     * 拡張機能のON/OFFを切り替える
     */
    public void setEnable(boolean use) {
        PluginDatabase db = new PluginDatabase(mContext);
        try {
            db.openWritable();
            if (use) {
                requestPostToServer(CentralDataCommand.CMD_onExtensionEnable, null);
                db.active(getInformation(), mPackageInfo);
            } else {
                requestPostToServer(CentralDataCommand.CMD_onExtensionDisable, null);
                db.remove(mPackageInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
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
        return mCmdMap.execute(this, cmd, payload);
    }

    private void buildDisplayCommands() {
        /**
         * ディスプレイ情報を更新する
         */
        mCmdMap.addAction(DisplayCommand.CMD_setDisplayValue, (Object sender, String cmd, Payload payload) -> {
            List<DisplayData> list = DisplayData.deserialize(payload.getBuffer(), DisplayData.class);
            // 表示内容を更新する
            mDataDisplayManagerWorker.request(it -> it.putValue(PluginConnector.this, list));
            return null;
        });

        /**
         * 通知を行う
         */
        mCmdMap.addAction(DisplayCommand.CMD_queueNotification, (Object sender, String cmd, Payload payload) -> {

            NotificationData notificationData = new NotificationData(mContext, payload.getBuffer());
            // 通知を送信する
            mNotificationDisplayManagerWorker.request(it -> it.queue(notificationData));

            return null;
        });
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
         * GPS座標を更新する
         */
        mCmdMap.addAction(CentralDataCommand.CMD_setLocation, (Object sender, String cmd, Payload payload) -> {
            PluginProtocol.SrcLocation idl = payload.deserializePublicField(PluginProtocol.SrcLocation.class);
            mCycleComputerDataWorker.request(it -> it.setLocation(idl.latitude, idl.longitude, idl.altitude, idl.accuracyMeter));
            return null;
        });

        /**
         * 心拍を設定する
         */
        mCmdMap.addAction(CentralDataCommand.CMD_setHeartrate, (Object sender, String cmd, Payload payload) -> {
            PluginProtocol.SrcHeartrate idl = payload.deserializePublicField(PluginProtocol.SrcHeartrate.class);
            mCycleComputerDataWorker.request(it -> it.setHeartrate(idl.bpm));
            return null;
        });

        /**
         * S&Cセンサーを設定する
         */
        mCmdMap.addAction(CentralDataCommand.CMD_setSpeedAndCadence, (Object sender, String cmd, Payload payload) -> {
            PluginProtocol.SrcSpeedAndCadence idl = payload.deserializePublicField(PluginProtocol.SrcSpeedAndCadence.class);
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
