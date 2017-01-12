package com.twsela.client.models.entities;

/**
 * Created by Shamyyoun on 1/11/17.
 */

public class Trip {
    private AppLocation fromLocation;
    private AppLocation toLocation;

    public AppLocation getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(AppLocation fromLocation) {
        this.fromLocation = fromLocation;
    }

    public AppLocation getToLocation() {
        return toLocation;
    }

    public void setToLocation(AppLocation toLocation) {
        this.toLocation = toLocation;
    }
}
