package com.eaglesakura.andriders.computer.notification;

import com.eaglesakura.android.framework.context.Resources;
import com.eaglesakura.android.graphics.Graphics;
import com.eaglesakura.math.Vector2;
import com.eaglesakura.util.MathUtil;

/**
 * 通知のレンダリングステート
 */
public class NotificationState {
    /**
     * カードの挿入速度を指定しておく
     */
    final float INOUT_TIME_SEC = 0.5f;

    /**
     * 通知終了時刻
     */
    long showEndTime;

    final long showStartTime = System.currentTimeMillis();

    /**
     * 表示しているカード番号
     */
    int cardNumber = 0;

    /**
     * 表示カード
     */
    NotificationCard card;


    /**
     * 開始レベル
     */
    float insertWeight = 0;

    /**
     * 現在の描画位置
     */
    Vector2 cardPosition = new Vector2();

    public NotificationState(NotificationCard card, int cardNumber) {
        this.card = card;
        this.showEndTime = System.currentTimeMillis() + card.getShowTimeMs();
        this.cardNumber = cardNumber;

        // カード用画像を構築する
        card.buildCardImage();

        // 現在のカード位置を初期化する
        cardPosition.set(getTargetPositionX(), getTargetPositionY());
    }

    /**
     * 現在表示すべきY位置を取得する
     */
    float getTargetPositionY() {
        final int CARD_HEIGHT = card.getCardImage().getHeight();
        return (float) CARD_HEIGHT * (cardNumber + NotificationCard.NOTIFICATION_TOPMARGIN_NUM);
    }

    /**
     * Y方向の移動速度を取得する
     */
    float getMoveSpeedY(double deltaTimeSec) {
        final double CARD_HEIGHT = card.getCardImage().getHeight();
        return (float) (CARD_HEIGHT * deltaTimeSec / INOUT_TIME_SEC);
    }

    /**
     * 現在表示すべきX位置を取得する
     */
    float getTargetPositionX() {
        final int CARD_WIDTH = card.getCardImage().getWidth();
        return (float) Resources.displaySize()[0] - (float) CARD_WIDTH * insertWeight;
    }

    public void setCardNumber(int cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getCardNumber() {
        return cardNumber;
    }

    /**
     * レンダリングしている時間を取得する
     */
    public long getShowTime() {
        return System.currentTimeMillis() - showStartTime;
    }

    /**
     * 表示が終了していたらtrue
     */
    public boolean isShowFinished() {
        return System.currentTimeMillis() > showEndTime && insertWeight <= 0;
    }

    /**
     * 用済みになったら解放を行う
     */
    public void dispose() {
        if (card != null) {
            card.dispose();
            card = null;
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
            insertWeight = (float) getShowTime() / (INOUT_TIME_SEC * 1000);
        } else if (getShowTime() > card.getShowTimeMs()) {
            // カードの表示時間を超えている
            float overTime = getShowTime() - card.getShowTimeMs();

            // カードを引っ込める
            insertWeight = 1.0f - (overTime / (INOUT_TIME_SEC * 1000));
        } else {
            insertWeight = 1.0f;
        }

        // カードを移動する
        {
            cardPosition.x = getTargetPositionX();
            cardPosition.y = MathUtil.targetMove(cardPosition.y, getMoveSpeedY(deltaTimeSec), getTargetPositionY());
        }

    }

    /**
     * レンダリングと更新を行う
     */
    public void rendering(Graphics graphics) {
        // 所定位置にレンダリングする
        graphics.setColorARGB(0xFFFFFFFF);
        graphics.drawBitmap(card.getCardImage(), (int) cardPosition.x, (int) cardPosition.y);
    }
}
