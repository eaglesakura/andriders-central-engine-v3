package com.eaglesakura.andriders.extension;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.eaglesakura.andriders.extension.internal.ExtensionServerImpl;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExtensionClientManager {
    final Context mContext;

    /**
     * 接続されたクライアント一覧
     */
    List<ExtensionClient> mExtensions = new ArrayList<>();

    public enum ConnectMode {
        /**
         * すべてのクライアントを列挙する
         */
        All,

        /**
         * 有効化されているクライアントのみを列挙する
         */
        Enabled,
    }

    public ExtensionClientManager(Context context) {
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
        Intent intent = new Intent(ExtensionServerImpl.ACTION_ACE_EXTENSION_BIND);
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, 0);
        return resolveInfos;
    }

    /**
     * 接続済であればtrue
     */
    public boolean isConnected() {
        synchronized (mExtensions) {
            return !CollectionUtil.isEmpty(mExtensions);
        }
    }

    /**
     * 表示設定を持つクライアントのみを列挙する
     */
    public List<ExtensionClient> listDisplayClients() {
        synchronized (mExtensions) {
            List<ExtensionClient> result = new ArrayList<>();
            for (ExtensionClient client : mExtensions) {
                if (!CollectionUtil.isEmpty(client.getDisplayInformations())) {
                    result.add(client);
                }
            }
            return result;
        }
    }

    public ExtensionClient findClient(String id) {
        for (ExtensionClient client : listClients()) {
            if (client.getInformation().getId().equals(id)) {
                return client;
            }
        }
        return null;
    }

    public DisplayInformation findDisplayInformation(String extensionId, String displayId) {
        ExtensionClient client = findClient(extensionId);
        if (client != null) {
            return client.findDisplayInformation(displayId);
        } else {
            return null;
        }
    }

    /**
     * ディスプレイ表示内容から拡張機能を逆引きする
     */
    public ExtensionClient findDisplayClient(DisplayInformation check) {
        synchronized (mExtensions) {
            for (ExtensionClient client : mExtensions) {
                List<DisplayInformation> informations = client.getDisplayInformations();
                if (CollectionUtil.isEmpty(informations)) {
                    continue;
                }

                for (DisplayInformation info : informations) {
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
    public List<ExtensionClient> listClients() {
        synchronized (mExtensions) {
            return new ArrayList<>(mExtensions);
        }
    }

    /**
     * 指定したカテゴリのクライアント一覧を返す
     */
    public List<ExtensionClient> listClients(ExtensionCategory category) {
        synchronized (mExtensions) {
            List<ExtensionClient> result = new ArrayList<>();
            for (ExtensionClient client : mExtensions) {
                // 拡張機能を列挙する
                ExtensionInformation information = client.getInformation();
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
        final ExtensionClient impl = new ExtensionClient(mContext, this, genUUID());
        impl.connect(info);

        Timer timer = new Timer();
        while (!impl.isConnected()) {
            if (timer.end() > (1000 * 10)) {
                throw new IllegalStateException("Connect Error!!");
            }
        }

        synchronized (mExtensions) {
            mExtensions.add(impl);
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
        for (ResolveInfo info : services) {
            connectService(info);
        }
    }

    /**
     * 全ての拡張サービスから切断する
     */
    public void disconnect() {
        AndroidThreadUtil.assertBackgroundThread();

        UIHandler.postWithWait(() -> {
            for (final ExtensionClient impl : mExtensions) {
                impl.disconnect();
            }
        }, 1000 * 15);
        mExtensions.clear();
    }

}
