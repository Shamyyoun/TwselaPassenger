package com.twsela.client;

import android.content.Context;

import com.twsela.client.connection.ConnectionHandler;
import com.twsela.client.connection.ConnectionListener;
import com.twsela.client.models.bodies.NearDriversBody;
import com.twsela.client.models.entities.MongoLocation;
import com.twsela.client.models.responses.DriversResponse;
import com.twsela.client.models.responses.LoginResponse;
import com.twsela.client.utils.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Shamyyoun on 5/31/16.
 */
public class ApiRequests {

    public static ConnectionHandler<LoginResponse> login(Context context, ConnectionListener<LoginResponse> listener,
                                                         String username, String password, String gcm) {

        // prepare url & tag
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
                                                                 double latitude, double longitude) {

        // prepare url & tag
        String url = AppUtils.getDriverApiUrl(Const.ROUTE_NEAR_DRIVERS);

        // create connection handler
        ConnectionHandler<DriversResponse> connectionHandler = new ConnectionHandler(context, url,
                DriversResponse.class, listener, Const.ROUTE_NEAR_DRIVERS);

        // create and set the body
        NearDriversBody body = new NearDriversBody();
        MongoLocation location = new MongoLocation();
        List<Double> coordinates = new ArrayList<>(2);
        coordinates.add(latitude);
        coordinates.add(longitude);
        location.setCoordinates(coordinates);
        body.setLocation(location);
        connectionHandler.setBody(body);

        // execute and return
        connectionHandler.executeRawJson();
        return connectionHandler;
    }

}
