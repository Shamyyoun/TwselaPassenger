package com.twsela.client.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.twsela.client.Const;
import com.twsela.client.R;
import com.twsela.client.utils.PermissionUtil;
import com.twsela.client.utils.Utils;

/**
 * Created by Shamyyoun on 5/28/16.
 */
public class HomeFragment extends ParentFragment implements OnMapReadyCallback {
    private SupportMapFragment mapFragment;
    private GoogleMap map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // init map fragment and obtain the map async
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        // check location permission
        // and request it from user if not granted
        if (!isLocationPermGranted()) {
            // request permission from user
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Const.PERM_REQ_LOCATION);
        }

        return rootView;
    }

    private boolean isLocationPermGranted() {
        return activity.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        // enable my location if location perm granted
        if (isLocationPermGranted()) {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        map.setMyLocationEnabled(true);

        // move camera to my location when possible
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                LatLng myCoordinate = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myCoordinate, Const.INITIAL_ZOOM_LEVEL);
                map.moveCamera(cameraUpdate);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == Const.PERM_REQ_LOCATION) {
            // check grant result
            if (PermissionUtil.isAllGranted(grantResults)) {
                // enable my location
                enableMyLocation();
            } else {
                // show msg and finish
                Utils.showLongToast(activity, R.string.location_perm_refuse_msg);
                activity.finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
