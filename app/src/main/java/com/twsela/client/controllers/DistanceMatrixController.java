package com.twsela.client.controllers;


import com.twsela.client.models.entities.DistanceMatrixRow;
import com.twsela.client.models.responses.DistanceMatrixResponse;
import com.twsela.client.utils.Utils;

import java.util.List;

/**
 * Created by Shamyyoun on 1/16/17.
 */

public class DistanceMatrixController {

    public long getTotalDistance(DistanceMatrixResponse response) {
        try {
            long distance = 0;

            List<DistanceMatrixRow> rows = response.getRows();
            for (int i = 0; i < rows.size(); i++) {
                long tempDistance = rows.get(i).getElements().get(i).getDistance().getValue();
                distance += tempDistance;

                Utils.logE("Distance (" + i + ") = " + tempDistance);
            }

            Utils.logE("Total Distance: " + distance);

            return distance;
        } catch (Exception e) {
            return 0;
        }
    }
}
