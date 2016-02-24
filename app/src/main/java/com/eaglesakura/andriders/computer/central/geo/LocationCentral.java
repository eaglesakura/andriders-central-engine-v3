package com.eaglesakura.andriders.computer.central.geo;

import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.computer.central.base.BaseCentral;
import com.eaglesakura.andriders.computer.central.calculator.AltitudeDataCalculator;
import com.eaglesakura.andriders.computer.central.calculator.DistanceDataCalculator;
import com.eaglesakura.andriders.internal.protocol.GeoProtocol;
import com.eaglesakura.andriders.internal.protocol.ExtensionProtocol;
import com.eaglesakura.geo.Geohash;

/**
 * GPS座標を構築する
 */
public class LocationCentral extends BaseCentral {

    GeoProtocol.GeoPayload mGeoPayload = new GeoProtocol.GeoPayload();

    GeoProtocol.GeoPoint mLocation = new GeoProtocol.GeoPoint();

    String mLastReceivedGeohash;

    AltitudeDataCalculator mAltitudeDataCalculator;

    DistanceDataCalculator mDistanceDataCalculator;

    public LocationCentral() {
        mAltitudeDataCalculator = new AltitudeDataCalculator();
        mDistanceDataCalculator = new DistanceDataCalculator();
    }

    /**
     * 位置情報が信じられる値であればtrue
     */
    public boolean hasLocation() {
//        return (System.currentTimeMillis() - mGeoBuilder.getDateInt()) < CentralDataManager.DATA_TIMEOUT_MS;
        return mLastReceivedGeohash != null;
    }

    public void setAltitudeDataCalculator(AltitudeDataCalculator altitudeDataCalculator) {
        this.mAltitudeDataCalculator = altitudeDataCalculator;
    }

    public void setDistanceDataCalculator(DistanceDataCalculator distanceDataCalculator) {
        this.mDistanceDataCalculator = distanceDataCalculator;
    }

    public DistanceDataCalculator getDistanceDataCalculator() {
        return mDistanceDataCalculator;
    }

    /**
     * 位置情報を更新する
     */
    public void setLocation(ExtensionProtocol.SrcLocation loc) {
        // 高さを更新
        mAltitudeDataCalculator.onLocationUpdated(loc.latitude, loc.longitude, loc.altitude);
        mDistanceDataCalculator.updateLocation(loc.latitude, loc.longitude);

        // 位置を更新
        mLocation.latitude = loc.latitude;
        mLocation.longitude = loc.longitude;
        mLocation.altitude = mAltitudeDataCalculator.getCurrentAltitudeMeter();
        if (mAltitudeDataCalculator.hasAltitude()) {
            mGeoPayload.inclinationPercent = (float) mAltitudeDataCalculator.getInclinationPercent();
        } else {
            mGeoPayload.inclinationPercent = (float) loc.altitude;
        }
        mGeoPayload.locationAccuracy = (float) loc.accuracyMeter;

        mLastReceivedGeohash = Geohash.encode(loc.latitude, loc.longitude);
    }

    @Override
    public void onUpdate(CentralDataManager parent) {
        if (hasLocation()) {
            mGeoPayload.inclinationPercent = ((float) mAltitudeDataCalculator.getInclinationPercent());
            final float absInclination = Math.abs(mGeoPayload.inclinationPercent);
            if (absInclination < 4) {
                // ゆるい傾斜は平坦として扱う
                mGeoPayload.inclinationType = GeoProtocol.GeoPayload.INCLINATION_NONE;
            } else if (absInclination < 8) {
                // そこそこの坂はそこそこである。
                mGeoPayload.inclinationType = GeoProtocol.GeoPayload.INCLINATION_HILL;
            } else {
                // ある程度を超えた傾斜は激坂として扱う
                mGeoPayload.inclinationType = GeoProtocol.GeoPayload.INCLINATION_INTENSE_HILL;
            }
        } else {
            mGeoPayload.location = null;
            mGeoPayload.inclinationType = GeoProtocol.GeoPayload.INCLINATION_NONE;
        }
    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}
