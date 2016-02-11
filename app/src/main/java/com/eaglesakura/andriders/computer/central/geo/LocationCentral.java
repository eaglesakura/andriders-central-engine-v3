package com.eaglesakura.andriders.computer.central.geo;

import com.eaglesakura.andriders.computer.central.CentralDataManager;
import com.eaglesakura.andriders.computer.central.base.BaseCentral;
import com.eaglesakura.andriders.computer.central.calculator.AltitudeDataCalculator;
import com.eaglesakura.andriders.computer.central.calculator.DistanceDataCalculator;
import com.eaglesakura.andriders.idl.remote.IdlLocation;
import com.eaglesakura.andriders.protocol.GeoProtocol;
import com.eaglesakura.geo.Geohash;

/**
 * GPS座標を構築する
 */
public class LocationCentral extends BaseCentral {

    GeoProtocol.GeoPayload.Builder mGeoBuilder = GeoProtocol.GeoPayload.newBuilder();

    GeoProtocol.GeoPoint.Builder mLocation = GeoProtocol.GeoPoint.newBuilder();

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
    public void setLocation(IdlLocation loc) {
        // 高さを更新
        mAltitudeDataCalculator.onLocationUpdated(loc.getLatitude(), loc.getLongitude(), loc.getAltitude());
        mDistanceDataCalculator.updateLocation(loc.getLatitude(), loc.getLongitude());

        // 位置を更新
        mLocation.setLatitude(loc.getLatitude());
        mLocation.setLongitude(loc.getLongitude());
        mLocation.setAltitude(mAltitudeDataCalculator.getCurrentAltitudeMeter());
        if (mAltitudeDataCalculator.hasAltitude()) {
            mGeoBuilder.setInclinationPercent((float) mAltitudeDataCalculator.getInclinationPercent());
        } else {
            mGeoBuilder.setInclinationPercent((float) loc.getAltitude());
        }
        mGeoBuilder.setLocationAccuracy((float) loc.getAccuracyMeter());

        mLastReceivedGeohash = Geohash.encode(loc.getLatitude(), loc.getLongitude());
    }

    @Override
    public void onUpdate(CentralDataManager parent) {
        if (hasLocation()) {
            mGeoBuilder.setInclinationPercent((float) mAltitudeDataCalculator.getInclinationPercent());
            final float absInclination = Math.abs(mGeoBuilder.getInclinationPercent());
            if (absInclination < 4) {
                // ゆるい傾斜は平坦として扱う
                mGeoBuilder.setInclinationType(GeoProtocol.InclinationType.None);
            } else if (absInclination < 8) {
                // そこそこの坂はそこそこである。
                mGeoBuilder.setInclinationType(GeoProtocol.InclinationType.Hill);
            } else {
                // ある程度を超えた傾斜は激坂として扱う
                mGeoBuilder.setInclinationType(GeoProtocol.InclinationType.IntenseHill);
            }
        } else {
            mGeoBuilder.clearLocation();
            mGeoBuilder.setInclinationType(GeoProtocol.InclinationType.None);
        }
    }

    @Override
    public boolean isDelete(CentralDataManager parent) {
        return false;
    }
}
