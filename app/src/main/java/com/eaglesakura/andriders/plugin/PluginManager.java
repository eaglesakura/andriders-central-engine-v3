package com.eaglesakura.andriders.plugin;

import com.eaglesakura.andriders.db.plugin.PluginDatabase;
import com.eaglesakura.andriders.plugin.internal.PluginServerImpl;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.Timer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PluginManager {
    final Context mContext;

    /**
     * 接続されたクライアント一覧
     */
    List<PluginConnector> mPlugins = new ArrayList<>();

    public enum ConnectMode {
        /**
         * すべてのクライアントを列挙する
         */
        All,

        /**
         * 有効化されているクライアントのみを列挙する
         */
        ActiveOnly,
    }

    public PluginManager(Context context) {
        this.mContext = context;
    }

    private static String genUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 4) + (System.nanoTime() % 1000);
    }

    /**
     * 拡張Service一覧を取得する
     */
    public static List<ResolveInfo> listExtensionServices(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(PluginServerImpl.ACTION_ACE_EXTENSION_BIND);
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, 0);
        return resolveInfos;
    }

    /**
     * 接続済であればtrue
     */
    public boolean isConnected() {
        synchronized (mPlugins) {
            return !CollectionUtil.isEmpty(mPlugins);
        }
    }

    /**
     * 表示設定を持つクライアントのみを列挙する
     */
    public List<PluginConnector> listDisplayClients() {
        synchronized (mPlugins) {
            List<PluginConnector> result = new ArrayList<>();
            for (PluginConnector client : mPlugins) {
                if (!CollectionUtil.isEmpty(client.getDisplayInformationList())) {
                    result.add(client);
                }
            }
            return result;
        }
    }

    public PluginConnector findClient(String id) {
        for (PluginConnector client : listClients()) {
            if (client.getInformation().getId().equals(id)) {
                return client;
            }
        }
        return null;
    }

    public DisplayKey findDisplayInformation(String extensionId, String displayId) {
        PluginConnector client = findClient(extensionId);
        if (client != null) {
            return client.findDisplayInformation(displayId);
        } else {
            return null;
        }
    }

    /**
     * ディスプレイ表示内容から拡張機能を逆引きする
     */
    public PluginConnector findDisplayClient(DisplayKey check) {
        synchronized (mPlugins) {
            for (PluginConnector client : mPlugins) {
                List<DisplayKey> informations = client.getDisplayInformationList();
                if (CollectionUtil.isEmpty(informations)) {
                    continue;
                }

                for (DisplayKey info : informations) {
                    if (info == check) {
                        return client;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 拡張Service一覧を取得する
     */
    public List<PluginConnector> listClients() {
        synchronized (mPlugins) {
            return new ArrayList<>(mPlugins);
        }
    }

    /**
     * 指定したカテゴリのクライアント一覧を返す
     */
    public List<PluginConnector> listClients(Category category) {
        synchronized (mPlugins) {
            List<PluginConnector> result = new ArrayList<>();
            for (PluginConnector client : mPlugins) {
                // 拡張機能を列挙する
                PluginInformation information = client.getInformation();
                if (category.equals(information.getCategory())) {
                    result.add(client);
                }
            }

            return result;
        }
    }

    /**
     * 指定したServiceに接続する
     */
    private void connectService(final ResolveInfo info) {
        final PluginConnector impl = new PluginConnector(mContext, this, genUUID());
        impl.connect(info);

        Timer timer = new Timer();
        while (!impl.isConnected()) {
            if (timer.end() > (1000 * 10)) {
                throw new IllegalStateException("Connect Error!!");
            }
        }

        synchronized (mPlugins) {
            mPlugins.add(impl);
        }
    }

    /**
     * 拡張サービスに接続する
     * <p>
     * ブロッキングで接続完了する。
     */
    public void connect(ConnectMode mode) {
        AndroidThreadUtil.assertBackgroundThread();


        List<ResolveInfo> services = listExtensionServices(mContext);
        if (mode == ConnectMode.ActiveOnly) {
            // 有効なプラグインだけを列挙する
            PluginDatabase db = new PluginDatabase(mContext);
            try {
                db.openReadOnly();
                for (ResolveInfo info : services) {
                    if (db.isActive(info)) {
                        AppLog.plugin("Active Plugin[%s@%s]", info.serviceInfo.packageName, info.serviceInfo.name);
                        connectService(info);
                    }
                }
            } finally {
                db.close();
            }
        } else {
            // 全てのプラグインを接続する
            for (ResolveInfo info : services) {
                AppLog.plugin("Connect Plugin[%s@%s]", info.serviceInfo.packageName, info.serviceInfo.name);
                connectService(info);
            }
        }
    }

    /**
     * 全ての拡張サービスから切断する
     */
    public void disconnect() {
        AndroidThreadUtil.assertBackgroundThread();

        UIHandler.postWithWait(() -> {
            for (final PluginConnector impl : mPlugins) {
                impl.disconnect();
            }
        }, 1000 * 15);
        mPlugins.clear();
    }

}
