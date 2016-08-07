package com.eaglesakura.andriders.display.notification;

import com.eaglesakura.andriders.AppUnitTestCase;
import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.device.display.DisplayInfo;
import com.eaglesakura.android.graphics.Graphics;
import com.eaglesakura.math.Vector2;

import org.junit.Test;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.UUID;

public class NotificationDisplayManagerTest extends AppUnitTestCase {

    NotificationData newDummyNotification() {
        return new NotificationData()
                .setIcon(getContext(), R.mipmap.ic_launcher)
                .setMessage("dummy message");
    }

    @Test
    public void 通知を追加することができる() throws Throwable {
        NotificationData notificationData = newDummyNotification();
        Clock clock = new Clock(System.currentTimeMillis());
        NotificationDisplayManager manager = new NotificationDisplayManager(getContext(), clock);

        assertEquals(manager.queue(notificationData), 0);   // データを登録できる
        assertEquals(manager.queue(notificationData), -1);  // 二重には登録できない

        // 登録したばかりなら、保留中
        assertEquals(manager.hasNotification(notificationData.getUniqueId()), NotificationDisplayManager.NOTIFICATION_PENDING);
        assertEquals(manager.hasNotification("dummy_id"), NotificationDisplayManager.NOTIFICATION_NONE);    // 登録した覚えがない
    }

    @Test
    public void 通知が保留表示削除の順番で処理される() throws Throwable {
        NotificationData notificationData = newDummyNotification();

        Clock clock = new Clock(System.currentTimeMillis());
        NotificationDisplayManager manager = new NotificationDisplayManager(getContext(), clock);

        manager.queue(notificationData);

        // 1秒経過したら保留から昇格している
        assertEquals(manager.hasNotification(notificationData.getUniqueId()), NotificationDisplayManager.NOTIFICATION_PENDING);
        for (int i = 0; i < 60; ++i) {
            clock.offset(1000 / 60);
            manager.onUpdate();
            if (manager.mNotificationStates.size() != 0) {
                NotificationState state = manager.mNotificationStates.get(0);
                assertTrue(state.mInsertWeight >= 0.0);
                assertTrue(state.mInsertWeight <= 1.0);
                Bitmap image = state.mCard.mCardImage;
                Vector2 position = state.mCardPosition;
                AppLog.test("Card Pos[%d, %d] Size[%d, %d]", (int) position.x, (int) position.y, image.getWidth(), image.getHeight());
            }
        }
        assertEquals(manager.hasNotification(notificationData.getUniqueId()), NotificationDisplayManager.NOTIFICATION_SHOWING);
        assertEquals(manager.mPendingNotifications.size(), 0);
        assertEquals(manager.mNotificationStates.size(), 1);

        // 1秒経過したら、まだ表示されている
        for (int i = 0; i < 60; ++i) {
            clock.offset(1000 / 60);
            manager.onUpdate();
        }
        assertEquals(manager.hasNotification(notificationData.getUniqueId()), NotificationDisplayManager.NOTIFICATION_SHOWING);
        assertEquals(manager.mPendingNotifications.size(), 0);
        assertEquals(manager.mNotificationStates.size(), 1);

        // さらに30秒経過したら、削除されている
        for (int i = 0; i < (60 * 30); ++i) {
            clock.offset(1000 / 60);
            manager.onUpdate();
            if (manager.mNotificationStates.size() != 0) {
                NotificationState state = manager.mNotificationStates.get(0);
                assertTrue(state.mInsertWeight >= 0.0);
                assertTrue(state.mInsertWeight <= 1.0);
                Bitmap image = state.mCard.mCardImage;
                Vector2 position = state.mCardPosition;
                AppLog.test("Card Pos[%d, %d] Size[%d, %d]", (int) position.x, (int) position.y, image.getWidth(), image.getHeight());
            }
        }
        assertEquals(manager.hasNotification(notificationData.getUniqueId()), NotificationDisplayManager.NOTIFICATION_NONE);
        assertEquals(manager.mPendingNotifications.size(), 0);
        assertEquals(manager.mNotificationStates.size(), 0);
    }

    @Test
    public void レンダリングが落ちずに最後まで行える() throws Throwable {

        Clock clock = new Clock(System.currentTimeMillis());
        NotificationDisplayManager manager = new NotificationDisplayManager(getContext(), clock);

        for (int i = 0; i < 10; ++i) {
            manager.queue(newDummyNotification().setUniqueId(UUID.randomUUID().toString()));
        }

        DisplayInfo info = new DisplayInfo(getContext());
        Graphics dummyGraphics = new Graphics(new Canvas(Bitmap.createBitmap(info.getWidthPixel(), info.getHeightPixel(), Bitmap.Config.ARGB_8888)));

        // 1分経過させる
        for (int i = 0; i < (60 * 60); ++i) {
            clock.offset(1000 / 60);
            manager.onUpdate();
            manager.rendering(dummyGraphics);
        }

        // 全てのレンダリングが完了している
        assertEquals(manager.mPendingNotifications.size(), 0);
        assertEquals(manager.mNotificationStates.size(), 0);
    }
}
