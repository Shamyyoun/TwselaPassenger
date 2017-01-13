package com.twsela.client.controllers;

import android.location.Location;

import com.twsela.client.models.entities.MongoLocation;

/**
 * Created by Shamyyoun on 1/11/17.
 */

public class LocationController {
    public boolean isLocationValid(double lat, double lng) {
        return !((lat == 0 && lng == 0));
    }

    public boolean isLocationValid(Location location) {
        if (location == null) {
            return false;
        } else {
            return isLocationValid(location.getLatitude(), location.getLongitude());
        }
    }

    public double getLatitude(MongoLocation mongoLocation) {
        try {
            return mongoLocation.getCoordinates().get(0);
        } catch (Exception e) {
            return 0;
        }
    }

    public double getLongitude(MongoLocation mongoLocation) {
        try {
            return mongoLocation.getCoordinates().get(1);
        } catch (Exception e) {
            return 0;
        }
    }
}
