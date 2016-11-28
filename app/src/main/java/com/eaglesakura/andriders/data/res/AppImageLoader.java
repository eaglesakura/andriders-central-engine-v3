package com.eaglesakura.andriders.data.res;

import com.eaglesakura.android.graphics.CachedImageLoader;
import com.eaglesakura.android.net.NetworkConnector;
import com.eaglesakura.android.net.request.ConnectRequest;
import com.eaglesakura.android.net.request.SimpleHttpRequest;
import com.eaglesakura.util.Timer;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/**
 * アプリ用ImageLoader
 */
public class AppImageLoader extends CachedImageLoader {

    final private NetworkConnector mNetworkConnector;

    public AppImageLoader(Context context) {
        this(context, 5, 5);
    }

    public AppImageLoader(Context context, @IntRange(from = 1) int imageCacheNum, @IntRange(from = 1) int errorCacheNum) {
        super(context, imageCacheNum, errorCacheNum);
        mNetworkConnector = new NetworkConnector(context);
    }

    public Builder newImage(Uri uri, boolean onMemoryCache) {
        if (uri.toString().startsWith("http")) {
            // HTTP経由
            SimpleHttpRequest request = new SimpleHttpRequest(ConnectRequest.Method.GET);
            request.getCachePolicy().setCacheLimitTimeMs(Timer.toMilliSec(1, 0, 0, 0, 0));
            request.getCachePolicy().setMaxItemBytes(1024 * 512);
            request.setUrl(uri.toString(), null);
            return super.newImage(mNetworkConnector, request, onMemoryCache);
        } else {
            // OnMemory経由
            return super.newImage(uri, onMemoryCache);
        }
    }

    public interface Holder {
        @NonNull
        AppImageLoader getImageLoader();
    }
}
