package com.twsela.client.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.twsela.client.Const;
import com.twsela.client.R;
import com.twsela.client.utils.PermissionUtil;
import com.twsela.client.utils.Utils;

public class MainActivity extends ParentActivity implements OnMapReadyCallback {
    private SupportMapFragment mapFragment;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init map fragment and obtain the map async
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        // check location permission
        // and request it from user if not granted
        boolean permGranted = checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!permGranted) {
            // request permission from user
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Const.PERM_REQ_LOCATION);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // customize the map
        this.map = googleMap;
//        map.setMyLocationEnabled(true);
//
//        googleMap.
//
//        // change camera to my location when possible
//        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
//            @Override
//            public void onMyLocationChange(Location location) {
//                Log.e("Shops Map", "Location Changed");
//                mCurrentLocation = location;
//
//                if (mZoomToMyLocation) {
//                    LatLng myCoordinate = new LatLng(location.getLatitude(), location.getLongitude());
//                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myCoordinate, 12);
//                    googleMap.animateCamera(cameraUpdate);
//
//                    mZoomToMyLocation = false;
//                }
//            }
//        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == Const.PERM_REQ_LOCATION) {
            // check grant result
            if (!PermissionUtil.isAllGranted(grantResults)) {
                // show msg and finish
                Utils.showLongToast(this, R.string.location_perm_refuse_msg);
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}