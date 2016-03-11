package com.eaglesakura.andriders.service.central;

import com.eaglesakura.andriders.central.CentralDataManager;
import com.eaglesakura.andriders.computer.display.DisplayManager;
import com.eaglesakura.andriders.computer.extension.client.ExtensionClientManager;
import com.eaglesakura.andriders.computer.notification.NotificationManager;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.io.Disposable;

import android.app.Service;

import rx.subjects.BehaviorSubject;

/**
 * ACEの表示状態
 */
public class CentralContext implements Disposable {

    /**
     * 設定
     */
    private final Settings mSettings = Settings.getInstance();

    private final Service mContext;

    /**
     * サイコン情報を表示する場合はtrue
     */
    private boolean mDisplayEnable = true;

    /**
     * 通知情報を表示する場合はtrue
     */
    private boolean mNotificationEnable = true;

    /**
     * サイコンデータ本体
     */
    private CentralDataManager mCentralDataManager;

    /**
     * サイコン表示内容管理
     */
    private DisplayManager mDisplayManager;

    /**
     * 通知内容管理
     */
    private NotificationManager mNotificationManager;

    /**
     * 拡張機能管理
     */
    private ExtensionClientManager mExtensionClientManager;

    /**
     * 廃棄済
     */
    private boolean mDisposed = false;

    private BehaviorSubject<LifecycleState> mSubject = BehaviorSubject.create(LifecycleState.NewObject);

    private SubscriptionController mSubscriptionController = new SubscriptionController().bind(mSubject);

    public CentralContext(Service context) {
        mContext = context;

        mCentralDataManager = new CentralDataManager(mContext, mSubscriptionController);
        mDisplayManager = new DisplayManager(mContext, mSubscriptionController);
        mNotificationManager = new NotificationManager(mContext, mSubscriptionController);

        mExtensionClientManager = new ExtensionClientManager(mContext);
        mExtensionClientManager.setCentralDataManager(mCentralDataManager);
        mExtensionClientManager.setDisplayManager(mDisplayManager);
    }

    public CentralDataManager getCentralDataManager() {
        return mCentralDataManager;
    }

    public DisplayManager getDisplayManager() {
        return mDisplayManager;
    }

    public NotificationManager getNotificationManager() {
        return mNotificationManager;
    }

    public ExtensionClientManager getExtensionClientManager() {
        return mExtensionClientManager;
    }

    /**
     * データ接続を開始する
     */
    public void onServiceInitializeCompleted() {
        throw new IllegalAccessError("not impl");
//        mExtensionClientManager
//                .connect(ExtensionClientManager.ConnectMode.Enabled);
    }

    /**
     * 定期更新を行う
     */
    public void onUpdated(final double deltaSec) {
        throw new IllegalAccessError("not impl");
//        mPipeline.pushBack(new Runnable() {
//            @Override
//            public void run() {
//                if (mDisposed) {
//                    return;
//                }
//
//                mCentralDataManager.updateInPipeline(deltaSec);
//                mDisplayManager.updateInPipeline(deltaSec);
//                mNotificationManager.updateInPipeline(deltaSec);
//            }
//        });
    }

    @Override
    public void dispose() {
        mSubject.onNext(LifecycleState.OnDestroyed);
        throw new IllegalAccessError("not impl");
//        mPipeline.pushBack(new Runnable() {
//            @Override
//            public void run() {
//                mCentralDataManager.finishSession();
//                mExtensionClientManager.disconnect();
//                mDisposed = true;
//            }
//        });
    }

}
