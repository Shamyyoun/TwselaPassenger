package com.twsela.client.models.enums;

/**
 * Created by Shamyyoun on 1/14/17.
 */

public enum NotificationKey {
    DRIVER_ACCEPTED_TRIP("Driver Accepted Trip"), DRIVER_ARRIVED("Driver Arrived"),
    DRIVER_STARTED_TRIP("Driver Start Trip"), DRIVER_ENDED_TRIP("Driver End Trip");

    private String value;

    NotificationKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
