package com.eaglesakura.andriders.model.profile;

import com.eaglesakura.andriders.system.context.config.FbProfile;

/**
 * ロードバイクのホイール周長を示す
 */
public class RoadbikeWheelLength {
    FbProfile.WheelConfig mRaw;

    public RoadbikeWheelLength(FbProfile.WheelConfig raw) {
        mRaw = raw;
    }

    /**
     * 表示タイトルを取得する
     */
    public String getTitle() {
        return mRaw.title;
    }

    /**
     * 外周長を取得する
     */
    public int getOuterLength() {
        return mRaw.length;
    }
}
