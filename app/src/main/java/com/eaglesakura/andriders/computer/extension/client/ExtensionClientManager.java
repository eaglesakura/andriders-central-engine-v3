package com.eaglesakura.andriders.computer.extension.client;

import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.computer.display.DisplayManager;
import com.eaglesakura.andriders.computer.notification.NotificationManager;
import com.eaglesakura.andriders.extension.DisplayInformation;
import com.eaglesakura.andriders.extension.ExtensionCategory;
import com.eaglesakura.andriders.extension.ExtensionInformation;
import com.eaglesakura.andriders.extension.internal.ExtensionServerImpl;
import com.eaglesakura.android.thread.async.AsyncTaskController;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.thread.async.IAsyncTask;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.util.Timer;
import com.eaglesakura.util.Util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExtensionClientManager {
    final Context mContext;

    CentralDataManager mCentralDataManager;

    DisplayManager mDisplayManager;

    NotificationManager mNotificationManager;

    /**
     * 接続されたクライアント一覧
     */
    List<ExtensionClient> extensions = new ArrayList<>();

    /**
     * 処理を直列化するためのパイプライン
     */
    AsyncTaskController mPipeline = new AsyncTaskController(1);

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

    public void setCentralDataManager(CentralDataManager centralDataManager) {
        mCentralDataManager = centralDataManager;
    }

    public void setDisplayManager(DisplayManager displayManager) {
        mDisplayManager = displayManager;
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
        synchronized (extensions) {
            return !Util.isEmpty(extensions);
        }
    }

    /**
     * 表示設定を持つクライアントのみを列挙する
     */
    public List<ExtensionClient> listDisplayClients() {
        synchronized (extensions) {
            List<ExtensionClient> result = new ArrayList<>();
            for (ExtensionClient client : extensions) {
                if (!Util.isEmpty(client.getDisplayInformations())) {
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
        synchronized (extensions) {
            for (ExtensionClient client : extensions) {
                List<DisplayInformation> informations = client.getDisplayInformations();
                if (Util.isEmpty(informations)) {
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
        synchronized (extensions) {
            return new ArrayList<>(extensions);
        }
    }

    /**
     * 指定したカテゴリのクライアント一覧を返す
     */
    public List<ExtensionClient> listClients(ExtensionCategory category) {
        synchronized (extensions) {
            List<ExtensionClient> result = new ArrayList<>();
            for (ExtensionClient client : extensions) {
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

        synchronized (extensions) {
            extensions.add(impl);
        }
    }

    /**
     * 拡張サービスに接続する
     * <p/>
     * 接続完了はコールバックで受け取れる。
     */
    public AsyncTaskResult<ExtensionClientManager> connect(ConnectMode mode) {
        return mPipeline.pushBack(new IAsyncTask<ExtensionClientManager>() {
            @Override
            public ExtensionClientManager doInBackground(AsyncTaskResult<ExtensionClientManager> result) throws Exception {
                List<ResolveInfo> services = listExtensionServices(mContext);
                for (ResolveInfo info : services) {
                    connectService(info);
                }
                return ExtensionClientManager.this;
            }
        });
    }

    /**
     * 拡張サービスから切断する
     */
    public AsyncTaskResult<ExtensionClientManager> disconnect() {
        return mPipeline.pushBack(new IAsyncTask<ExtensionClientManager>() {
            @Override
            public ExtensionClientManager doInBackground(AsyncTaskResult<ExtensionClientManager> result) throws Exception {
                for (final ExtensionClient impl : extensions) {
                    UIHandler.postWithWait(new Runnable() {
                        @Override
                        public void run() {
                            impl.disconnect();
                        }
                    }, 1000 * 5);
                }
                extensions.clear();
                return null;
            }
        });
    }
}
