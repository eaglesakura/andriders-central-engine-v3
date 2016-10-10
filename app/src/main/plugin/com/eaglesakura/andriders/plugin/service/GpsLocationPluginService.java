package com.eaglesakura.andriders.plugin.service;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.notification.NotificationData;
import com.eaglesakura.andriders.plugin.AcePluginService;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.plugin.connection.PluginConnection;
import com.eaglesakura.andriders.plugin.data.CentralEngineSessionData;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.plugin.display.DisplayDataSender;
import com.eaglesakura.andriders.plugin.display.LineValue;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.geo.Geohash;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.StringUtil;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 現在位置を配信するExtension
 */
public class GpsLocationPluginService extends Service implements AcePluginService {
    GoogleApiClient mLocationApiClient;

    DisplayDataSender mDisplayExtension;

    DisplayData mDebugLocation;

    DisplayData mDebugGeohash;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AppLog.system("onBind(%s)", toString());
        PluginConnection connection = PluginConnection.onBind(this, intent);
        if (connection == null) {
            return null;
        }

        return connection.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AppLog.system("onUnbind(%s)", toString());
        PluginConnection.onUnbind(this, intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public PluginInformation getExtensionInformation(PluginConnection connection) {
        PluginInformation info = new PluginInformation(this, "gps_loc");
        info.setSummary("現在位置をサイクルコンピュータに反映します。");
        info.setCategory(Category.CATEGORY_LOCATION);
        info.setHasSetting(false);
        return info;
    }

    @Override
    public List<DisplayKey> getDisplayInformation(PluginConnection connection) {

        List<DisplayKey> result = new ArrayList<>();

        if (connection.isDebuggable()) {
            // 位置情報をデバッグ表示
            {
                DisplayKey info = new DisplayKey(this, "debug.loc");
                info.setTitle("DBG:GPS座標");
                result.add(info);
            }
            // 位置情報をデバッグ表示
            {
                DisplayKey info = new DisplayKey(this, "debug.geohash");
                info.setTitle("DBG:ジオハッシュ");
                result.add(info);
            }
        }

        if (CollectionUtil.isEmpty(result)) {
            return null;
        } else {
            return result;
        }
    }

    private boolean isRuntimePermissionGranted() {
        return PermissionUtil.isRuntimePermissionGranted(this, PermissionUtil.PermissionType.SelfLocation);
    }

    @Override
    public void onAceServiceConnected(PluginConnection connection) {
        if (!isRuntimePermissionGranted()) {
            // 許可されていないので、このServiceは何もしない
            AppLog.system("Not Granted GPS!!");
            NotificationData notification =
                    new NotificationData.Builder(this)
                            .message(getString(R.string.Message_LocationPlugin_PermissionError))
                            .getNotification();
            connection.getDisplay().queueNotification(notification);
            return;
        }

        mDebugGeohash = new DisplayData(this, "debug.loc");
        mDebugGeohash.setValue(new LineValue(2)); // hash, time

        mDebugLocation = new DisplayData(this, "debug.geohash");
        mDebugLocation.setValue(new LineValue(3)); // lat, lng, time

        mDisplayExtension = connection.getDisplay();

        mLocationApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        AppLog.gps("location client :: connected");
                        try {
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    mLocationApiClient,
                                    createLocationRequest(),
                                    new LocationListenerImpl(connection)
                            );
                        } catch (SecurityException e) {
                            // failed connect...
                            AppLog.printStackTrace(e);
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
    public void onAceServiceDisconnected(PluginConnection connection) {
        if (mLocationApiClient != null) {
            mLocationApiClient.disconnect();
        }
    }

    @Override
    public void onEnable(PluginConnection connection) {
    }

    @Override
    public void onDisable(PluginConnection connection) {

    }

    @Override
    public void startSetting(PluginConnection connection) {

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

    class LocationListenerImpl implements LocationListener {
        PluginConnection mPluginConnection;

        public LocationListenerImpl(PluginConnection pluginConnection) {
            mPluginConnection = pluginConnection;
        }

        @Override
        public void onLocationChanged(Location newLocation) {
            if (newLocation == null) {
                return;
            }

            CentralEngineSessionData centralData = mPluginConnection.getCentralData();
            centralData.setLocation(newLocation);

            // デバッグ情報を与える
            if (mPluginConnection.isDebuggable()) {
                String time = StringUtil.toString(new Date());
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
    }
}

