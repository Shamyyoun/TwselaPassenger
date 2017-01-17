package com.twsela.client.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.twsela.client.ApiRequests;
import com.twsela.client.Const;
import com.twsela.client.R;
import com.twsela.client.TwselaApp;
import com.twsela.client.connection.ConnectionHandler;
import com.twsela.client.controllers.DistanceMatrixController;
import com.twsela.client.controllers.LocationController;
import com.twsela.client.controllers.TripController;
import com.twsela.client.models.entities.DistanceMatrixResult;
import com.twsela.client.models.entities.Trip;
import com.twsela.client.models.responses.DistanceMatrixResponse;
import com.twsela.client.models.responses.TripResponse;
import com.twsela.client.utils.AppUtils;
import com.twsela.client.utils.Utils;

public class TripDetailsActivity extends ParentActivity {
    private String tripId;
    private TripController tripController;
    private LocationController locationController;
    private DistanceMatrixController distanceMatrixController;

    private TextView tvDriverName;
    private TextView tvPickupAddress;
    private TextView tvDestinationAddress;
    private TextView tvDistance;
    private TextView tvDuration;
    private TextView tvFare;
    private Button btnClose;

    private Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // obtain main objects
        tripId = getIntent().getStringExtra(Const.KEY_ID);
        tripController = new TripController();
        locationController = new LocationController();
        distanceMatrixController = new DistanceMatrixController();

        // init views
        tvDriverName = (TextView) findViewById(R.id.tv_driver_name);
        tvPickupAddress = (TextView) findViewById(R.id.tv_pickup_address);
        tvDestinationAddress = (TextView) findViewById(R.id.tv_destination_address);
        tvDistance = (TextView) findViewById(R.id.tv_distance);
        tvDuration = (TextView) findViewById(R.id.tv_duration);
        tvFare = (TextView) findViewById(R.id.tv_fare);
        btnClose = (Button) findViewById(R.id.btn_close);

        // add listeners
        btnClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_close) {
            onBackPressed();
        } else {
            super.onClick(v);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // load trip details
        preLoadTripDetails();
    }

    private void updateUI() {
        // set driver name
        String driverUsername = tripController.getDriverUsername(trip);
        if (driverUsername != null) {
            tvDriverName.setText(driverUsername);
        } else {
            tvDriverName.setText("---------------");
        }

        // set pickup address
        if (!Utils.isNullOrEmpty(trip.getPickupAddress())) {
            tvPickupAddress.setText(trip.getPickupAddress());
        } else {
            tvPickupAddress.setText(R.string.point_on_map);
        }

        // set destination
        if (trip.getActualDestinationLocation() != null) {
            if (!Utils.isNullOrEmpty(trip.getActualDestinationAddress())) {
                tvDestinationAddress.setText(trip.getActualDestinationAddress());
            } else {
                tvDestinationAddress.setText(R.string.point_on_map);
            }
        } else {
            tvDestinationAddress.setText(R.string.not_available);
        }

        // set duration
        String duration = tripController.getDuration(this, trip);
        if (duration != null) {
            tvDuration.setText(duration);
        } else {
            tvDuration.setText("---------------");
        }

        // set fare
        String costStr = Utils.formatDouble(trip.getCost()) + " " + getString(R.string.currency);
        tvFare.setText(costStr);
    }

    private void updateDistanceUI(String distance) {
        if (distance != null) {
            tvDistance.setText(distance);
        } else {
            tvDistance.setText("---------------");
        }
    }

    private void preLoadTripDetails() {
        // check internet connection
        if (hasInternetConnection()) {
            showProgressDialog();
            loadTripDetails();
        } else {
            Utils.showShortToast(this, R.string.no_internet_connection);
        }
    }

    private void loadTripDetails() {
        // send the request
        ConnectionHandler connectionHandler = ApiRequests.getTripDetails(this, this, tripId);
        cancelWhenDestroyed(connectionHandler);
    }

    @Override
    public void onSuccess(Object response, int statusCode, String tag) {

        // check tag
        if (Const.ROUTE_GET_DETAILS_BY_ID.equals(tag)) {
            // trip details request
            // check response
            TripResponse tripResponse = (TripResponse) response;
            if (tripResponse.isSuccess() && tripResponse.getContent() != null) {
                // update the ui
                this.trip = tripResponse.getContent();
                updateUI();

                // load distance matrix
                loadDistanceMatrix();
            } else {
                // show msg
                String msg = AppUtils.getResponseMsg(this, tripResponse, R.string.failed_loading_details);
                Utils.showShortToast(this, msg);
            }
        } else if (Const.TAG_DISTANCE_MATRIX.equals(tag)) {
            hideProgressDialog();

            // get distance matrix result
            DistanceMatrixResponse distanceMatrixResponse = (DistanceMatrixResponse) response;
            DistanceMatrixResult distanceResult = distanceMatrixController.getDistanceResult(distanceMatrixResponse);

            // check it
            if (distanceResult != null) {
                updateDistanceUI(distanceResult.getText());
            } else {
                updateDistanceUI(null);
                // show msg
                Utils.showShortToast(this, R.string.failed_calculating_distance);
            }
        }
    }

    private void loadDistanceMatrix() {
        // prepare params
        double originLat = locationController.getLatitude(trip.getPickupLocation());
        double originLng = locationController.getLongitude(trip.getPickupLocation());
        double destLat = locationController.getLatitude(trip.getActualDestinationLocation());
        double destLng = locationController.getLongitude(trip.getActualDestinationLocation());
        String distanceApiKey = getString(R.string.google_api_server_key);
        String language = TwselaApp.getLanguage(this);

        // send the request
        ConnectionHandler connectionHandler = ApiRequests.getDistanceMatrix(this, this, originLat,
                originLng, destLat, destLng, distanceApiKey, language);
        cancelWhenDestroyed(connectionHandler);
    }
}