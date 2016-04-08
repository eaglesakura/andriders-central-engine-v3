package com.eaglesakura.andriders.display.notification;

import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.android.device.display.DisplayInfo;
import com.eaglesakura.android.graphics.Graphics;
import com.eaglesakura.math.Vector2;
import com.eaglesakura.util.MathUtil;

import android.support.annotation.NonNull;

/**
 * 通知のレンダリングステート
 */
public class NotificationState {
    /**
     * カードの挿入速度を指定しておく
     */
    final float INOUT_TIME_SEC = 0.5f;

    @NonNull
    final ClockTimer mClockTimer;

    /**
     * 表示しているカード番号
     */
    int mCardNumber = 0;

    /**
     * 表示カード
     */
    NotificationCard mCard;


    /**
     * 開始レベル
     */
    float mInsertWeight = 0;

    /**
     * 現在の描画位置
     */
    Vector2 mCardPosition = new Vector2();

    @NonNull
    DisplayInfo mDisplayInfo;

    public NotificationState(DisplayInfo displayInfo, @NonNull NotificationCard card, int cardNumber, @NonNull Clock clock) {
        mCard = card;
        mClockTimer = new ClockTimer(clock);
        mCardNumber = cardNumber;

        // カード用画像を構築する
        card.buildCardImage(displayInfo);

        // 現在のカード位置を初期化する
        mCardPosition.set(getTargetPositionX(), getTargetPositionY());
    }

    /**
     * 現在表示すべきY位置を取得する
     */
    float getTargetPositionY() {
        final int CARD_HEIGHT = mCard.getCardImage().getHeight();
        return (float) CARD_HEIGHT * (mCardNumber + NotificationCard.NOTIFICATION_TOPMARGIN_NUM);
    }

    /**
     * Y方向の移動速度を取得する
     */
    float getMoveSpeedY(double deltaTimeSec) {
        final double CARD_HEIGHT = mCard.getCardImage().getHeight();
        return (float) (CARD_HEIGHT * deltaTimeSec / INOUT_TIME_SEC);
    }

    /**
     * 現在表示すべきX位置を取得する
     */
    float getTargetPositionX() {
        final int CARD_WIDTH = mCard.getCardImage().getWidth();
        return (float) mDisplayInfo.getWidthPixel() - (float) CARD_WIDTH * mInsertWeight;
    }

    public void setCardNumber(int cardNumber) {
        this.mCardNumber = cardNumber;
    }

    public int getCardNumber() {
        return mCardNumber;
    }

    /**
     * レンダリングしている時間を取得する
     */
    public long getShowTime() {
        return mClockTimer.end();
    }

    /**
     * 表示が終了していたらtrue
     */
    public boolean isShowFinished() {
        return (getShowTime() > mCard.getShowTimeMs()) && mInsertWeight <= 0;
    }

    /**
     * 用済みになったら解放を行う
     */
    public void dispose() {
        if (mCard != null) {
            mCard.dispose();
            mCard = null;
        }
    }

    /**
     * 位置を更新させる
     *
     * @param deltaTimeSec 経過時間（秒）
     */
    public void update(double deltaTimeSec) {
        // inとoutは1000msで行う
        if (getShowTime() < (INOUT_TIME_SEC * 1000)) {
            // カードの出現
            mInsertWeight = (float) getShowTime() / (INOUT_TIME_SEC * 1000);
        } else if (getShowTime() > mCard.getShowTimeMs()) {
            // カードの表示時間を超えている
            float overTime = getShowTime() - mCard.getShowTimeMs();

            // カードを引っ込める
            mInsertWeight = 1.0f - (overTime / (INOUT_TIME_SEC * 1000));
        } else {
            mInsertWeight = 1.0f;
        }

        // カードを移動する
        {
            mCardPosition.x = getTargetPositionX();
            mCardPosition.y = MathUtil.targetMove(mCardPosition.y, getMoveSpeedY(deltaTimeSec), getTargetPositionY());
        }

    }

    /**
     * レンダリングと更新を行う
     */
    public void rendering(Graphics graphics) {
        // 所定位置にレンダリングする
        graphics.setColorARGB(0xFFFFFFFF);
        graphics.drawBitmap(mCard.getCardImage(), (int) mCardPosition.x, (int) mCardPosition.y);
    }
}
