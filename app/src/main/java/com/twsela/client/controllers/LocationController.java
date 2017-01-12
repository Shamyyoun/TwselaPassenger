package com.twsela.client.controllers;

import android.location.Location;

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
}
