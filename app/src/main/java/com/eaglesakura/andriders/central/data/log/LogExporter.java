package com.eaglesakura.andriders.central.data.log;

import com.eaglesakura.andriders.data.db.SessionLogDatabase;
import com.eaglesakura.andriders.error.AppException;
import com.eaglesakura.andriders.provider.AppDatabaseProvider;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.garnet.Inject;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.lambda.CancelCallback;

import android.content.Context;
import android.net.Uri;

import java.io.File;

/**
 * ログの書き出しを行なう
 */
public class LogExporter {

    Context mContext;

    /**
     * 書込み先URI
     */
    Uri mDstUri;

    /**
     * 出力対象のセッションID
     */
    long mSessionId;

    @Inject(value = AppDatabaseProvider.class, name = AppDatabaseProvider.NAME_WRITEABLE)
    SessionLogDatabase mLogDatabase;

    LogExporter(Builder builder) {
        mContext = builder.mContext;
        mDstUri = builder.mUri;
        mSessionId = builder.mSessionId;
    }

    /**
     * 出力を行なう
     *
     * @param cancelCallback キャンセルコールバック
     */
    public void export(CancelCallback cancelCallback) throws AppException, TaskCanceledException {

    }

    public static class Builder {
        Context mContext;

        Uri mUri;

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

        public Builder session(SessionHeader session) {
            mSessionId = session.getSessionId();
            return this;
        }

        public Builder writeTo(Uri uri) {
            mUri = uri;
            return this;
        }

        public Builder whiteTo(File file) {
            return writeTo(Uri.fromFile(file));
        }

        public LogExporter build() {
            LogExporter logExporter = new LogExporter(this);
            Garnet.create(logExporter)
                    .depend(Context.class, mContext)
                    .inject();
            return logExporter;
        }
    }
}
