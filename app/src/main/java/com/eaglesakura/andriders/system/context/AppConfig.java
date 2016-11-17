package com.eaglesakura.andriders.system.context;

import com.eaglesakura.andriders.model.profile.RoadbikeWheelLength;
import com.eaglesakura.andriders.system.context.config.AppConfigManager;
import com.eaglesakura.andriders.system.context.config.FbConfigRoot;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.andriders.v3.gen.config.AppStatusConfig;
import com.eaglesakura.android.error.NetworkNotConnectException;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.CollectionUtil;

import android.content.Context;

import java.util.List;

/**
 * 設定値情報
 */
public class AppConfig {
    AppConfigManager mConfigManager;

    AppStatusConfig mConfig;

    public AppConfig(Context context, AppConfigManager configManager) {
        mConfig = new AppStatusConfig();
        mConfigManager = configManager;
    }

    /**
     * Fetchを必要とする状態であればtrue
     */
    public boolean requireFetch() {
        return mConfigManager.get() == null;
    }

    FbConfigRoot getRaw() {
        if (requireFetch()) {
            throw new IllegalStateException("Fetch not completed");
        }
        return mConfigManager.get();
    }

    /**
     * すべてのホイール周長を取得する
     */
    public DataCollection<RoadbikeWheelLength> listWheelLength() {
        List<RoadbikeWheelLength> roadbikeWheelLengthList = CollectionUtil.asOtherList(getRaw().profile.wheel, it -> new RoadbikeWheelLength(it));
        return new DataCollection<>(roadbikeWheelLengthList);
    }

    /**
     * データ同期を行う
     */
    public int fetch(CancelCallback cancelCallback) throws TaskCanceledException, NetworkNotConnectException {
        AppLog.system("CurrentConfigPath[%s]", mConfig.getDatabasePathConfig());
        return mConfigManager.fetch(cancelCallback);
    }
}
