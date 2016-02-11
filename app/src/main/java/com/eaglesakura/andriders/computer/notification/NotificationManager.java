package com.eaglesakura.andriders.computer.notification;

import com.eaglesakura.andriders.computer.CycleComputerManager;
import com.eaglesakura.android.graphics.Graphics;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 通知を管理する
 */
public class NotificationManager extends CycleComputerManager {

    /**
     * 通知対象のステート一覧
     */
    List<NotificationState> mNotificationStates = new ArrayList<>();

    /**
     * 保留されている通知
     */
    List<NotificationCard> mPendingNotifications = new ArrayList<>();

    public NotificationManager(Context context) {
        super(context);
    }

    @Override
    public void updateInPipeline(double deltaTimeSec) {
        updateNotifications(deltaTimeSec);
    }

    /**
     * 通知一覧を更新する
     */
    private void updateNotifications(double deltaTimeSec) {
        synchronized (mNotificationStates) {
            Iterator<NotificationState> iterator = mNotificationStates.iterator();
            int cardNumber = 0;
            while (iterator.hasNext()) {
                NotificationState state = iterator.next();
                state.update(deltaTimeSec);

                if (state.isShowFinished()) {
                    // 表示が終了したら削除する
                    state.dispose();
                    iterator.remove();
                } else {
                    // そうでなければカード番号を更新する
                    state.setCardNumber(cardNumber);
                    ++cardNumber;
                }
            }
        }
    }

    /**
     * 描画を行う
     */
    public void rendering(Graphics graphics) {
        synchronized (mNotificationStates) {

        }
    }
}
