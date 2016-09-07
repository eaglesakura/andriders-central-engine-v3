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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

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
                return FrameworkCentral.getApplication().getString(R.string.Common_Formatter_Time_S, sessionTimeSec);
            } else {
                // 分秒
                return FrameworkCentral.getApplication().getString(R.string.Common_Formatter_Time_MS, sessionTimeMinute, sessionTimeSec);
            }
        } else {
            // 時分
            return FrameworkCentral.getApplication().getString(R.string.Common_Formatter_Time_HM, sessionTimeHour, sessionTimeMinute);
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


//    public static LatLng toLatLng(RawGeoPoint geo) {
//        if (geo == null) {
//            return null;
//        }
//        return new LatLng(geo.latitude, geo.longitude);
//    }
//

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
}
