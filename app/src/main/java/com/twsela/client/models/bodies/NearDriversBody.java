
package com.twsela.client.models.bodies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.twsela.client.models.entities.MongoLocation;

public class NearDriversBody {

    @SerializedName("location")
    @Expose
    private MongoLocation location;

    public MongoLocation getLocation() {
        return location;
    }

    public void setLocation(MongoLocation location) {
        this.location = location;
    }

}
