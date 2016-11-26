package com.eaglesakura.andriders.data.notification;

import com.eaglesakura.andriders.util.Clock;
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

    /**
     * 表示時間（秒）
     */
    double mShowTimeSec = 0.0;

    /**
     * 表示しているカード番号
     */
    int mCardNumber = NotificationCard.MAX_NOTIFICATION_CARDS;

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
    final Vector2 mCardPosition = new Vector2();

    @NonNull
    final DisplayInfo mDisplayInfo;

    public NotificationState(DisplayInfo displayInfo, @NonNull NotificationCard card, @NonNull Clock clock) {
        mCard = card;
        mDisplayInfo = displayInfo;

        // カード用画像を構築する
        card.buildCardImage(displayInfo, clock);

        // 現在のカード位置を初期化する
        mCardPosition.set(getTargetPositionX(), getTargetPositionY());
    }

    public String getUniqueId() {
        return mCard.getUniqueId();
    }

    /**
     * 現在表示すべきY位置を取得する
     */
    float getTargetPositionY() {
        final int CARD_HEIGHT = mCard.getCardImage().getHeight();
        return (float) CARD_HEIGHT * (mCardNumber + NotificationCard.NOTIFICATION_TOP_MARGIN_NUM);
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

    /**
     * レンダリングしている時間を取得する
     */
    public long getShowTimeMs() {
        return (long) (mShowTimeSec * 1000.0);
    }

    /**
     * 表示が終了していたらtrue
     */
    public boolean isShowFinished() {
        return (mShowTimeSec > mCard.getShowTimeSec()) && mInsertWeight <= 0;
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
        // inとoutは早めにする
        if (mShowTimeSec < INOUT_TIME_SEC) {
            // カードの出現
            mInsertWeight = (float) (mShowTimeSec / INOUT_TIME_SEC);
//            AppLog.test("mInsertWeight[%.3f]", mInsertWeight);
        } else if (mShowTimeSec > mCard.getShowTimeSec()) {
            // カードの表示時間を超えている
            double overTime = mShowTimeSec - mCard.getShowTimeSec();

            // カードを引っ込める
            mInsertWeight = 1.0f - (float) (overTime / INOUT_TIME_SEC);
//            AppLog.test("mInsertWeight[%.3f]", mInsertWeight);
        } else {
            mInsertWeight = 1.0f;
//            AppLog.test("mInsertWeight[%.3f]", mInsertWeight);
        }

        // カードを移動する
        {
            mCardPosition.x = getTargetPositionX();
            mCardPosition.y = MathUtil.targetMove(mCardPosition.y, getMoveSpeedY(deltaTimeSec), getTargetPositionY());
        }

        mShowTimeSec += deltaTimeSec;
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
