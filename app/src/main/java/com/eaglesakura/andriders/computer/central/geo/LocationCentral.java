package com.eaglesakura.andriders.computer.central.geo;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.computer.central.base.BaseCentral;
import com.eaglesakura.andriders.computer.central.data.geo.AltitudeData;
import com.eaglesakura.andriders.computer.central.data.geo.GeoSpeedData;
import com.eaglesakura.andriders.internal.protocol.ExtensionProtocol;
import com.eaglesakura.andriders.internal.protocol.RawCentralData;
import com.eaglesakura.andriders.internal.protocol.RawLocation;
import com.eaglesakura.andriders.sensor.InclinationType;
import com.eaglesakura.geo.Geohash;

/**
 * GPS座標を構築する
 */
public class LocationCentral extends BaseCentral {

    RawLocation mRaw = new RawLocation();

    String mLastReceivedGeohash;

    AltitudeData mAltitudeDataCalculator;

    GeoSpeedData mDistanceDataCalculator;

    public LocationCentral() {
        mAltitudeDataCalculator = new AltitudeData();
        mDistanceDataCalculator = new GeoSpeedData();
    }

    /**
     * 位置情報が信じられる値であればtrue
     */
    public boolean hasLocation() {
        return (System.currentTimeMillis() - mRaw.date) < CentralDataManager.DATA_TIMEOUT_MS;
    }

    public void setAltitudeDataCalculator(AltitudeData altitudeDataCalculator) {
        this.mAltitudeDataCalculator = altitudeDataCalculator;
    }

    public void setDistanceDataCalculator(GeoSpeedData distanceDataCalculator) {
        this.mDistanceDataCalculator = distanceDataCalculator;
    }

    public GeoSpeedData getDistanceDataCalculator() {
        return mDistanceDataCalculator;
    }

    /**
     * 位置情報を更新する
     */
    public void setLocation(ExtensionProtocol.SrcLocation loc) {
        // 高さを更新
        mAltitudeDataCalculator.setLocation(loc.latitude, loc.longitude, loc.altitude);
//        mDistanceDataCalculator.onUpdateLocation(loc.latitude, loc.longitude);

        // 位置を更新
        mRaw.latitude = loc.latitude;
        mRaw.longitude = loc.longitude;
        mRaw.date = System.currentTimeMillis();
        mRaw.altitude = mAltitudeDataCalculator.getCurrentAltitudeMeter();
        if (mAltitudeDataCalculator.hasAltitude()) {
            mRaw.inclinationPercent = (float) mAltitudeDataCalculator.getInclinationPercent();
        } else {
            mRaw.inclinationPercent = (float) loc.altitude;
        }
        mRaw.locationAccuracy = (float) loc.accuracyMeter;

        mLastReceivedGeohash = Geohash.encode(loc.latitude, loc.longitude);
    }

    @Override
    public void onUpdate(CentralDataManager parent, long diffTimeMs) {
        if (hasLocation()) {
            mRaw.inclinationPercent = ((float) mAltitudeDataCalculator.getInclinationPercent());
            final float absInclination = Math.abs(mRaw.inclinationPercent);
            if (absInclination < 4) {
                // ゆるい傾斜は平坦として扱う
                mRaw.inclinationType = InclinationType.None;
            } else if (absInclination < 8) {
                // そこそこの坂はそこそこである。
                mRaw.inclinationType = InclinationType.Hill;
            } else {
                // ある程度を超えた傾斜は激坂として扱う
                mRaw.inclinationType = InclinationType.IntenseHill;
            }
        } else {
            mRaw.date = 0;
            mRaw.inclinationType = InclinationType.None;
        }
    }

    /**
     * データ構築を行う
     *
     * @param parent 呼び出し元
     * @param result 構築先
     */
    @Override
    public void buildData(CentralDataManager parent, RawCentralData result) {
        if (hasLocation()) {
            result.sensor.location = AceUtils.publicFieldClone(mRaw);
            result.centralStatus.connectedFlags |= RawCentralData.RawCentralStatus.CONNECTED_FLAG_GPS;
        }
    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}
