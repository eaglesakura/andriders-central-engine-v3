package com.eaglesakura.andriders.util;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.location.LocationServices;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.andriders.command.CommandSetting;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.android.util.ImageUtil;
import com.eaglesakura.io.CancelableInputStream;
import com.eaglesakura.lambda.CancelCallback;

import org.greenrobot.greendao.annotation.NotNull;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

/**
 *
 */
public class AppUtil {

    /**
     * ホイールの長さ設定
     */
    public static final int WHEEL_LENGTH_MAX = 2350;

    /**
     * ミリ秒単位の時間を適当なフォーマットにかけて文字列として返す
     *
     * @param milliSeconds 時刻のミリ秒
     */
    public static String formatTimeMilliSecToString(long milliSeconds) {
        int sessionTimeHour = (int) (milliSeconds / 1000 / 60 / 60);
        int sessionTimeMinute = (int) (milliSeconds / 1000 / 60) % 60;
        int sessionTimeSec = (int) (milliSeconds / 1000) % 60;
        if (sessionTimeHour == 0) {
            if (sessionTimeMinute == 0) {
                // 秒だけ
                return FrameworkCentral.getApplication().getString(R.string.Word_Common_TimeS, sessionTimeSec);
            } else {
                // 分秒
                return FrameworkCentral.getApplication().getString(R.string.Word_Common_TimeMS, sessionTimeMinute, sessionTimeSec);
            }
        } else {
            // 時分
            return FrameworkCentral.getApplication().getString(R.string.Word_Common_TimeHM, sessionTimeHour, sessionTimeMinute);
        }
    }


    /**
     * ホイール回転数と外周長から時速を算出する
     */
    public static double calcSpeedKmPerHour(double wheelRpm, double wheelOuterLength) {
        // 現在の1分間回転数から毎時間回転数に変換
        final double currentRpHour = wheelRpm * 60;
        // ホイールの外周mm/hに変換
        double moveLength = currentRpHour * wheelOuterLength;
        // mm => m => km
        moveLength /= (1000.0 * 1000.0);
        return moveLength;
    }


    /**
     * コマンド設定を行うためのIntentを投げる
     *
     * @param commandKey コマンド識別キー
     */
    public static Intent newCommandSettingIntent(@NonNull Context context, @NonNull CommandKey commandKey) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("acecommand://"));
        intent.putExtra(CommandSetting.EXTRA_COMMAND_KEY, commandKey);
        return Intent.createChooser(intent, null);
    }

    public static GoogleApiClient.Builder newSignInClient(Context context) {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        return new GoogleApiClient.Builder(context)
                // Firebase Auth
                .addApi(Auth.GOOGLE_SIGN_IN_API, options)
                ;
    }

    public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();

    public static GoogleApiClient.Builder newFullPermissionClient(Context context) {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        return new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, options)
                // Google Fit
                .addApiIfAvailable(Fitness.HISTORY_API, Fitness.SCOPE_ACTIVITY_READ_WRITE, Fitness.SCOPE_BODY_READ_WRITE, Fitness.SCOPE_LOCATION_READ_WRITE)
                .addApiIfAvailable(Fitness.BLE_API)
                .addApiIfAvailable(Fitness.SESSIONS_API)
                // GPS
                .addApiIfAvailable(LocationServices.API)
                ;
//        return new GoogleApiClient.Builder(context)
//                // Google Fit
//                .addApi(Fitness.HISTORY_API)
//                .addApi(Fitness.BLE_API)
//                .addApi(Fitness.SESSIONS_API)
//                .addScope(Fitness.SCOPE_ACTIVITY_READ_WRITE)
//                .addScope(Fitness.SCOPE_BODY_READ_WRITE)
//                .addScope(Fitness.SCOPE_LOCATION_READ_WRITE)
//                // GPS
//                .addApi(LocationServices.API)
//                ;
    }

    public static String getErrorTitle(Throwable e) {
        Context context = FrameworkCentral.getApplication();
        return context.getString(R.string.Title_Error_Runtime);
    }

    public static String getErrorMessage(Throwable e) {
        Context context = FrameworkCentral.getApplication();
        return context.getString(R.string.Message_Error_Runtime);
    }

    /**
     * アイコンをロードする
     *
     * @param uri アイコンURI
     */
    @NonNull
    public static Bitmap loadIcon(@NonNull Context context, @NotNull Uri uri, CancelCallback cancelCallback) throws TaskCanceledException {
        try (InputStream is = new CancelableInputStream(context.getContentResolver().openInputStream(uri), cancelCallback)) {
            Bitmap image = ImageUtil.decode(is);
            Bitmap scaled = ImageUtil.toScaledImage(image, 256, 256);
            if (image != scaled) {
                image.recycle();
            }
            return scaled;
        } catch (IOException e) {
            return ImageUtil.decode(context, R.mipmap.ic_user_position);
        }
    }
}
