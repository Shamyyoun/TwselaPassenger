package com.twsela.client.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twsela.client.ApiRequests;
import com.twsela.client.Const;
import com.twsela.client.R;
import com.twsela.client.connection.ConnectionHandler;
import com.twsela.client.controllers.LocationController;
import com.twsela.client.models.entities.AppLocation;
import com.twsela.client.models.entities.Driver;
import com.twsela.client.models.entities.Trip;
import com.twsela.client.models.responses.DriversResponse;
import com.twsela.client.utils.DialogUtils;
import com.twsela.client.utils.LocationUtils;
import com.twsela.client.utils.MarkerUtils;
import com.twsela.client.utils.PermissionUtil;
import com.twsela.client.utils.Utils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Shamyyoun on 5/28/16.
 */
public class HomeFragment extends ParentFragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, Runnable {
    private static final int MAP_PADDING = 300;
    private static final int LOCATION_FROM = 0;
    private static final int LOCATION_TO = 1;

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private View layoutLocations;
    private TextView tvFrom;
    private ImageButton ibFromMap;
    private ImageButton ibFromSearch;
    private TextView tvTo;
    private ImageButton ibToMap;
    private ImageButton ibToSearch;
    private View layoutMarkers;
    private ImageView ivMarker;
    private Button btnSelectLocation;

    private LocationController locationController;
    private LocationManager locationManager;
    private Trip tripHolder; // used to hold trip values
    private Marker[] markers; // used to hold map markers
    private boolean controlsEnabled;
    private AsyncTask addressTask;
    private AlertDialog gpsDialog;
    private int currentSelectLocationSign; // used to hold current location sign to set it after selecting it
    private Handler driversHandler;
    private ConnectionHandler driversConnectionHandler;
    private HashMap<String, Marker> driversMarkers;
    private boolean mapLoaded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create main objects
        locationController = new LocationController();
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        tripHolder = new Trip();
        markers = new Marker[2];
        driversHandler = new Handler();
        driversMarkers = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // init views
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        layoutLocations = findViewById(R.id.layout_locations);
        tvFrom = (TextView) findViewById(R.id.tv_from);
        ibFromMap = (ImageButton) findViewById(R.id.ib_from_map);
        ibFromSearch = (ImageButton) findViewById(R.id.ib_from_search);
        tvTo = (TextView) findViewById(R.id.tv_to);
        ibToMap = (ImageButton) findViewById(R.id.ib_to_map);
        ibToSearch = (ImageButton) findViewById(R.id.ib_to_search);
        layoutMarkers = findViewById(R.id.layout_markers);
        ivMarker = (ImageView) findViewById(R.id.iv_marker);
        btnSelectLocation = (Button) findViewById(R.id.btn_select_location);

        // disable controls till get the map
        enableControls(false);

        // add listeners
        ibFromMap.setOnClickListener(this);
        ibFromSearch.setOnClickListener(this);
        ibToMap.setOnClickListener(this);
        ibToSearch.setOnClickListener(this);
        btnSelectLocation.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // check location permission
        if (!isLocationPermGranted()) {
            // request permission from user
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Const.PERM_REQ_LOCATION);
            return;
        }

        // check gps
        if (!LocationUtils.isGpsEnabled(activity)) {
            showEnableGPSDialog();
            return;
        }

        // check internet
        if (!hasInternetConnection()) {
            // show msg
            Utils.showShortToast(activity, R.string.enable_internet_connection_to_use_app);
        }

        // then get the map async if all settings are ok
        mapFragment.getMapAsync(this);
    }

    private boolean isLocationPermGranted() {
        return activity.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        // customize the map
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setOnMapLoadedCallback(this);

        // enable the controls
        enableControls(true);
    }

    @Override
    public void onMapLoaded() {
        // get suitable location
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        // add from location and set its address if possible
        if (location != null) {
//            addFromLocation(location.getLatitude(), location.getLongitude(), null);
            addFromLocation(-73.856077, 40.848447, null); // TODO remove
            setFromAddress();
        }

        // start near drivers handler
        driversHandler.post(this);

        // update the flag
        mapLoaded = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == Const.PERM_REQ_LOCATION) {
            // check grant result
            if (PermissionUtil.isAllGranted(grantResults)) {
                // check gps
                if (!LocationUtils.isGpsEnabled(activity)) {
                    showEnableGPSDialog();
                } else {
                    // all settings are ok
                    // get the map async
                    mapFragment.getMapAsync(this);
                }
            } else {
                // show msg and finish
                Utils.showLongToast(activity, R.string.location_perm_refuse_msg);
                activity.finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showEnableGPSDialog() {
        // show confirm dialog
        gpsDialog = DialogUtils.showConfirmDialog(activity, R.string.enable_gps_to_use_app, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // open location settings activity
                LocationUtils.openLocationSettingsActivityForResult(fragment, Const.REQ_ENABLE_GPS);
            }
        }, R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // finish the activity
                dialogInterface.dismiss();
                activity.finish();
            }
        }, R.string.close);
        gpsDialog.setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_from_map:
                onFromMap();
                break;

            case R.id.ib_from_search:
                onFromSearch();
                break;

            case R.id.ib_to_map:
                onToMap();
                break;

            case R.id.ib_to_search:
                onToSearch();
                break;

            case R.id.btn_select_location:
                onSelectLocation();
                break;

            default:
                super.onClick(v);
        }
    }

    private void onFromSearch() {
        openAutoCompleteActivity(Const.REQ_FROM_SEARCH);
    }

    private void onToSearch() {
        openAutoCompleteActivity(Const.REQ_TO_SEARCH);
    }

    private void openAutoCompleteActivity(int requestCode) {
        // open auto complete activity if possible
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(activity);
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.REQ_ENABLE_GPS) {
            // check gps
            if (LocationUtils.isGpsEnabled(activity)) {
                // all settings are ok
                // get the map async
                mapFragment.getMapAsync(this);
            } else {
                // show msg and finish
                Utils.showShortToast(activity, R.string.gps_refuse_msg);
                activity.finish();
            }

        } else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Const.REQ_FROM_SEARCH) {
                // add from location
                Place place = PlaceAutocomplete.getPlace(activity, data);
                LatLng latLng = place.getLatLng();
                addFromLocation(latLng.latitude, latLng.longitude, place.getAddress().toString());
            } else if (requestCode == Const.REQ_TO_SEARCH) {
                // add to location
                Place place = PlaceAutocomplete.getPlace(activity, data);
                LatLng latLng = place.getLatLng();
                addToLocation(latLng.latitude, latLng.longitude, place.getAddress().toString());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addFromLocation(double lat, double lng, String address) {
        // save location, add marker, update ui and zoom to markers
        AppLocation location = saveLocation(lat, lng, address, LOCATION_FROM);
        addMarker(location, LOCATION_FROM);
        updateFromUI();

        zoomToMarkers();
    }

    private void addToLocation(double lat, double lng, String address) {
        // save location, add marker, update ui and zoom to markers
        AppLocation location = saveLocation(lat, lng, address, LOCATION_TO);
        addMarker(location, LOCATION_TO);
        updateToUI();

        zoomToMarkers();
    }

    private AppLocation saveLocation(double lat, double lng, String address, int locationSign) {
        // create the location
        AppLocation location = new AppLocation();
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setAddress(address);

        // save it
        if (locationSign == LOCATION_FROM) {
            tripHolder.setFromLocation(location);
        } else {
            tripHolder.setToLocation(location);
        }

        return location;
    }

    private void addMarker(AppLocation location, int locationSign) {
        // create objects
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(locationSign == LOCATION_FROM ?
                R.drawable.green_marker : R.drawable.red_marker);

        // check from marker
        Marker marker = markers[locationSign];
        if (marker == null) {
            // create the marker and add it the map
            MarkerOptions options = new MarkerOptions()
                    .position(latLng);
            marker = map.addMarker(options);
        } else {
            // set new position to the marker
            marker.setPosition(latLng);
        }

        // customize the marker
        marker.setIcon(icon);
        markers[locationSign] = marker;
    }

    private void zoomToMarkers() {
        // check markers map
        if (markers[LOCATION_TO] == null) {
            // this means that no to marker yet,
            // so zoom to the from marker only
            zoomToFromMarker();
        } else {
            // otherwise, zoom to all markers
            zoomToAllMarkers();
        }
    }

    private void zoomToFromMarker() {
        AppLocation location = tripHolder.getFromLocation();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, Const.INITIAL_ZOOM_LEVEL);
        map.animateCamera(cameraUpdate);
    }

    private void zoomToAllMarkers() {
        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                if (marker != null) {
                    builder.include(marker.getPosition());
                }
            }
            LatLngBounds bounds = builder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING);
            map.animateCamera(cameraUpdate);
        } catch (Exception e) {
            printStackTrace(e);
        }
    }

    private void updateFromUI() {
        if (tripHolder.getFromLocation() != null) {
            String addressStr = getString(R.string.from) + ": ";
            String address = tripHolder.getFromLocation().getAddress();
            if (address != null) {
                addressStr = addressStr + address;
            }

            tvFrom.setText(addressStr);
        } else {
            tvFrom.setText(R.string.choose_pickup);
        }
    }

    private void updateToUI() {
        if (tripHolder.getToLocation() != null) {
            String addressStr = getString(R.string.to) + ": ";
            String address = tripHolder.getToLocation().getAddress();
            if (address != null) {
                addressStr = addressStr + address;
            }

            tvTo.setText(addressStr);
        } else {
            tvTo.setText(R.string.choose_destination);
        }
    }

    private void enableControls(boolean enable) {
        ibFromMap.setEnabled(enable);
        ibFromSearch.setEnabled(enable);
        ibToMap.setEnabled(enable);
        ibToSearch.setEnabled(enable);

        controlsEnabled = enable;
    }

    private void setFromAddress() {
        setLocationAddress(LOCATION_FROM);
    }

    private void setToAddress() {
        setLocationAddress(LOCATION_TO);
    }

    private void setLocationAddress(final int locationSign) {
        // cancel previous one
        cancelAddressTask();

        // create the new one
        addressTask = new AsyncTask<Object, Object, String>() {
            // get suitable location
            AppLocation location = locationSign == LOCATION_FROM ? tripHolder.getFromLocation()
                    : tripHolder.getToLocation();

            @Override
            protected String doInBackground(Object[] params) {
                if (location != null) {
                    return LocationUtils.getAddress(activity, location.getLatitude(), location.getLongitude());
                } else {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String address) {
                if (address != null) {
                    // set it in the location address
                    location.setAddress(address);

                    // set the location and update the ui
                    if (locationSign == LOCATION_FROM) {
                        tripHolder.setFromLocation(location);
                        updateFromUI();
                    } else {
                        tripHolder.setToLocation(location);
                        updateToUI();
                    }
                }
            }
        };

        // execute it
        addressTask.execute();
    }

    private void cancelAddressTask() {
        if (addressTask != null) {
            addressTask.cancel(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelAddressTask();

        // cancel near drivers request if possible
        if (driversConnectionHandler != null) {
            driversConnectionHandler.cancel(true);
        }
    }

    private void onFromMap() {
        // show markers layout
        showMarkersLayout(LOCATION_FROM);

        // center the map to from location;
        centerMapToLocation(LOCATION_FROM);
    }

    private void onToMap() {
        // show markers layout
        showMarkersLayout(LOCATION_TO);

        // center the map to to location;
        centerMapToLocation(LOCATION_TO);
    }

    private void showMarkersLayout(int locationSign) {
        showLocationsLayout(locationSign, false);
    }

    private void showLocationsLayout(int locationSign) {
        showLocationsLayout(locationSign, true);
    }

    private void showLocationsLayout(int locationSign, boolean show) {
        // customize locations layout visibility
        layoutLocations.setVisibility(show ? View.VISIBLE : View.GONE);

        // customize the marker visibility if possible
        Marker marker = markers[locationSign];
        if (marker != null) {
            marker.setVisible(show);
        }

        // customize the marker icon
        ivMarker.setImageResource(locationSign == LOCATION_FROM ? R.drawable.large_green_marker : R.drawable.large_red_marker);

        // customize markers layout visibility
        layoutMarkers.setVisibility(show ? View.GONE : View.VISIBLE);

        // save the current locationSign
        currentSelectLocationSign = locationSign;
    }

    private void onSelectLocation() {
        // show locations layout
        showLocationsLayout(currentSelectLocationSign);

        // get map center location
        LatLng latLng = map.getCameraPosition().target;

        // add the suitable location and set its address
        if (currentSelectLocationSign == LOCATION_FROM) {
            addFromLocation(latLng.latitude, latLng.longitude, null);
            setFromAddress();
        } else {
            addToLocation(latLng.latitude, latLng.longitude, null);
            setToAddress();
        }
    }

    private void centerMapToLocation(int locationSign) {
        // get suitable location
        AppLocation location = locationSign == LOCATION_FROM ? tripHolder.getFromLocation()
                : tripHolder.getToLocation();

        // check the location
        if (location != null) {
            // animate the map to this location
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            map.animateCamera(cameraUpdate);
        }
    }

    // near drivers runnable run method
    @Override
    public void run() {
        CameraPosition cameraPosition = map.getCameraPosition();

        // check zoom level
        if (cameraPosition.zoom < Const.MIN_LOADING_DRIVERS_ZOOM_LEVEL) {
            // continue and exit this method
            continueNearDriversTask();
            return;
        }

        // get map center location
        LatLng latLng = cameraPosition.target;

        // create and execute the request
        driversConnectionHandler = ApiRequests.nearDrivers(activity, this, latLng.latitude, latLng.longitude);
    }

    private void continueNearDriversTask() {
        // continue drivers task after static time
        driversHandler.removeCallbacks(this);
        driversHandler.postDelayed(this, Const.NEAR_DRIVERS_REQ_DELAY);
    }

    @Override
    public void onSuccess(Object response, int statusCode, String tag) {
        // check tag
        if (Const.ROUTE_NEAR_DRIVERS.equals(tag)) {
            // near drivers request
            // check response
            DriversResponse driversResponse = (DriversResponse) response;
            if (driversResponse.isSuccess() && driversResponse.getContent() != null) {
                updateDriversMarkers(driversResponse.getContent());
                continueNearDriversTask();
            }
        }
    }

    @Override
    public void onFail(Exception ex, int statusCode, String tag) {
        // continue the task
        continueNearDriversTask();
    }

    private void updateDriversMarkers(List<Driver> drivers) {
        // check drivers size
        if (drivers.size() == 0) {
            // clear all markers
            map.clear();
            driversMarkers.clear();
            return;
        }

        // create temp hash map to hold all drivers markers
        HashMap<String, Marker> tempDriverMarkers = new HashMap<>();

        // loop all drivers to add a new marker or just change its current marker
        for (Driver driver : drivers) {
            // prepare driver values
            String id = driver.getId();
            double lat = locationController.getLatitude(driver.getLocation());
            double lng = locationController.getLongitude(driver.getLocation());
            float bearing = Utils.convertToFloat(driver.getBearing());
            LatLng latLng = new LatLng(lat, lng);

            // get driver marker and check it
            Marker marker = driversMarkers.get(id);
            if (marker != null) {
                // marker found
                // this driver already has a marker on the map
                // animate his marker
                MarkerUtils.animateMarkerToICSWithBearing(marker, latLng, bearing, new MarkerUtils.LatLngInterpolator.LinearFixed());

                // remove this marker from the drivers markers map
                driversMarkers.remove(id);
            } else {
                // marker not found
                // this is a new driver and doesn't has a marker on the map yet
                // add new one for him
                marker = map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .rotation(bearing)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_car_icon))
                        .anchor(0.5f, 0.5f)
                        .flat(true));
            }

            // add the marker to the temp hash map
            tempDriverMarkers.put(id, marker);
        }

        // now, all remaining markers in driversMarkers map not exists, so remove them
        for (Marker marker : driversMarkers.values()) {
            marker.remove();
        }

        // assign temp marker map to current driverMarkers map because they are the current existing drivers
        driversMarkers.clear();
        driversMarkers = tempDriverMarkers;
    }

    @Override
    public void onPause() {
        super.onPause();

        // stop near drivers task
        driversHandler.removeCallbacks(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // start near drivers task if possible
        if (mapLoaded) {
            driversHandler.post(this);
        }
    }
}
