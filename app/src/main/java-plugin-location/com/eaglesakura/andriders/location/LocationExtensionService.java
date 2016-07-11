package com.eaglesakura.andriders.location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.plugin.CentralEngineConnection;
import com.eaglesakura.andriders.plugin.AcePluginService;
import com.eaglesakura.andriders.plugin.data.CentralEngineData;
import com.eaglesakura.andriders.plugin.display.DisplayData;
import com.eaglesakura.andriders.plugin.display.DisplayDataSender;
import com.eaglesakura.andriders.plugin.display.LineValue;
import com.eaglesakura.andriders.service.base.AppBaseService;
import com.eaglesakura.andriders.ui.auth.PermissionRequestActivity;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.geo.Geohash;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 現在位置を配信するExtension
 */
public class LocationExtensionService extends AppBaseService implements AcePluginService {
    GoogleApiClient mLocationApiClient;

    CentralEngineData mCentralDataManager;

    DisplayDataSender mDisplayExtension;

    DisplayData mDebugLocation;

    DisplayData mDebugGeohash;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AppLog.system("onBind(%s)", toString());
        CentralEngineConnection session = CentralEngineConnection.onBind(this, intent);
        if (session == null) {
            return null;
        }

        return session.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AppLog.system("onUnbind(%s)", toString());
        CentralEngineConnection.onUnbind(this, intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public PluginInformation getExtensionInformation(CentralEngineConnection connection) {
        PluginInformation info = new PluginInformation(this, "gps_loc");
        info.setSummary("現在位置をサイクルコンピュータに反映します。");
        info.setCategory(Category.CATEGORY_LOCATION);
        info.setHasSetting(false);
        return info;
    }

    @Override
    public List<DisplayKey> getDisplayInformation(CentralEngineConnection connection) {

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
    public void onAceServiceConnected(CentralEngineConnection connection) {
        if (!isRuntimePermissionGranted()) {
            // 許可されていないので、このServiceは何もしない
            return;
        }

        mDebugGeohash = new DisplayData(this, "debug.loc");
        mDebugGeohash.setValue(new LineValue(2)); // hash, time

        mDebugLocation = new DisplayData(this, "debug.geohash");
        mDebugLocation.setValue(new LineValue(3)); // lat, lng, time

        mCentralDataManager = connection.getCentralDataExtension();
        mDisplayExtension = connection.getDisplayExtension();

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
    public void onAceServiceDisconnected(CentralEngineConnection connection) {
        if (mLocationApiClient == null) {
            return;
        }

        mCentralDataManager = null;
        mLocationApiClient.disconnect();
    }

    @Override
    public void onEnable(CentralEngineConnection connection) {
        if (!isRuntimePermissionGranted()) {
            Toast.makeText(this, "Request GPS Permission!!", Toast.LENGTH_SHORT).show();
            Intent intent = PermissionRequestActivity.createIntent(LocationExtensionService.this,
                    new PermissionUtil.PermissionType[]{PermissionUtil.PermissionType.SelfLocation});
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onDisable(CentralEngineConnection connection) {

    }

    @Override
    public void startSetting(CentralEngineConnection connection) {

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
            if (mSettings.isDebuggable()) {
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
    };
}

