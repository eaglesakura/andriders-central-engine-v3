package com.eaglesakura.andriders.service.central;

import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.display.notification.NotificationDisplayManager;
import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.util.AppLog;

import android.content.Intent;

/**
 * 通知表示
 */
class NotificationShowingListenerImpl implements NotificationDisplayManager.OnNotificationShowingListener {
    final CentralContext mCentralContext;

    public NotificationShowingListenerImpl(CentralContext centralContext) {
        mCentralContext = centralContext;
    }

    @Override
    public void onNotificationShowing(NotificationDisplayManager self, NotificationData data) {

        try {
            Intent intent = new Intent();
            intent.setAction(CentralDataReceiver.ACTION_RECEIVED_NOTIFICATION);
            intent.addCategory(CentralDataReceiver.INTENT_CATEGORY);

            byte[] rawNotification = data.serialize();
            intent.putExtra(CentralDataReceiver.EXTRA_NOTIFICATION_DATA, rawNotification);

            mCentralContext.mContext.sendBroadcast(intent);

            // ローカル伝達
            mCentralContext.mLocalReceiver.onReceivedNotificationData(rawNotification);
        } catch (Throwable e) {
            AppLog.report(e);
        }

    }
}
