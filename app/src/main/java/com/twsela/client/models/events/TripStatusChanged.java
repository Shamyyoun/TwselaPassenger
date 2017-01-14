package com.twsela.client.models.events;

import com.twsela.client.models.enums.TripStatus;

/**
 * Created by Shamyyoun on 1/14/17.
 */

public class TripStatusChanged {
    private String id;
    private TripStatus status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }
}
