package com.eaglesakura.andriders.ui.navigation.session;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;

import com.eaglesakura.andriders.R;
import com.eaglesakura.andriders.central.CentralDataReceiver;
import com.eaglesakura.andriders.central.SensorDataReceiver;
import com.eaglesakura.andriders.data.res.AppImageLoader;
import com.eaglesakura.andriders.serialize.RawCentralData;
import com.eaglesakura.andriders.serialize.RawLocation;
import com.eaglesakura.andriders.ui.navigation.base.AppFragment;
import com.eaglesakura.andriders.util.AppLog;
import com.eaglesakura.android.firebase.auth.FirebaseAuthorizeManager;
import com.eaglesakura.android.framework.ui.FragmentHolder;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.util.ImageUtil;
import com.eaglesakura.lambda.CancelCallback;
import com.squareup.otto.Subscribe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

/**
 * ユーザーの簡易ナビゲートを行う
 */
@FragmentLayout(R.layout.session_info_navigation)
public class NavigationMapFragment extends AppFragment {

    CentralDataReceiver mCentralDataReceiver;

    FragmentHolder<SupportMapFragment> mMapFragment = new FragmentHolder<SupportMapFragment>(this, R.id.ViewHolder_GoogleMap, SupportMapFragment.class.getSimpleName()) {
        @NonNull
        @Override
        protected SupportMapFragment newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception {
            GoogleMapOptions opt = new GoogleMapOptions();
            opt.mapToolbarEnabled(false);
            opt.compassEnabled(true);
            opt.zOrderOnTop(false);
            opt.mapType(GoogleMap.MAP_TYPE_NORMAL);
            SupportMapFragment fragment = SupportMapFragment.newInstance(opt);
            return fragment;
        }
    }.bind(mLifecycleDelegate);

    GoogleMap mGoogleMap;

    /**
     * ユーザー位置表示用マーカー
     */
    Marker mUserMarker;

    Bitmap mUserIcon;

    @NonNull
    SessionControlBus mSessionControlBus;

    @NonNull
    AppImageLoader mImageLoader;


    private static final float MAP_ZOOM_DEFAULT = 10;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCentralDataReceiver = new CentralDataReceiver(context);
        mCentralDataReceiver.addHandler(mLocationHandlerImpl);

        mSessionControlBus = findInterfaceOrThrow(SessionControlBus.Holder.class).getSessionControlBus();
        mSessionControlBus.bind(mLifecycleDelegate, this);

        mImageLoader = findInterfaceOrThrow(AppImageLoader.Holder.class).getImageLoader();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mUserIcon == null) {
            mUserIcon = ImageUtil.decode(getContext(), R.mipmap.ic_user_position);
        }

        // Google Mapの同期待を行う
        mMapFragment.get().getMapAsync(googleMap -> {
            // 正常なForeground状態でのみハンドリングする
            getCallbackQueue().run(CallbackTime.CurrentForeground, () -> {
                onGoogleMapReady(googleMap);
            });
        });

        loadUserIcon();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mUserMarker != null) {
            mUserMarker.remove();
            mUserMarker = null;
        }
        mGoogleMap = null;
        if (mCentralDataReceiver.isConnected()) {
            mCentralDataReceiver.disconnect();
        }
    }

    /**
     * GoogleMap準備完了したタイミングでCentralに接続する
     */
    @UiThread
    protected void onGoogleMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mCentralDataReceiver.connect();
    }

    /**
     * ユーザー表示アイコンをロードする
     */
    @UiThread
    void loadUserIcon() {
        asyncUI((BackgroundTask<Bitmap> task) -> {
            CancelCallback cancelCallback = AppSupportUtil.asCancelCallback(task);
            FirebaseUser user = FirebaseAuthorizeManager.getInstance().await(cancelCallback);
            Drawable drawable = mImageLoader.newImage(user.getPhotoUrl(), true).await(cancelCallback);
            return ((BitmapDrawable) drawable).getBitmap();
        }).completed((result, task) -> {
            // アイコンを保持し、必要であれば切り替える
            mUserIcon = result;
            if (mUserMarker != null) {
                mUserMarker.setIcon(BitmapDescriptorFactory.fromBitmap(result));
            }
        }).failed((error, task) -> {
            AppLog.printStackTrace(error);
        }).start();
    }

    private Marker newMarker(Bitmap icon) {
        MarkerOptions opt = new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .visible(true);

        return mGoogleMap.addMarker(opt);
    }

    @Subscribe
    void onModified(SessionControlBus sessionControlBus) {
        AppLog.widget("Bus Modified[%s]", sessionControlBus.getData());
    }

    /**
     * GPS情報ハンドリングして、位置を更新する
     */
    SensorDataReceiver.LocationHandler mLocationHandlerImpl = new SensorDataReceiver.LocationHandler() {
        @Override
        public void onReceived(@NonNull RawCentralData master, @NonNull RawLocation sensor) {
            if (mUserMarker == null) {
                mUserMarker = newMarker(mUserIcon);
            }
            LatLng latLng = new LatLng(sensor.latitude, sensor.longitude);
            mUserMarker.setPosition(latLng);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    };
}
