package com.eaglesakura.andriders.google;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.data.CentralLogManager;
import com.eaglesakura.andriders.central.data.log.LogStatistics;
import com.eaglesakura.andriders.central.data.log.SessionHeader;
import com.eaglesakura.andriders.central.data.log.SessionHeaderCollection;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.error.io.AppIOException;
import com.eaglesakura.andriders.provider.AppManagerProvider;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawSensorData;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.util.AppUtil;
import com.eaglesakura.android.garnet.BuildConfig;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.gms.client.PlayServiceConnection;
import com.eaglesakura.android.gms.util.PlayServiceUtil;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.lambda.Action1;
import com.eaglesakura.lambda.CancelCallback;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * GoogleFitへのアップロード管理を行う
 */
public class GoogleFitUploader {

    final Context mContext;

    /**
     * アップロード対象のセッションID
     */
    final long mSessionId;

    @Inject(AppManagerProvider.class)
    CentralLogManager mCentralLogManager;

    GoogleFitUploader(Builder builder) {
        mContext = builder.mContext;
        mSessionId = builder.mSessionId;
    }

    String getApplicationPackageName() {
        return mContext.getPackageName();
    }

    /**
     * セッションを含む1日分のログをアップロードする
     *
     * @param cancelCallback キャンセルチェック
     * @return アップロードしたセッション数
     */
    public int uploadDaily(UploadCallback callback, CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        try (PlayServiceConnection connection = PlayServiceConnection.newInstance(AppUtil.newFullPermissionClient(mContext), cancelCallback)) {

            // 統計情報をアップロードする
            SessionHeaderCollection sessionHeaderCollection = mCentralLogManager.listDailyHeaders(mSessionId, cancelCallback);
            for (SessionHeader header : sessionHeaderCollection.list()) {
                callback.onUploadStart(this, header);
                // セッションごとにアップロードする
                LogStatistics logStatistics = mCentralLogManager.loadSessionStatistics(header, cancelCallback);

                UploadConnection uploadConnection = new UploadConnection(connection);
                mCentralLogManager.eachDailySessionPoints(mSessionId, uploadConnection.newAction(), cancelCallback);

                // セッション統計をアップロードする
                uploadConnection.commit(logStatistics, cancelCallback);

                callback.onUploadCompleted(this, header);
            }

            return sessionHeaderCollection.size();
        } catch (IOException e) {
            throw new AppIOException(e);
        }
    }

    /**
     * 1アップロード用のコネクション
     */
    class UploadConnection {
        @NonNull
        DataSource heartateDataSource;

        @NonNull
        DataSet heartrateDataSet;

        @NonNull
        DataSource cadenceDataSource;

        @NonNull
        DataSet cadenceDataSet;

        @NonNull
        DataSource wheelRpmDataSource;

        @NonNull
        DataSet wheelRpmDataSet;

        @NonNull
        PlayServiceConnection mConnection;

        UploadConnection(PlayServiceConnection connection) {
            mConnection = connection;
            initializeDataSet();
        }

        private void initializeDataSet() {
            {
                DataSource.Builder builder = new DataSource.Builder();
                builder.setDataType(DataType.TYPE_HEART_RATE_BPM);
                builder.setAppPackageName(getApplicationPackageName());
                builder.setName(mContext.getString(R.string.Word_Fit_HeartrateName));
                builder.setType(DataSource.TYPE_RAW);

                heartateDataSource = builder.build();
                heartrateDataSet = DataSet.create(heartateDataSource);
            }
            {
                DataSource.Builder builder = new DataSource.Builder();
                builder.setDataType(DataType.TYPE_CYCLING_PEDALING_CADENCE);
                builder.setAppPackageName(getApplicationPackageName());
                builder.setName(mContext.getString(R.string.Word_Fit_CadenceName));
                builder.setType(DataSource.TYPE_DERIVED);

                cadenceDataSource = builder.build();
                cadenceDataSet = DataSet.create(cadenceDataSource);
            }
            {
                DataSource.Builder builder = new DataSource.Builder();
                builder.setDataType(DataType.TYPE_CYCLING_WHEEL_RPM);
                builder.setAppPackageName(getApplicationPackageName());
                builder.setName(mContext.getString(R.string.Word_Fit_WheelRpmName));
                builder.setType(DataSource.TYPE_DERIVED);

                wheelRpmDataSource = builder.build();
                wheelRpmDataSet = DataSet.create(wheelRpmDataSource);
            }
        }

        /**
         * セッションデータの挿入を行う
         */
        public Action1<RawCentralData> newAction() {
            return raw -> {
                long timestamp = raw.centralStatus.date;
                RawSensorData.RawHeartrate heartrate = raw.sensor.heartrate;
                RawSensorData.RawCadence cadence = raw.sensor.cadence;
                RawSensorData.RawSpeed speed = raw.sensor.speed;

                if (heartrate != null) {
                    DataPoint dataPoint = DataPoint.create(heartateDataSource);
                    dataPoint.getValue(Field.FIELD_BPM).setFloat(heartrate.bpm);
                    dataPoint.setTimestamp(timestamp, TimeUnit.MILLISECONDS);

                    heartrateDataSet.add(dataPoint);
                }

                if (cadence != null) {
                    DataPoint dataPoint = DataPoint.create(cadenceDataSource);
                    dataPoint.getValue(Field.FIELD_RPM).setFloat(cadence.rpm);
                    dataPoint.setTimestamp(timestamp, TimeUnit.MILLISECONDS);

                    cadenceDataSet.add(dataPoint);
                }

                if (speed != null) {
                    if (speed.hasWheelRpm()) {
                        DataPoint dataPoint = DataPoint.create(wheelRpmDataSource);
                        dataPoint.getValue(Field.FIELD_RPM).setFloat(speed.wheelRpm);
                        dataPoint.setTimestamp(timestamp, TimeUnit.MILLISECONDS);

                        wheelRpmDataSet.add(dataPoint);
                    }
                }
            };
        }

        /**
         * 各打刻点と統計情報をアップロードする
         */
        public void commit(LogStatistics logStatistics, CancelCallback cancelCallback) throws TaskCanceledException, AppException {
            // 全データをコミット
            commit(heartrateDataSet, "Heartrate", cancelCallback);
            commit(cadenceDataSet, "Cadence", cancelCallback);
            commit(wheelRpmDataSet, "WheelRpm", cancelCallback);

            // セッション情報を生成
            {
                Session.Builder fitSession = new Session.Builder();
                fitSession.setStartTime(logStatistics.getStartDate().getTime(), TimeUnit.MILLISECONDS);
                fitSession.setEndTime(logStatistics.getEndDate().getTime(), TimeUnit.MILLISECONDS);
                fitSession.setName(mContext.getString(R.string.Word_Fit_SessionName));
                fitSession.setActivity(FitnessActivities.BIKING);
                fitSession.setIdentifier(mContext.getString(R.string.Word_Fit_SessionIdentifier, mSessionId));
                fitSession.setDescription(mContext.getString(R.string.Word_Fit_SessionDescription, BuildConfig.VERSION_NAME));

                SessionInsertRequest.Builder sessionRequest = new SessionInsertRequest.Builder();
                sessionRequest.setSession(fitSession.build());


                Status status = PlayServiceUtil.await(Fitness.SessionsApi.insertSession(mConnection.getClient(), sessionRequest.build()), cancelCallback);
                if (!status.isSuccess()) {
                    throw new AppIOException("Session upload failed.");
                }
            }

            // カロリー消費が存在する
            if (logStatistics.getCalories() > 1) {
                final float calories = logStatistics.getCalories();

                DataSource.Builder builder = new DataSource.Builder();
                builder.setDataType(DataType.TYPE_CALORIES_EXPENDED);
                builder.setAppPackageName(getApplicationPackageName());
                builder.setName(mContext.getString(R.string.Word_Fit_CaloriesName));
                builder.setType(DataSource.TYPE_RAW);

                DataSource calorieDataSource = builder.build();
                DataSet calorieDataSet = DataSet.create(calorieDataSource);

                DataPoint dp = DataPoint.create(calorieDataSource);
                dp.getValue(Field.FIELD_CALORIES).setFloat(calories);
                dp.setTimeInterval(
                        logStatistics.getStartDate().getTime(), logStatistics.getEndDate().getTime(),
                        TimeUnit.MILLISECONDS
                );
                calorieDataSet.add(dp);

                // commit
                commit(calorieDataSet, "Calories", cancelCallback);
            }
        }

        private void commit(DataSet dataSet, String type, CancelCallback cancelCallback) throws TaskCanceledException, AppException {
            if (dataSet.getDataPoints().isEmpty()) {
                return;
            }
            // 全データをコミット
            Status status = PlayServiceUtil.await(Fitness.HistoryApi.insertData(mConnection.getClient(), dataSet), cancelCallback);
            if (status.isSuccess()) {
                AppLog.system("Success HistoryApi.insertData(%s) Points(%d)", type, dataSet.getDataPoints().size());
            } else {
                throw new AppIOException(type + " Upload filed");
            }
        }
    }

    public static class Builder {
        Context mContext;

        long mSessionId;

        public static Builder from(Context context) {
            Builder builder = new Builder();
            builder.mContext = context;
            return builder;
        }

        public Builder session(long sessionId) {
            mSessionId = sessionId;
            return this;
        }

        public GoogleFitUploader build() {
            GoogleFitUploader result = new GoogleFitUploader(this);
            Garnet.create(result)
                    .depend(Context.class, mContext)
                    .inject();
            return result;
        }
    }

    public interface UploadCallback {
        /**
         * セッションのアップロードを開始した
         */
        void onUploadStart(GoogleFitUploader self, SessionHeader session);

        /**
         * セッションのアップロードが完了した
         */
        void onUploadCompleted(GoogleFitUploader self, SessionHeader session);
    }
}
