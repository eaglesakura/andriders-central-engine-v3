package com.eaglesakura.andriders.model.profile;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.system.context.config.FbProfile;

import android.content.Context;

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

    public String getDisplayTitle(Context context) {
        return context.getString(R.string.Word_Profile_WheelOuterLengthFormat, getTitle(), getOuterLength());
    }

    /**
     * 外周長を取得する
     */
    public int getOuterLength() {
        return mRaw.length;
    }
}
