package com.twsela.client;

import android.content.Context;

import com.twsela.client.connection.ConnectionHandler;
import com.twsela.client.connection.ConnectionListener;
import com.twsela.client.models.bodies.NearDriversBody;
import com.twsela.client.models.entities.MongoLocation;
import com.twsela.client.models.entities.Trip;
import com.twsela.client.models.responses.DirectionsResponse;
import com.twsela.client.models.responses.DistanceMatrixResponse;
import com.twsela.client.models.responses.DriversResponse;
import com.twsela.client.models.responses.LoginResponse;
import com.twsela.client.models.responses.TripResponse;
import com.twsela.client.utils.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Shamyyoun on 5/31/16.
 */
public class ApiRequests {

    public static ConnectionHandler<LoginResponse> login(Context context, ConnectionListener<LoginResponse> listener,
                                                         String username, String password, String gcm) {

        // prepare url
        String url = AppUtils.getPassengerApiUrl(Const.ROUTE_LOGIN);

        // create connection handler
        ConnectionHandler<LoginResponse> connectionHandler = new ConnectionHandler(context, url,
                LoginResponse.class, listener, Const.ROUTE_LOGIN);

        // add parameters
        Map<String, String> params = new HashMap<>();
        params.put(Const.PARAM_USERNAME, username);
        params.put(Const.PARAM_PASSWORD, password);
        params.put(Const.PARAM_GCM, gcm);
        connectionHandler.setParams(params);

        // execute and return
        connectionHandler.executePost();
        return connectionHandler;
    }

    public static ConnectionHandler<DriversResponse> nearDrivers(Context context, ConnectionListener<DriversResponse> listener,
                                                                 double lat, double lng) {

        // prepare url
        String url = AppUtils.getDriverApiUrl(Const.ROUTE_NEAR_DRIVERS);

        // create connection handler
        ConnectionHandler<DriversResponse> connectionHandler = new ConnectionHandler(context, url,
                DriversResponse.class, listener, Const.ROUTE_NEAR_DRIVERS);

        // create and set the body
        NearDriversBody body = new NearDriversBody();
        MongoLocation location = new MongoLocation();
        List<Double> coordinates = new ArrayList<>(2);
        coordinates.add(lat);
        coordinates.add(lng);
        location.setCoordinates(coordinates);
        body.setLocation(location);
        connectionHandler.setBody(body);

        // execute and return
        connectionHandler.executeRawJson();
        return connectionHandler;
    }

    public static ConnectionHandler<TripResponse> requestTrip(Context context, ConnectionListener<TripResponse> listener,
                                                              String passengerId, double pickupLat,
                                                              double pickupLng, String pickupAddress,
                                                              double destinationLat, double destinationLng,
                                                              String destinationAddress) {

        // prepare url 
        String url = AppUtils.getTripApiUrl(Const.ROUTE_REQUEST_TRIP);

        // create connection handler
        ConnectionHandler<TripResponse> connectionHandler = new ConnectionHandler(context, url,
                TripResponse.class, listener, Const.ROUTE_REQUEST_TRIP);

        // create and set the body
        Trip body = new Trip();
        body.setPassengerId(passengerId);
        MongoLocation pickupLocation = new MongoLocation();
        List<Double> pickupCoordinates = new ArrayList<>(2);
        pickupCoordinates.add(pickupLat);
        pickupCoordinates.add(pickupLng);
        pickupLocation.setCoordinates(pickupCoordinates);
        body.setPickupLocation(pickupLocation);
        body.setPickupAddress(pickupAddress);
        if (destinationLat != 0 && destinationLng != 0) {
            MongoLocation destinationLocation = new MongoLocation();
            List<Double> destinationCoordinates = new ArrayList<>(2);
            destinationCoordinates.add(destinationLat);
            destinationCoordinates.add(destinationLng);
            destinationLocation.setCoordinates(destinationCoordinates);
            body.setDestinationLocation(destinationLocation);
            body.setDestinationAddress(destinationAddress);
        }
        connectionHandler.setBody(body);

        // execute and return
        connectionHandler.executeRawJson();
        return connectionHandler;
    }

    public static ConnectionHandler<TripResponse> getTripDetails(Context context,
                                                                 ConnectionListener<TripResponse> listener, String id) {

        // prepare url
        String url = AppUtils.getTripApiUrl(Const.ROUTE_GET_DETAILS_BY_ID);
        url += "?" + Const.PARAM_ID + "=" + id;

        // create connection handler
        ConnectionHandler<TripResponse> connectionHandler = new ConnectionHandler(context, url,
                TripResponse.class, listener, Const.ROUTE_GET_DETAILS_BY_ID);

        // execute and return
        connectionHandler.executeGet();
        return connectionHandler;
    }

    public static ConnectionHandler<DistanceMatrixResponse> getDistanceMatrix(Context context,
                                                                              ConnectionListener<DistanceMatrixResponse> listener,
                                                                              String origins, String destinations,
                                                                              String apiKey, String language) {
        // prepare url
        String url = String.format(Locale.ENGLISH,
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s&destinations=%s&language=%s&key=%s",
                origins, destinations, language, apiKey);

        // create connection handler
        ConnectionHandler<DistanceMatrixResponse> connectionHandler = new ConnectionHandler(context, url,
                DistanceMatrixResponse.class, listener, Const.TAG_DISTANCE_MATRIX);

        connectionHandler.executeGet();
        return connectionHandler;
    }

    public static ConnectionHandler<DirectionsResponse> getDirections(Context context,
                                                                      ConnectionListener<DirectionsResponse> listener,
                                                                      double originLat, double originLng,
                                                                      double destLat, double destLng,
                                                                      String apiKey, String language) {
        // prepare url
        String url = String.format(Locale.ENGLISH,
                "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&language=%s&key=%s",
                originLat, originLng, destLat, destLng, language, apiKey);

        // create connection handler
        ConnectionHandler<DirectionsResponse> connectionHandler = new ConnectionHandler(context, url,
                DirectionsResponse.class, listener, Const.TAG_DIRECTIONS);

        connectionHandler.executeGet();
        return connectionHandler;
    }

}
