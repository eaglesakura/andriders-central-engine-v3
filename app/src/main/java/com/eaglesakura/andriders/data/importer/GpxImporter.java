package com.eaglesakura.andriders.data.importer;

import com.eaglesakura.andriders.central.data.CentralDataManager;
import com.eaglesakura.andriders.central.data.session.SessionInfo;
import com.eaglesakura.andriders.data.gpx.Gpx;
import com.eaglesakura.andriders.data.gpx.GpxParser;
import com.eaglesakura.andriders.data.gpx.GpxPoint;
import com.eaglesakura.andriders.data.gpx.GpxSegment;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.error.io.AppDataNotSupportedException;
import com.eaglesakura.andriders.error.io.AppIOException;
import com.eaglesakura.andriders.gen.prop.CentralServiceSettings;
import com.eaglesakura.andriders.serialize.RawGeoPoint;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.cerberus.error.TaskCanceledException;
import com.eaglesakura.io.CancelableInputStream;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.sloth.db.property.PropertyStore;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.eaglesakura.sloth.util.AppSupportUtil.assertNotCanceled;

/**
 * GPXファイルからimportを行う
 */
public class GpxImporter implements SessionImporter {
    @NonNull
    private final Context mContext;

    @Nullable
    private final Uri mFileUri;

    /**
     * Debug等で直接ファイルを指定できるようにする
     */
    @Nullable
    private final File mFile;

    /**
     * パーサは初期設定が必要なので、必要に応じて使用する
     */
    @NonNull
    private GpxParser mParser;

    GpxImporter(@NonNull Builder builder) {
        mContext = builder.mContext;
        mFileUri = builder.mUri;
        mFile = builder.mFile;
        mParser = builder.mParser;

        if (mContext == null || (mFileUri == null && mFile == null) || mParser == null) {
            throw new NullPointerException("Required data error");
        }
    }

    private InputStream openStream(CancelCallback cancelCallback) throws AppException {
        try {
            InputStream rawStream;
            if (mFile != null) {
                rawStream = new FileInputStream(mFile);
            } else {
                rawStream = mContext.getContentResolver().openInputStream(mFileUri);
            }
            return new CancelableInputStream(rawStream, cancelCallback);
        } catch (IOException e) {
            throw new AppIOException(e);
        }
    }

    /**
     * 新規にセッションを生成する
     */
    SessionInfo newSession(Clock clock) {
        return new SessionInfo.Builder(mContext, clock) {
            @Override
            protected CentralServiceSettings newCentralServiceSettings(PropertyStore store) {
                return new CentralServiceSettings(store) {
                    @Override
                    public boolean isGpsSpeedEnable() {
                        // GPS Importでは常にGPS速度である
                        return true;
                    }

                    @Override
                    public float getGpsAccuracy() {
                        // すべてを信頼する精度レベル
                        return 50.0f;
                    }
                };
            }
        }.build();
    }


    @Override
    public void install(@NonNull Listener listener, @Nullable CancelCallback cancelCallback) throws AppException, TaskCanceledException {
        Gpx gpx;

        try (InputStream is = openStream(cancelCallback)) {
            gpx = mParser.parse(is);

            if (gpx.getTrackSegments().isEmpty()) {
                // 取り込むべきセグメントが存在しない
                return;
            }
            assertNotCanceled(cancelCallback);
        } catch (XmlPullParserException e) {
            throw new AppDataNotSupportedException(e);
        } catch (IOException e) {
            throw new AppIOException(e);
        }

        // GPXからデータをエミュレートする
        for (GpxSegment segment : gpx.getTrackSegments()) {
            // セグメント数が一定未満の場合は短すぎるセッションなのでスキップする
            if (segment.getPoints().size() < 10) {
                return;
            }

            Clock clock = new Clock(segment.getFirstPoint().getTime().getTime());
            // Import用のセッションを生成する
            SessionInfo info = newSession(clock);
            CentralDataManager centralDataManager = new CentralDataManager(info, null, null);

            // import開始を通知
            listener.onSessionStart(this, centralDataManager);

            for (GpxPoint pt : segment.getPoints()) {
                assertNotCanceled(cancelCallback);

                clock.set(pt.getTime().getTime());


                RawGeoPoint location = pt.getLocation();
                if (location != null) {
                    // 位置を書き込む
                    centralDataManager.setLocation(location.latitude, location.longitude, location.altitude, 1.0);
                }

                if (centralDataManager.onUpdate()) {
                    // 更新を通知する
                    listener.onPointInsert(this, centralDataManager, centralDataManager.getLatestCentralData());
                }
            }

            // import終了を通知
            listener.onSessionFinished(this, centralDataManager);
        }

    }

    public static class Builder {
        @NonNull
        final Context mContext;

        @Nullable
        Uri mUri;

        File mFile;

        GpxParser mParser;

        public Builder(@NonNull Context context) {
            mContext = context;
        }

        public Builder parser(GpxParser.DateOption dateOption) {
            if (mParser == null) {
                mParser = new GpxParser();
            }
            mParser.setDateOption(dateOption);
            return this;
        }

        public Builder parser(GpxParser parser) {
            mParser = parser;
            return this;
        }

        public Builder file(@NonNull File file) {
            mFile = file;
            return this;
        }

        public Builder uri(@NonNull Uri uri) {
            mUri = uri;
            return this;
        }

        public GpxImporter build() {
            if (mParser == null) {
                parser(GpxParser.DateOption.AddTimeZone);
            }
            return new GpxImporter(this);
        }
    }
}
