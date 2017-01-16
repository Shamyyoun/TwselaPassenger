package com.twsela.client.controllers;

import android.content.Context;

import com.twsela.client.Const;
import com.twsela.client.R;
import com.twsela.client.models.entities.Trip;
import com.twsela.client.utils.DateUtils;

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

    public String getDuration(Context context, Trip trip) {
        try {
            String str = "";
            long[] differenceArr = DateUtils.getDifferenceArray(trip.getStartTime(), trip.getEndTime(),
                    Const.SER_DATE_TIME_FORMAT);

            // add days
            if (differenceArr[0] != 0) {
                str += differenceArr[0] + " " + context.getString(R.string.days);
            }

            // add hours
            if (differenceArr[1] != 0) {
                if (!str.isEmpty()) {
                    str += ", ";
                }
                str += differenceArr[1] + " " + context.getString(R.string.hrs);
            }

            // add minuets
            if (differenceArr[2] != 0) {
                if (!str.isEmpty()) {
                    str += ", ";
                }
                str += differenceArr[2] + " " + context.getString(R.string.mins);
            }

            // check str
            if (!str.isEmpty()) {
                return str;
            } else {
                // return seconds
                str += differenceArr[3] + " " + context.getString(R.string.seconds);
                return str;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
