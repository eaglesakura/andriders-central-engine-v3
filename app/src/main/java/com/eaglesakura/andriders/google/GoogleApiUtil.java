package com.eaglesakura.andriders.google;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.gms.util.PlayServiceUtil;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.DateUtil;
import com.eaglesakura.util.Util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Google系APIの便利メソッドをまとめておく
 */
public class GoogleApiUtil {

    /**
     * Google Fitから体重データを取得する
     * <br>
     * 取得できなかったら負の値を返す
     */
    public static float getUserWeightFromFit(GoogleApiClient client, CancelCallback cancelCallback) throws TaskCanceledException {
        if (AndroidThreadUtil.isUIThread()) {
            throw new IllegalStateException("call uithread");
        }

        DataReadRequest request = new DataReadRequest.Builder()
                .setTimeRange(DateUtil.getDateStart(new Date(), AppUtil.DEFAULT_TIMEZONE).getTime() - (1000 * 3600 * 24 * 7), System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .setLimit(1)
                .read(DataType.TYPE_WEIGHT)
                .build();
        DataReadResult readResult = PlayServiceUtil.await(Fitness.HistoryApi.readData(client, request), cancelCallback);
        DataSet dataSet = readResult.getDataSet(DataType.TYPE_WEIGHT);

        try {
            Value value = dataSet.getDataPoints().get(0).getValue(DataType.TYPE_WEIGHT.getFields().get(0));
            float resultValue = value.asFloat();
            if (resultValue <= 0) {
                return -1;
            } else {
                return resultValue;
            }
        } catch (Exception e) {
            return -1;
        }
    }
}
