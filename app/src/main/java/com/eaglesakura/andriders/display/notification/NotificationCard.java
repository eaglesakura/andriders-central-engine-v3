package com.eaglesakura.andriders.display.notification;

import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.notification.NotificationLength;
import com.eaglesakura.android.device.display.DisplayInfo;
import com.eaglesakura.android.graphics.Font;
import com.eaglesakura.android.graphics.Graphics;
import com.eaglesakura.android.util.ImageUtil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 通知用カード
 */
public class NotificationCard {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("HH:mm:ss");

    /**
     * 通知カード
     */
    Bitmap cardImage;

    /**
     * 通知データ
     */
    NotificationData notificationData;

    public NotificationCard(NotificationData data) {
        this.notificationData = data;
    }

    /**
     * カード用画像を構築する
     */
    public void buildCardImage(@NonNull DisplayInfo displayInfo) {

        int[] displaySize = {displayInfo.getWidthPixel(), displayInfo.getHeightPixel()};

        // 1カードの幅と高さ
        // 比率は必ず16:9になるようにする
        final int CARD_HEIGHT = displaySize[1] / (MAX_NOTIFICATION_CARDS + NOTIFICATION_BOTTOMMARGIN_NUM + NOTIFICATION_TOPMARGIN_NUM);
        final int CARD_WIDTH = (int) (((float) CARD_HEIGHT / 9.0f) * (32.0f + 9.0f));   // アイコン領域＋16:9の通知領域を持たせる
//        final int CARD_WIDTH = (int) ((float) displaySize[0] * 0.4f);

        // カードを生成する
        cardImage = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888);

        Graphics graphics = new Graphics(new Canvas(cardImage));
        graphics.setAntiAlias(true);

        // カードの背景を染める
        graphics.setColorARGB(notificationData.getBackgroundColor());
        graphics.fillRoundRect(0, 0, CARD_WIDTH, CARD_HEIGHT, (float) CARD_HEIGHT * 0.05f);

        // 囲い線を入れる
        graphics.setColorARGB(ImageUtil.getNegaColor(notificationData.getBackgroundColor()));
        graphics.drawRoundRect(0, 0, CARD_WIDTH, CARD_HEIGHT, (float) CARD_HEIGHT * 0.05f);

        // アイコンを描画する
        // 多少内側に入れる
        {
            int INSET_PIXEL = 1;
            graphics.drawBitmap(notificationData.getIcon(), INSET_PIXEL, INSET_PIXEL, CARD_HEIGHT - (INSET_PIXEL * 2), CARD_HEIGHT - (INSET_PIXEL * 2));
        }

        // 通知文字列を描画する
        {
            int fontColor = ImageUtil.getNegaColor(notificationData.getBackgroundColor());

            final int FONT_AREA_WIDTH = CARD_WIDTH - (int) ((float) CARD_HEIGHT * 1.1f);
            final int FONT_HEIGHT = CARD_HEIGHT / MAX_NOTIFICATION_MESSAGE_LINES;
            final int FONT_MARGIN = 2;

            Font font = new Font();
            font.setColorARGB(fontColor);
            Canvas canvas = graphics.getCanvas();
            font.drawString(
                    String.format("%s\n%s", DATE_FORMATTER.format(new Date()), notificationData.getMessage()),
                    "...",
                    (int) ((float) CARD_HEIGHT * 1.05f), FONT_MARGIN,
                    FONT_HEIGHT,
                    FONT_AREA_WIDTH,
                    MAX_NOTIFICATION_MESSAGE_LINES, FONT_MARGIN,
                    canvas);
        }
    }

    public void dispose() {
        if (cardImage != null) {
            cardImage.recycle();
        }
        cardImage = null;

        if (notificationData != null) {
            notificationData.getIcon().recycle();
            notificationData = null;
        }
    }

    public int getShowTimeMs() {
        return (int) getNotificationTimeMs(notificationData.getNotificationLength());
    }

    /**
     * 表示カードを取得する
     */
    public Bitmap getCardImage() {
        return cardImage;
    }

    /**
     * 通知時間から表示時間（ミリ秒）へ変換する
     */
    public static long getNotificationTimeMs(NotificationLength length) {
        switch (length) {
            case Short:
                return 1000 * 5;
            case Long:
                return 1000 * 30;
            default: // Normal:
                return 1000 * 10;
        }
    }

    /**
     * 通知に表示できる最大行数
     */
    public static final int MAX_NOTIFICATION_MESSAGE_LINES = 3;

    /**
     * 同時に通知可能な最大カード数
     */
    public static final int MAX_NOTIFICATION_CARDS = 5;

    /**
     * マージンとして上に開けておくスロット数
     */
    public static final int NOTIFICATION_TOPMARGIN_NUM = 2;

    /**
     * マージンとして下に開けておくスロット数
     */
    public static final int NOTIFICATION_BOTTOMMARGIN_NUM = 2;
}
