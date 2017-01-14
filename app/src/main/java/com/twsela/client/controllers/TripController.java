package com.twsela.client.controllers;

import com.twsela.client.models.entities.Trip;

/**
 * Created by Shamyyoun on 1/14/17.
 */

public class TripController {
    public String getDriverUsername(Trip trip) {
        try {
            return trip.getDriver().getUserName();
        } catch (Exception e) {
            return null;
        }
    }
}
