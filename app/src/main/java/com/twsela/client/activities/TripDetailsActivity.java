package com.twsela.client.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twsela.client.ApiRequests;
import com.twsela.client.Const;
import com.twsela.client.R;
import com.twsela.client.connection.ConnectionHandler;
import com.twsela.client.controllers.ActiveUserController;
import com.twsela.client.controllers.LocationController;
import com.twsela.client.controllers.TripController;
import com.twsela.client.models.entities.Trip;
import com.twsela.client.models.enums.TripStatus;
import com.twsela.client.models.events.TripStatusChanged;
import com.twsela.client.models.responses.TripResponse;
import com.twsela.client.utils.MarkerUtils;
import com.twsela.client.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TripDetailsActivity extends ParentActivity implements OnMapReadyCallback {
    private static final int MAP_PADDING = 200;
    private static final int MARKER_SIGN_DRIVER = 0;
    private static final int MARKER_SIGN_PICKUP = 1;
    private static final int MARKER_SIGN_DESTINATION = 2;

    private String id;
    private TripStatus tripStatus;
    private ActiveUserController activeUserController;
    private TripController tripController;
    private LocationController locationController;

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private TextView tvTripStatus;
    private TextView tvDriverName;

    private Trip trip;
    private Marker[] markers;
    private boolean zoomToMarkers = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // obtain main objects
        id = getIntent().getStringExtra(Const.KEY_ID);
        tripStatus = (TripStatus) getIntent().getSerializableExtra(Const.KEY_STATUS);
        activeUserController = new ActiveUserController(this);
        tripController = new TripController();
        locationController = new LocationController();
        markers = new Marker[3];

        // init views
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        tvTripStatus = (TextView) findViewById(R.id.tv_trip_status);
        tvDriverName = (TextView) findViewById(R.id.tv_driver_name);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // get the map
        mapFragment.getMapAsync(this);

        // update trip status
        updateTripStatusUI();

        // load trip details
        preLoadTripDetails();

        // start listening for events
        EventBus.getDefault().register(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // customize map
        this.map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);
    }

    private void updateTripStatusUI() {
        if (tripStatus == TripStatus.ACCEPTED) {
            tvTripStatus.setText(R.string.driver_in_his_way_to_pickup);
        } else if (tripStatus == TripStatus.DRIVER_ARRIVED) {
            tvTripStatus.setText(R.string.driver_has_arrived_to_pickup);
        } else if (tripStatus == TripStatus.STARTED) {
            tvTripStatus.setText(R.string.in_way_to_your_destination);
        } else {
            tvTripStatus.setText("----------------");
        }
    }

    private void updateUI() {
        // set driver name if possible
        String driverUsername = tripController.getDriverUsername(trip);
        if (driverUsername != null) {
            tvDriverName.setText(driverUsername);
        } else {
            tvDriverName.setText("----------------");
        }
    }

    private void preLoadTripDetails() {
        if (hasInternetConnection()) {
            showProgressDialog();
            loadTripDetails();
        } else {
            Utils.showShortToast(activity, R.string.no_internet_connection);
        }
    }

    private void loadTripDetails() {
        ConnectionHandler request = ApiRequests.getTripDetails(activity, this, id);
        cancelWhenDestroyed(request);
    }

    @Override
    public void onSuccess(Object response, int statusCode, String tag) {
        hideProgressDialog();

        // check response
        TripResponse tripResponse = (TripResponse) response;
        if (tripResponse.isSuccess() && tripResponse.getContent() != null) {
            // update the ui
            trip = tripResponse.getContent();
            updateUI();

            // update driver marker
            updateDriverMarker();

            // check zoom flag
            if (zoomToMarkers) {
                // update pickup and zoom for the first time only
                updatePickupMarker();
                zoomToMarkers();
                zoomToMarkers = false;
            }
        }
    }

    private void updateDriverMarker() {
        updateMarker(MARKER_SIGN_DRIVER);
    }

    private void updatePickupMarker() {
        updateMarker(MARKER_SIGN_PICKUP);
    }

    private void updateMarker(int markerSign) {
        try {
            // get marker
            Marker marker = markers[markerSign];

            // prepare objects and values
            LatLng latLng;
            float bearing = 0;
            BitmapDescriptor icon;
            if (markerSign == MARKER_SIGN_DRIVER) {
                latLng = locationController.createLatLng(trip.getDriver().getLocation());
                bearing = Utils.convertToFloat(trip.getDriver().getBearing());
                icon = BitmapDescriptorFactory.fromResource(R.drawable.map_car_icon);
            } else if (markerSign == MARKER_SIGN_PICKUP) {
                latLng = locationController.createLatLng(trip.getPickupLocation());
                icon = BitmapDescriptorFactory.fromResource(R.drawable.green_marker);
            } else {
                latLng = locationController.createLatLng(trip.getDestinationLocation());
                icon = BitmapDescriptorFactory.fromResource(R.drawable.red_marker);
            }

            // check marker
            if (marker == null) {
                // create the marker and add it to the map
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .rotation(bearing)
                        .icon(icon);
                marker = map.addMarker(options);
            } else {
                // animate to the new position
                MarkerUtils.animateMarkerToICSWithBearing(marker, latLng, bearing,
                        new MarkerUtils.LatLngInterpolator.LinearFixed());
            }

            // save the marker in the array
            markers[markerSign] = marker;
        } catch (Exception e) {
            printStackTrace(e);
        }
    }

    private void zoomToMarkers() {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTripStatusChanged(TripStatusChanged event) {
        // update last trip status
        activeUserController.updateLastTripStatus(event.getStatus());

        // update trip status
        this.tripStatus = event.getStatus();
        updateTripStatusUI();

        // check trip status
        if (event.getStatus() == TripStatus.STARTED) {
            // add destination marker if possible
            showDestinationMarker();
        } else if (event.getStatus() == TripStatus.ENDED) {
            // show msg and finish
            Utils.showLongToast(this, R.string.thanks_for_using_twsela);
            finish();
        }
    }

    private void showDestinationMarker() {
        if (trip.getDestinationLocation() != null) {
            // add destination marker
            updateMarker(MARKER_SIGN_DESTINATION);
        }

        // remove pickup marker
        Marker marker = markers[MARKER_SIGN_PICKUP];
        marker.remove();
        markers[MARKER_SIGN_PICKUP] = null;

        // zoom to markers
        zoomToMarkers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // stop listening for events
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
    }
}