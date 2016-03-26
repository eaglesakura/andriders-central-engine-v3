package com.eaglesakura.andriders.display.notification;

import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.android.graphics.Graphics;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    public NotificationDisplayManager(@NonNull Context context, @NonNull Clock clock) {
        mClock = clock;
        mContext = context.getApplicationContext();
        mClockTimer = new ClockTimer(clock);
    }

    /**
     * 更新を行う
     *
     * 時間経過はコンストラクタで渡されたClockによって管理される。
     */
    public void onUpdate() {
        synchronized (mNotificationStates) {
            Iterator<NotificationState> iterator = mNotificationStates.iterator();
            int cardNumber = 0;
            while (iterator.hasNext()) {
                NotificationState state = iterator.next();
                state.update(mClockTimer.endSec());

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
