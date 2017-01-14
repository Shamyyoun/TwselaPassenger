package com.twsela.client.models.payloads;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TripPayload {

    @SerializedName("driver_id")
    @Expose
    private String driverId;
    @SerializedName("trip_id")
    @Expose
    private String tripId;
    @SerializedName("driver_accept_time")
    @Expose
    private String driverAcceptTime;
    @SerializedName("car_id")
    @Expose
    private String carId;
    @SerializedName("status")
    @Expose
    private String status;

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getDriverAcceptTime() {
        return driverAcceptTime;
    }

    public void setDriverAcceptTime(String driverAcceptTime) {
        this.driverAcceptTime = driverAcceptTime;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}