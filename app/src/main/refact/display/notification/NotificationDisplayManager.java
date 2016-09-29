package com.eaglesakura.andriders.display.notification;

import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.android.device.display.DisplayInfo;
import com.eaglesakura.android.graphics.Graphics;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 通知を管理する
 *
 * MEMO: Clockは外部要因によって更新される
 */
public class NotificationDisplayManager {

    /**
     * 通知対象のステート一覧
     */
    final List<NotificationState> mNotificationStates = new ArrayList<>();

    /**
     * 保留されている通知
     */
    final List<NotificationCard> mPendingNotifications = new ArrayList<>();

    @NonNull
    final Clock mClock;

    @NonNull
    final ClockTimer mClockTimer;

    @NonNull
    final Context mContext;

    @NonNull
    final DisplayInfo mDisplayInfo;

    final Set<OnNotificationShowingListener> mShowingListeners = new HashSet<>();

    private final Object lock = new Object();

    public NotificationDisplayManager(@NonNull Context context, @NonNull Clock clock) {
        mClock = clock;
        mContext = context.getApplicationContext();
        mDisplayInfo = new DisplayInfo(context);
        mClockTimer = new ClockTimer(clock);
    }

    public static final int NOTIFICATION_NONE = 0;

    public static final int NOTIFICATION_SHOWING = 1;

    public static final int NOTIFICATION_PENDING = 2;

    /**
     * 指定したNotificationDataを保持しているか確認する
     *
     * @param uniqueId 通知ID
     */
    public int hasNotification(String uniqueId) {
        synchronized (lock) {
            for (NotificationState state : mNotificationStates) {
                if (state.getUniqueId().equals(uniqueId)) {
                    // 表示中
                    return NOTIFICATION_SHOWING;
                }
            }

            for (NotificationCard card : mPendingNotifications) {
                if (card.getUniqueId().equals(uniqueId)) {
                    // 保留中
                    return NOTIFICATION_PENDING;
                }
            }

            return NOTIFICATION_NONE;
        }
    }

    public void addListener(OnNotificationShowingListener listener) {
        synchronized (lock) {
            mShowingListeners.add(listener);
        }
    }

    /**
     * 通知を表示キューに追加する
     *
     * @param notificationData 通知
     * @return キューイングのインデックス, 既に同じIDの通知が登録済みである場合0未満の値
     */
    public int queue(NotificationData notificationData) {
        synchronized (lock) {
            if (hasNotification(notificationData.getUniqueId()) != NOTIFICATION_NONE) {
                // 既に同じIDが登録されているので何もしない
                return -1;
            }

            NotificationCard card = new NotificationCard(notificationData);
            mPendingNotifications.add(card);
            return mPendingNotifications.size() - 1;
        }
    }

    /**
     * 更新を行う
     *
     * 時間経過はコンストラクタで渡されたClockによって管理される。
     */
    public void onUpdate() {
        synchronized (lock) {
            int cardNumber = 0;

            final double DELTA_SEC = mClockTimer.endSec();

            // 既存のカードを更新する
            {
                Iterator<NotificationState> iterator = mNotificationStates.iterator();
                while (iterator.hasNext()) {
                    NotificationState state = iterator.next();
                    state.update(DELTA_SEC);

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

            // カード数が規定以下であれば、追加する
            {
                Iterator<NotificationCard> iterator = mPendingNotifications.iterator();
                while (cardNumber < NotificationCard.MAX_NOTIFICATION_CARDS && iterator.hasNext()) {
                    NotificationCard next = iterator.next();
                    NotificationState state = new NotificationState(mDisplayInfo, next, mClock);

                    // 更新する
                    state.setCardNumber(cardNumber);
                    state.update(DELTA_SEC);

                    mNotificationStates.add(state);

                    iterator.remove();
                    ++cardNumber;

                    for (OnNotificationShowingListener listener : mShowingListeners) {
                        listener.onNotificationShowing(this, next.mNotificationData);
                    }
                }
            }
        }
    }

    /**
     * 描画を行う
     */
    public void rendering(Graphics graphics) {
        synchronized (lock) {
            for (NotificationState state : mNotificationStates) {
                state.rendering(graphics);
            }
        }
    }

    public interface OnNotificationShowingListener {
        /**
         * 通知の表示を開始した
         *
         * @param self Manager本体
         * @param data 表示開始したデータ
         */
        void onNotificationShowing(NotificationDisplayManager self, NotificationData data);
    }
}
