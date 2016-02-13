package com.eaglesakura.andriders.extension;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.eaglesakura.andriders.AceUtils;
import com.eaglesakura.andriders.db.Settings;
import com.eaglesakura.andriders.extension.data.CentralDataExtension;
import com.eaglesakura.andriders.extension.display.DisplayData;
import com.eaglesakura.andriders.extension.display.DisplayExtension;
import com.eaglesakura.andriders.extension.display.LineValue;
import com.eaglesakura.andriders.extension.setting.PermissionRequestActivity;
import com.eaglesakura.andriders.service.base.AppBaseService;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.geo.Geohash;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 現在位置を配信するExtension
 */
public class LocationExtensionService extends AppBaseService implements IExtensionService {
    GoogleApiClient mLocationApiClient;

    CentralDataExtension mCentralDataManager;

    DisplayExtension mDisplayExtension;

    DisplayData mDebugLocation;

    DisplayData mDebugGeohash;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.log("onBind(%s)", toString());
        ExtensionSession session = ExtensionSession.onBind(this, intent);
        if (session == null) {
            return null;
        }

        return session.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.log("onUnbind(%s)", toString());
        ExtensionSession.onUnbind(this, intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        ExtensionInformation info = new ExtensionInformation(this, "gps_loc");
        info.setSummary("現在位置をサイクルコンピュータに反映します。");
        info.setCategory(ExtensionCategory.CATEGORY_LOCATION);
        info.setHasSetting(false);
        return info;
    }

    @Override
    public List<DisplayInformation> getDisplayInformation() {

        List<DisplayInformation> result = new ArrayList<>();

        if (Settings.isDebugable()) {
            // 位置情報をデバッグ表示
            {
                DisplayInformation info = new DisplayInformation(this, "debug.loc");
                info.setTitle("DBG:GPS座標");
                result.add(info);
            }
            // 位置情報をデバッグ表示
            {
                DisplayInformation info = new DisplayInformation(this, "debug.geohash");
                info.setTitle("DBG:ジオハッシュ");
                result.add(info);
            }
        }

        if (Util.isEmpty(result)) {
            return null;
        } else {
            return result;
        }
    }

    private boolean isRuntimePermissionGranted() {
        return PermissionUtil.isRuntimePermissionGranted(this, PermissionUtil.PermissionType.SelfLocation);
    }

    @Override
    public void onAceServiceConnected(ExtensionSession session) {
        if (!isRuntimePermissionGranted()) {
            // 許可されていないので、このServiceは何もしない
            return;
        }

        mDebugGeohash = new DisplayData(this, "debug.geohash");
        mDebugGeohash.setTimeoutMs(1000 * 30);
        mDebugGeohash.setValue(new LineValue(2)); // hash, time

        mDebugLocation = new DisplayData(this, "debug.geohash");
        mDebugLocation.setTimeoutMs(1000 * 30);
        mDebugLocation.setValue(new LineValue(3)); // lat, lng, time

        mCentralDataManager = session.getCentralDataExtension();
        mDisplayExtension = session.getDisplayExtension();

        mLocationApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        LogUtil.log("location client :: connected");
                        try {
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    mLocationApiClient,
                                    createLocationRequest(),
                                    mLocationListenerImpl
                            );
                        } catch (SecurityException e) {
                            // failed connect...
                            LogUtil.log(e);
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();
        mLocationApiClient.connect();
    }

    @Override
    public void onAceServiceDisconnected(ExtensionSession session) {
        if (mLocationApiClient == null) {
            return;
        }

        mCentralDataManager = null;
        mLocationApiClient.disconnect();
    }

    @Override
    public void onEnable(ExtensionSession session) {
        if (!isRuntimePermissionGranted()) {
            toast("Request GPS Permission!!");
            Intent intent = PermissionRequestActivity.createIntent(LocationExtensionService.this,
                    new PermissionUtil.PermissionType[]{PermissionUtil.PermissionType.SelfLocation});
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onDisable(ExtensionSession session) {

    }

    @Override
    public void startSetting(ExtensionSession session) {

    }

    /**
     * 更新リクエストを生成する
     */
    LocationRequest createLocationRequest() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setSmallestDisplacement(1);
        request.setFastestInterval(500);
        request.setInterval(500);
        return request;
    }

    /**
     * 位置更新チェック
     */
    final LocationListener mLocationListenerImpl = new LocationListener() {
        @Override
        public void onLocationChanged(Location newLocation) {
            if (mCentralDataManager == null || newLocation == null) {
                return;
            }

            mCentralDataManager.setLocation(newLocation);

            // デバッグ情報を与える
            if (Settings.isDebugable()) {
                String time = AceUtils.formatTimeMilliSecToString(newLocation.getTime());
                {
                    int index = 0;
                    mDebugGeohash.getLineValue().setLine(index++, "Hash", Geohash.encode(newLocation.getLatitude(), newLocation.getLongitude()));
                    mDebugGeohash.getLineValue().setLine(index++, "更新", time);
                }

                {
                    int index = 0;
                    mDebugLocation.getLineValue().setLine(index++, "Lat", String.format("%.3f", newLocation.getLatitude()));
                    mDebugLocation.getLineValue().setLine(index++, "Lng", String.format("%.3f", newLocation.getLongitude()));
                    mDebugLocation.getLineValue().setLine(index++, "更新", time);
                }

                mDisplayExtension.setValue(Arrays.asList(mDebugGeohash, mDebugLocation));
            }
        }
    };
}

