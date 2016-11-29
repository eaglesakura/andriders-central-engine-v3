package com.eaglesakura.andriders.service.command;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.model.command.CommandData;
import com.eaglesakura.andriders.model.command.CommandDataCollection;
import com.eaglesakura.andriders.service.ui.AnimationFrame;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.android.graphics.Font;
import com.eaglesakura.android.graphics.Graphics;
import com.eaglesakura.android.util.AndroidUtil;
import com.squareup.otto.Subscribe;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.HashMap;
import java.util.Map;

/**
 * 近接コマンドの操作を行う
 */
public class ProximityFeedbackManager {
    /**
     *
     */
    @NonNull
    private final Context mContext;

    /**
     * 現在時刻チェック
     */
    private final Clock mClock;

    /**
     * 現在の近接状態
     */
    private ProximityData mCurrentProximity;

    /**
     * 近接コマンド一覧
     */
    private CommandDataCollection mProximityCommands;

    @ColorInt
    private final int[] BACKGROUND_COLOR_TABLE = {
            0xFFff4500,
            0xFF87cefa,
            0xFF228b22,
            0xFF7cfc00,
            0xFFffff00,
    };

    /**
     * レンダリング対象のアイコン一覧
     */
    Map<CommandKey, Bitmap> mIcons = new HashMap<>();

    /**
     * 最後に振動フィードバックを行った秒数
     */
    int mLastVibeSec;

    public ProximityFeedbackManager(@NonNull Context context, @NonNull Clock clock) {
        mContext = context;
        mClock = clock;
    }

    public void setProximityCommands(CommandDataCollection proximityCommands) {
        mProximityCommands = proximityCommands;
    }

    private static final int VIBRATE_TIME_SHORT = 100;

    private static final int VIBRATE_TIME_LONG = 500;

    /**
     * 近接コマンド状態が更新された
     */
    @Subscribe
    private void onUpdateProximity(ProximityData.Bus data) {
        mCurrentProximity = data.getData();
        if (!mCurrentProximity.isProximity()) {
            // 手を話したので状態をリセット
            mLastVibeSec = 0;
        } else {
            // 手をかざした
            AndroidUtil.vibrate(mContext, VIBRATE_TIME_SHORT);
        }
    }

    @Subscribe
    private void onAnimation(AnimationFrame.Bus data) {
        int durationSec = mCurrentProximity != null ? (int) (mClock.now() - mCurrentProximity.getDate().getTime()) : 0;
        if (durationSec > 0 && mLastVibeSec != durationSec && mCurrentProximity.isProximity()) {
            // フィードバック時刻になったので端末を振動
            AndroidUtil.vibrate(mContext, VIBRATE_TIME_LONG);
            mLastVibeSec = durationSec;

            // 画像をロードする
        }
    }

    Bitmap getIcon(CommandKey key) {
        Bitmap cache = mIcons.get(key);
        if (cache == null) {
            CommandData command = mProximityCommands.find(key);
            if (command == null) {
                return null;
            }

            cache = command.loadIcon();
            mIcons.put(key, cache);
        }
        return cache;
    }

    /**
     * UIのレンダリングを行う
     */
    @UiThread
    public void rendering(Graphics graphics) {
        if (mCurrentProximity == null || !mCurrentProximity.isProximity()) {
            // 近接状態に無いのでレンダリングしない
            return;
        }

        int DURATION_TIME_MS = ((int) (mClock.now() - mCurrentProximity.getDate().getTime()));
        int CURRENT_TIME_SEC = DURATION_TIME_MS / 1000;
        graphics.setColorARGB(0xFFFFFFFF);

        final double WINDOW_WIDTH = graphics.getWidth();
        final double WINDOW_HEIGHT = graphics.getHeight();

        final double WINDOW_CENTER_X = WINDOW_WIDTH / 2;
        final double WINDOW_CENTER_Y = WINDOW_HEIGHT / 2;

        final double ROUND_RADIUS = Math.min(WINDOW_WIDTH, WINDOW_HEIGHT) * 0.4;

        @ColorInt
        int backgroundColor = BACKGROUND_COLOR_TABLE[0];
        if (CURRENT_TIME_SEC < BACKGROUND_COLOR_TABLE.length) {
            backgroundColor = BACKGROUND_COLOR_TABLE[CURRENT_TIME_SEC];
        }

        // 中心円を表示する
        {
            // 待機時間
            final float CURRENT_WEIGHT = CURRENT_TIME_SEC > 0 ? 1.0f : (float) (DURATION_TIME_MS % 1000) / 1000.0f;

            // 中心を指定色で塗りつぶす
            graphics.setColorARGB(backgroundColor);
            graphics.fillRoundRect(
                    (int) (WINDOW_CENTER_X - CURRENT_WEIGHT * ROUND_RADIUS),
                    (int) (WINDOW_CENTER_Y - CURRENT_WEIGHT * ROUND_RADIUS),
                    (int) (CURRENT_WEIGHT * ROUND_RADIUS * 2), (int) (CURRENT_WEIGHT * ROUND_RADIUS * 2),
                    (float) (CURRENT_WEIGHT * ROUND_RADIUS * 0.1f));
        }

        // アイコンをレンダリングする
        if (CURRENT_TIME_SEC > 0) {
            Bitmap icon = getIcon(CommandKey.fromProximity(CURRENT_TIME_SEC));
            if (icon != null) {
                // アイコンのレンダリング
                graphics.setColorARGB(0xFFFFFFFF);
                final double ICON_RADIUS = ROUND_RADIUS * 0.75;
                graphics.drawBitmap(icon, (int) (WINDOW_CENTER_X - ICON_RADIUS), (int) (WINDOW_CENTER_Y - ICON_RADIUS), (int) (ICON_RADIUS * 2), (int) (ICON_RADIUS * 2));
            } else {
                graphics.setFontSize(new Font().calcFontSize("0", (int) (WINDOW_HEIGHT * 0.65)));
                graphics.setColorARGB(0xEF000000);
                graphics.drawString(String.valueOf(CURRENT_TIME_SEC), (int) WINDOW_CENTER_X, (int) WINDOW_CENTER_Y, -1, -1, Graphics.STRING_CENTER_X | Graphics.STRING_CENTER_Y);
            }
        }
    }
}
