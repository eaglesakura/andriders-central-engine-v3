package com.eaglesakura.andriders.data.importer;

import com.eaglesakura.andriders.central.CentralDataManager;
import com.eaglesakura.andriders.central.session.SessionInfo;
import com.eaglesakura.andriders.data.gpx.Gpx;
import com.eaglesakura.andriders.data.gpx.GpxParser;
import com.eaglesakura.andriders.data.gpx.GpxPoint;
import com.eaglesakura.andriders.data.gpx.GpxSegment;
import com.eaglesakura.andriders.data.AppSettings;
import com.eaglesakura.andriders.error.io.AppDataNotFoundException;
import com.eaglesakura.andriders.error.io.AppIOException;
import com.eaglesakura.andriders.provider.AppContextProvider;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.io.CancelableInputStream;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Date;

/**
 *
 */
public class GpxImporter {

    @NonNull
    final Context mContext;

    @Nullable
    final Uri mGpxUri;

    @Nullable
    final File mGpxFile;

    @NonNull
    final GpxParser mParser = new GpxParser();

    @Nullable
    Date mImportStartDate;

    @Nullable
    Date mImportEndDate;

    @Inject(AppContextProvider.class)
    AppSettings mAppSettings;

    public GpxImporter(@NonNull Context context, @NonNull Uri gpxFile) {
        mContext = context;
        mGpxUri = gpxFile;
        mGpxFile = null;
    }

    public GpxImporter(@NonNull Context context, @NonNull File gpxFile) {
        mContext = context;
        mGpxUri = null;
        mGpxFile = gpxFile;
    }

    @NonNull
    public GpxParser getParser() {
        return mParser;
    }

    @Nullable
    public Date getImportStartDate() {
        return mImportStartDate;
    }

    @Nullable
    public Date getImportEndDate() {
        return mImportEndDate;
    }

    private InputStream openStream(CancelCallback cancelCallback) throws IOException {
        if (mGpxUri != null) {
            return new CancelableInputStream(mContext.getContentResolver().openInputStream(mGpxUri), cancelCallback);
        } else if (mGpxFile != null) {
            return new CancelableInputStream(new FileInputStream(mGpxFile), cancelCallback);
        } else {
            throw new IOException();
        }
    }

    /**
     * インストールを行う
     *
     * @return 読み込んだセグメント数
     */
    public int install(CancelCallback cancelCallback) throws AppIOException {
        Gpx gpx;

        try (InputStream is = openStream(cancelCallback)) {
            gpx = mParser.parse(is);

            if (gpx.getTrackSegments().isEmpty()) {
                return 0;
            }

            // GPXからデータをエミュレートする
            int segmentIndex = 0;
            for (GpxSegment segment : gpx.getTrackSegments()) {
                Clock clock = new Clock(segment.getFirstPoint().getTime().getTime());
                ClockTimer clockTimer = new ClockTimer(clock);

                SessionInfo info = new SessionInfo.Builder(mContext, clock)
                        .profile(mAppSettings.dumpCentralSettings())
                        .build();

                CentralDataManager centralDataManager = new CentralDataManager(info);

                for (GpxPoint pt : segment.getPoints()) {
                    clock.set(pt.getTime().getTime());

                    centralDataManager.setGpxPoint(pt);

                    if (!centralDataManager.onUpdate()) {
                        continue;
                    }

                    // 適当な内部時間が経過したら、ログをコミットして確定させる
                    if (clockTimer.overTimeMs(1000 * 30)) {
                        // FIXME DBへの保存を行わなければならない
                        clockTimer.start();
                        throw new Error("Not Impl");
                    }
                }

                ++segmentIndex;

                if (CallbackUtils.isCanceled(cancelCallback)) {
                    throw new InterruptedIOException("Import Canceled");
                }

                // 最後のデータを書き込む
                // FIXME DBへの保存を行わなければならない
                throw new Error("Not Impl");
            }

            // インストール範囲を指定する
            mImportStartDate = gpx.getFirstSegment().getFirstPoint().getTime();
            mImportEndDate = gpx.getLastSegment().getLastPoint().getTime();

            return segmentIndex;
        } catch (IOException e) {
            throw new AppIOException(e);
        } catch (XmlPullParserException e) {
            throw new AppDataNotFoundException(e);
        }
    }
}
