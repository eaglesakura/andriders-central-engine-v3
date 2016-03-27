package com.eaglesakura.andriders.db.importer;

import com.eaglesakura.andriders.central.CentralDataManager;
import com.eaglesakura.andriders.data.gpx.Gpx;
import com.eaglesakura.andriders.data.gpx.GpxParser;
import com.eaglesakura.andriders.data.gpx.GpxPoint;
import com.eaglesakura.andriders.data.gpx.GpxSegment;
import com.eaglesakura.andriders.util.CancelSignal;
import com.eaglesakura.andriders.util.Clock;
import com.eaglesakura.andriders.util.ClockTimer;
import com.eaglesakura.util.IOUtil;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public int install(CancelSignal cancelSignal) throws XmlPullParserException, IOException {
        Gpx gpx;
        InputStream is;
        if (mGpxUri != null) {
            is = mContext.getContentResolver().openInputStream(mGpxUri);
        } else if (mGpxFile != null) {
            is = new FileInputStream(mGpxFile);
        } else {
            throw new IllegalStateException();
        }

        try {
            gpx = mParser.parse(is);
        } finally {
            IOUtil.close(is);
        }

        if (gpx.getTrackSegments().isEmpty()) {
            return 0;
        }

        // GPXからデータをエミュレートする
        int segmentIndex = 0;
        for (GpxSegment segment : gpx.getTrackSegments()) {
            Clock clock = new Clock(segment.getFirstPoint().getTime().getTime());
            ClockTimer clockTimer = new ClockTimer(clock);
            CentralDataManager centralDataManager = new CentralDataManager(mContext, clock);

            for (GpxPoint pt : segment.getPoints()) {
                clock.set(pt.getTime().getTime());

                centralDataManager.setGpxPoint(pt);

                if (!centralDataManager.onUpdate()) {
                    continue;
                }

                if (clockTimer.overTimeMs(1000 * 30)) {
                    centralDataManager.commit();
                    clockTimer.start();
                }
            }

            // 最後のデータを書き込む
            centralDataManager.commit();
            ++segmentIndex;

            if (cancelSignal != null && cancelSignal.isCanceled()) {
                return -1;
            }
        }

        // インストール範囲を指定する
        mImportStartDate = gpx.getFirstSegment().getFirstPoint().getTime();
        mImportEndDate = gpx.getLastSegment().getLastPoint().getTime();

        return segmentIndex;
    }
}
