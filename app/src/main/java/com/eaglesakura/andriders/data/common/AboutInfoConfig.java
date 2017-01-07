package com.eaglesakura.andriders.data.common;

import com.eaglesakura.andriders.system.context.config.FbAboutInfo;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.util.CollectionUtil;

/**
 * 開発者情報
 */
public class AboutInfoConfig {
    FbAboutInfo mRaw;

    public AboutInfoConfig(FbAboutInfo raw) {
        mRaw = raw;
    }

    /**
     * 開発者情報へのリンクを取得する
     */
    public DataCollection<Link> listDeveloperLinks() {
        return new DataCollection<>(CollectionUtil.asOtherList(mRaw.developer.link, it -> new Link(it)));
    }
}
